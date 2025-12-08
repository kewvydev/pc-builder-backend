#!/usr/bin/env python3
"""
Carga masiva del dataset/csv a PostgreSQL según schema.sql.

Requisitos:
    pip install psycopg2-binary python-slugify
    (opcional) pip install python-dotenv  # si prefieres leer credenciales desde .env

Ejecución:
    python load_dataset.py --dsn "postgresql://user:password@localhost:5432/pcbuilder"
"""

import argparse
import csv
import hashlib
import pathlib
import sys
from datetime import datetime
from slugify import slugify

import psycopg2
from psycopg2.extras import execute_batch, DictCursor


DATASET_DIR = pathlib.Path("dataset/csv")

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
}


def normalize_field(field_name: str) -> str:
    field_name = field_name.strip().lower()
    return ALIAS_MAP.get(field_name, field_name)


def bool_from_value(value: str):
    if value is None:
        return None
    value = value.strip().lower()
    if value in {"true", "t", "1", "yes", "y"}:
        return True
    if value in {"false", "f", "0", "no", "n"}:
        return False
    return None


def hash_component_id(category: str, name: str, url: str) -> str:
    base = f"{category}|{name}|{url}"
    return hashlib.sha1(base.encode("utf-8")).hexdigest()


def parse_csv(path: pathlib.Path):
    with path.open("r", newline="", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for row in reader:
            yield row


def upsert_components(conn, category_slug: str, rows):
    components_payload = []
    attr_payload = []
    tag_payload = []

    now = datetime.utcnow()

    for raw in rows:
        normalized = {normalize_field(k): v.strip() if isinstance(v, str) else v
                      for k, v in raw.items() if k}

        name = normalized.get("name")
        brand = normalized.get("brand")
        if not name:
            continue

        product_url = normalized.get("product_url") or normalized.get("url")
        component_id = hash_component_id(category_slug, name, product_url or "")

        # Campos estándar
        component = {
            "id": component_id,
            "category": category_slug,
            "name": name,
            "brand": brand,
            "price": float(normalized["price"]) if normalized.get("price") else None,
            "previous_price": float(normalized["previous_price"]) if normalized.get("previous_price") else None,
            "image_url": normalized.get("image_url"),
            "product_url": product_url,
            "in_stock": bool_from_value(normalized.get("in_stock")) if normalized.get("in_stock") else True,
            "stock_units": int(normalized["stock_units"]) if normalized.get("stock_units") else 0,
            "last_updated": now,
        }
        components_payload.append(component)

        # Atributos adicionales
        for key, value in normalized.items():
            if key in STANDARD_FIELDS or not value:
                continue
            attr_payload.append({
                "component_id": component_id,
                "attribute_key": key,
                "attribute_value": value,
            })

        # Tags: marca + categoría (puedes extender con otras columnas)
        tags = set()
        if brand:
            tags.add(brand)
        tags.add(category_slug)
        for tag in tags:
            tag_payload.append({
                "component_id": component_id,
                "tag": tag,
            })

    with conn.cursor() as cur:
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

    conn.commit()


def import_dataset(dsn: str):
    conn = psycopg2.connect(dsn)
    try:
        csv_files = sorted(DATASET_DIR.glob("*.csv"))
        if not csv_files:
            print("No se encontraron CSV en dataset/csv/")
            return

        for csv_path in csv_files:
            category_slug = slugify(csv_path.stem).replace("-", "_")
            # Si tus categorías están alineadas con los enums existentes,
            # reemplaza el slug por el nombre real (p.ej. 'memory' -> 'RAM').
            print(f"Ingresando {csv_path.name} como categoría '{category_slug}'...")
            rows = list(parse_csv(csv_path))
            if not rows:
                continue
            upsert_components(conn, category_slug.upper(), rows)

        print("Carga completada.")
    finally:
        conn.close()


def main():
    parser = argparse.ArgumentParser(description="Carga CSV a PostgreSQL")
    parser.add_argument("--dsn", required=True,
                        help="Cadena DSN estilo postgresql://user:pass@host:puerto/db")
    args = parser.parse_args()
    import_dataset(args.dsn)


if __name__ == "__main__":
    sys.exit(main())