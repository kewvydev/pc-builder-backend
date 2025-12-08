#!/usr/bin/env python3
"""
Carga masiva del dataset CSV a PostgreSQL según schema.sql.

Requisitos:
    pip install -r requirements.txt
    # O manualmente: pip install psycopg2-binary python-slugify python-dotenv

Configuración:
    Crea un archivo .env con las credenciales de la base de datos:
    
    PGHOST=localhost
    PGPORT=5432
    PGDATABASE=pcbuilder
    PGUSER=postgres
    PGPASSWORD=tu_password_aqui

Ejecución:
    # Usando variables de entorno desde .env
    python load-dataset.py
    
    # Usando DSN explícito
    python load-dataset.py --dsn "postgresql://user:password@localhost:5432/pcbuilder"
    
    # Especificando directorio del dataset
    python load-dataset.py --dataset-dir ./mi-dataset/csv
"""

import argparse
import csv
import hashlib
import os
import pathlib
import sys
from datetime import datetime
from slugify import slugify

import psycopg2
from psycopg2.extras import execute_batch, DictCursor
from dotenv import load_dotenv


# Directorio por defecto del dataset
DEFAULT_DATASET_DIR = pathlib.Path("dataset/csv")

# Campos "estándar" del esquema components
STANDARD_FIELDS = {
    "name",
    "brand",
    "price",
    "previous_price",
    "image_url",
    "product_url",
    "in_stock",
    "stock_units",
}

# Columnas que intentaremos mapear aunque tengan nombres distintos
ALIAS_MAP = {
    "url": "product_url",
    "link": "product_url",
    "image": "image_url",
    "img": "image_url",
    "current_price": "price",
    "price_usd": "price",
    "last_price": "previous_price",
    "stock": "in_stock",
    "available": "in_stock",
}

# Mapeo de categorías del CSV a los nombres en la BD
CATEGORY_MAP = {
    "cpu": "CPU",
    "processor": "CPU",
    "gpu": "GPU",
    "graphics_card": "GPU",
    "video_card": "GPU",
    "ram": "RAM",
    "memory": "RAM",
    "motherboard": "MOTHERBOARD",
    "mobo": "MOTHERBOARD",
    "storage": "STORAGE",
    "ssd": "STORAGE",
    "hdd": "STORAGE",
    "psu": "PSU",
    "power_supply": "PSU",
    "case": "CASE",
    "chassis": "CASE",
}


def normalize_field(field_name: str) -> str:
    """Normaliza nombres de campos usando el mapa de alias."""
    field_name = field_name.strip().lower()
    return ALIAS_MAP.get(field_name, field_name)


def normalize_category(category_name: str) -> str:
    """Normaliza nombres de categorías."""
    category_slug = slugify(category_name).replace("-", "_").lower()
    return CATEGORY_MAP.get(category_slug, category_slug.upper())


def bool_from_value(value: str):
    """Convierte un string a booleano."""
    if value is None:
        return None
    value = value.strip().lower()
    if value in {"true", "t", "1", "yes", "y", "available", "in stock"}:
        return True
    if value in {"false", "f", "0", "no", "n", "out of stock", "unavailable"}:
        return False
    return None


def hash_component_id(category: str, name: str, url: str) -> str:
    """Genera un ID único para un componente basado en categoría, nombre y URL."""
    base = f"{category}|{name}|{url}"
    return hashlib.sha1(base.encode("utf-8")).hexdigest()


def parse_csv(path: pathlib.Path):
    """Lee un archivo CSV y devuelve las filas como diccionarios."""
    with path.open("r", newline="", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for row in reader:
            yield row


def upsert_components(conn, category: str, rows):
    """Inserta o actualiza componentes en la base de datos."""
    components_payload = []
    attr_payload = []
    tag_payload = []

    now = datetime.utcnow()

    for raw in rows:
        # Normalizar nombres de campos
        normalized = {normalize_field(k): v.strip() if isinstance(v, str) else v
                      for k, v in raw.items() if k}

        name = normalized.get("name")
        if not name:
            continue

        brand = normalized.get("brand")
        product_url = normalized.get("product_url") or normalized.get("url") or ""
        component_id = hash_component_id(category, name, product_url)

        # Preparar datos del componente
        component = {
            "id": component_id,
            "category": category,
            "name": name,
            "brand": brand,
            "price": float(normalized["price"]) if normalized.get("price") else None,
            "previous_price": float(normalized["previous_price"]) if normalized.get("previous_price") else None,
            "image_url": normalized.get("image_url"),
            "product_url": product_url if product_url else None,
            "in_stock": bool_from_value(normalized.get("in_stock")) if normalized.get("in_stock") else True,
            "stock_units": int(normalized["stock_units"]) if normalized.get("stock_units") else 0,
            "last_updated": now,
        }
        components_payload.append(component)

        # Atributos adicionales (campos que no son estándar)
        for key, value in normalized.items():
            if key in STANDARD_FIELDS or not value:
                continue
            attr_payload.append({
                "component_id": component_id,
                "attribute_key": key,
                "attribute_value": value,
            })

        # Tags: marca + categoría
        tags = set()
        if brand:
            tags.add(brand)
        tags.add(category)
        for tag in tags:
            tag_payload.append({
                "component_id": component_id,
                "tag": tag,
            })

    # Insertar componentes
    with conn.cursor() as cur:
        if components_payload:
            execute_batch(
                cur,
                """
                INSERT INTO components (
                    id, category, name, brand, price, previous_price,
                    image_url, product_url, in_stock, stock_units, last_updated
                ) VALUES (
                    %(id)s, %(category)s, %(name)s, %(brand)s, %(price)s, %(previous_price)s,
                    %(image_url)s, %(product_url)s, %(in_stock)s, %(stock_units)s, %(last_updated)s
                )
                ON CONFLICT (id) DO UPDATE SET
                    brand = EXCLUDED.brand,
                    price = EXCLUDED.price,
                    previous_price = EXCLUDED.previous_price,
                    image_url = EXCLUDED.image_url,
                    product_url = EXCLUDED.product_url,
                    in_stock = EXCLUDED.in_stock,
                    stock_units = EXCLUDED.stock_units,
                    last_updated = EXCLUDED.last_updated,
                    updated_at = NOW();
                """,
                components_payload,
                page_size=100,
            )
            print(f"  ✓ Insertados/actualizados {len(components_payload)} componentes")

        # Insertar atributos
        if attr_payload:
            execute_batch(
                cur,
                """
                INSERT INTO component_attributes (component_id, attribute_key, attribute_value)
                VALUES (%(component_id)s, %(attribute_key)s, %(attribute_value)s)
                ON CONFLICT (component_id, attribute_key)
                DO UPDATE SET attribute_value = EXCLUDED.attribute_value;
                """,
                attr_payload,
                page_size=500,
            )
            print(f"  ✓ Insertados/actualizados {len(attr_payload)} atributos")

        # Insertar tags
        if tag_payload:
            execute_batch(
                cur,
                """
                INSERT INTO component_tags (component_id, tag)
                VALUES (%(component_id)s, %(tag)s)
                ON CONFLICT (component_id, normalized_tag) DO NOTHING;
                """,
                tag_payload,
                page_size=500,
            )
            print(f"  ✓ Insertados {len(tag_payload)} tags")

    conn.commit()


def import_dataset(dsn: str, dataset_dir: pathlib.Path):
    """Importa todos los archivos CSV del directorio del dataset."""
    print(f"Conectando a la base de datos...")
    conn = psycopg2.connect(dsn)
    
    try:
        # Verificar que las tablas existan
        with conn.cursor() as cur:
            cur.execute("""
                SELECT COUNT(*) FROM information_schema.tables 
                WHERE table_name = 'components'
            """)
            if cur.fetchone()[0] == 0:
                print("❌ Error: La tabla 'components' no existe.")
                print("   Por favor, ejecuta primero el schema.sql")
                return False

        csv_files = sorted(dataset_dir.glob("*.csv"))
        if not csv_files:
            print(f"❌ No se encontraron archivos CSV en {dataset_dir}/")
            return False

        print(f"\n📦 Encontrados {len(csv_files)} archivos CSV\n")

        total_components = 0
        for csv_path in csv_files:
            # Determinar categoría desde el nombre del archivo
            category_slug = csv_path.stem
            category = normalize_category(category_slug)
            
            print(f"📁 Procesando: {csv_path.name}")
            print(f"   Categoría: {category}")
            
            rows = list(parse_csv(csv_path))
            if not rows:
                print(f"   ⚠ Archivo vacío, saltando...")
                continue
            
            upsert_components(conn, category, rows)
            total_components += len(rows)
            print()

        print(f"✅ Carga completada: {total_components} componentes procesados")
        return True
        
    except Exception as e:
        print(f"❌ Error durante la importación: {e}")
        conn.rollback()
        return False
    finally:
        conn.close()


def build_dsn_from_env():
    """Construye el DSN desde variables de entorno."""
    host = os.getenv("PGHOST", "localhost")
    port = os.getenv("PGPORT", "5432")
    database = os.getenv("PGDATABASE", "pcbuilder")
    user = os.getenv("PGUSER", "postgres")
    password = os.getenv("PGPASSWORD", "")
    
    return f"postgresql://{user}:{password}@{host}:{port}/{database}"


def main():
    # Cargar variables de entorno desde .env si existe
    load_dotenv()
    
    parser = argparse.ArgumentParser(
        description="Carga CSV del dataset a PostgreSQL",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Ejemplos:
  # Usando variables de entorno (PGHOST, PGPORT, PGDATABASE, PGUSER, PGPASSWORD)
  # Las variables se pueden definir en un archivo .env
  python load-dataset.py
  
  # Usando DSN explícito
  python load-dataset.py --dsn "postgresql://user:pass@host:5432/db"
  
  # Especificando directorio del dataset
  python load-dataset.py --dataset-dir ./mi-dataset/csv
        """
    )
    
    parser.add_argument(
        "--dsn",
        help="Cadena DSN estilo postgresql://user:pass@host:puerto/db"
    )
    parser.add_argument(
        "--dataset-dir",
        type=pathlib.Path,
        default=DEFAULT_DATASET_DIR,
        help=f"Directorio con los archivos CSV (default: {DEFAULT_DATASET_DIR})"
    )
    
    args = parser.parse_args()
    
    # Determinar DSN
    dsn = args.dsn if args.dsn else build_dsn_from_env()
    
    print("=" * 60)
    print("  IMPORTADOR DE DATASET CSV A POSTGRESQL")
    print("=" * 60)
    
    if not args.dataset_dir.exists():
        print(f"❌ Error: El directorio {args.dataset_dir} no existe")
        return 1
    
    success = import_dataset(dsn, args.dataset_dir)
    return 0 if success else 1


if __name__ == "__main__":
    sys.exit(main())

