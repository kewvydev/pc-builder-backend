# 🚀 Getting Started - PCBuilder+ Backend

## ✅ Estado Actual del Proyecto

El backend está **100% listo** para conectarse con el frontend. Todas las funcionalidades principales están implementadas:

- ✅ API REST completa con 27 endpoints
- ✅ Manejo de excepciones global
- ✅ Validación de datos
- ✅ DTOs para separación de capas
- ✅ CORS configurado para localhost:3000
- ✅ Base de datos PostgreSQL configurada
- ✅ Sin errores de compilación

---

## 📋 Requisitos Previos

1. **Java 17** o superior
2. **Maven 3.8+** (incluido con el proyecto)
3. **PostgreSQL** (local o Neon)
4. **Python 3.8+** (para cargar datos iniciales)

---

## 🔧 Configuración Paso a Paso

### 1. Configurar Base de Datos

#### Opción A: PostgreSQL Local

1. Instalar PostgreSQL si no lo tienes
2. Crear la base de datos:

```bash
createdb pcbuilder
```

3. Crear archivo `.env` en la raíz del proyecto:

```bash
# Backend/.env
PGHOST=localhost
PGPORT=5432
PGDATABASE=pcbuilder
PGUSER=postgres
PGPASSWORD=tu_password_aqui
PGSSLMODE=disable
PGCHANNELBINDING=disable

SERVER_PORT=8080
```

#### Opción B: Neon (PostgreSQL en la nube - GRATIS)

1. Crear cuenta en [Neon](https://neon.tech)
2. Crear un proyecto nuevo
3. Copiar la connection string
4. Crear archivo `.env`:

```bash
# Backend/.env
SPRING_DATASOURCE_URL=jdbc:postgresql://ep-xxx.region.aws.neon.tech/neondb?sslmode=require
SPRING_DATASOURCE_USERNAME=usuario_neon
SPRING_DATASOURCE_PASSWORD=password_neon

SERVER_PORT=8080
```

### 2. Inicializar el Esquema de Base de Datos

El esquema se crea automáticamente al iniciar la aplicación (está en `src/main/resources/schema.sql`).

Si prefieres crearlo manualmente:

```bash
psql -U postgres -d pcbuilder -f src/main/resources/schema.sql
```

### 3. Cargar Datos de Ejemplo (Opcional pero Recomendado)

Instalar dependencias de Python:

```bash
pip install -r requirements.txt
```

Cargar dataset de ejemplo:

```bash
python load-dataset.py
```

Este script carga componentes de ejemplo en la base de datos para que puedas empezar a trabajar inmediatamente.

### 4. Iniciar el Backend

#### Con Maven Wrapper (recomendado):

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Mac/Linux
./mvnw spring-boot:run
```

#### Con Maven instalado:

```bash
mvn spring-boot:run
```

### 5. Verificar que Funciona

El backend debería iniciar en `http://localhost:8080`

Prueba estos endpoints:

```bash
# Ver todos los componentes
curl http://localhost:8080/api/components

# Ver componentes por categoría
curl http://localhost:8080/api/components/cpu

# Ver todos los builds
curl http://localhost:8080/api/builds
```

---

## 🌐 API Endpoints Disponibles

### **Components API** (`/api/components`)

#### CRUD Básico

```
GET    /api/components                    → Todos los componentes
GET    /api/components/{type}             → Por categoría (cpu, gpu, ram, etc.)
GET    /api/components/{type}/{id}        → Componente específico
GET    /api/components/id/{id}            → Por ID directo
POST   /api/components                    → Crear componente
PUT    /api/components/{id}               → Actualizar componente
DELETE /api/components/{id}               → Eliminar componente
```

#### Búsqueda y Filtrado

```
GET    /api/components/search?query=intel       → Buscar por nombre
GET    /api/components/search/cpu?query=intel   → Buscar en categoría
GET    /api/components/filter?brand=intel       → Filtrar por marca
GET    /api/components/filter?inStock=true      → Solo en stock
GET    /api/components/filter?minPrice=100&maxPrice=500  → Por rango de precio
GET    /api/components/cpu/filter?minPrice=200&maxPrice=400  → Categoría + precio
```

#### Utilidades

```
GET    /api/components/brands             → Todas las marcas disponibles
GET    /api/components/count              → Total de componentes
GET    /api/components/cpu/count          → Contar por categoría
POST   /api/components/bulk               → Crear múltiples componentes
```

### **Builds API** (`/api/builds`)

#### CRUD Básico

```
POST   /api/builds                        → Crear build
GET    /api/builds/{id}                   → Obtener build por ID
GET    /api/builds                        → Todos los builds
PUT    /api/builds/{id}                   → Actualizar build completo
DELETE /api/builds/{id}                   → Eliminar build
```

#### Gestión de Componentes

```
PUT    /api/builds/{id}/components        → Agregar/actualizar componente
DELETE /api/builds/{id}/components/{category}  → Remover componente
```

#### Validación y Análisis

```
POST   /api/builds/{id}/validate          → Validar compatibilidad
GET    /api/builds/{id}/alerts            → Ver alertas de compatibilidad
GET    /api/builds/{id}/recommendations   → Ver recomendaciones
GET    /api/builds/{id}/complete          → Verificar si está completo
```

---

## 📝 Ejemplos de Uso desde el Frontend

### Obtener Componentes por Categoría

```typescript
// TypeScript/JavaScript (Next.js)
async function getComponentsByCategory(category: string) {
  const response = await fetch(
    `http://localhost:8080/api/components/${category}`
  );
  if (!response.ok) throw new Error("Failed to fetch");
  return await response.json();
}

// Uso
const cpus = await getComponentsByCategory("cpu");
```

### Crear un Build

```typescript
async function createBuild(buildData: {
  name: string;
  budget?: number;
  components: Array<{ category: string; componentId: string }>;
}) {
  const response = await fetch("http://localhost:8080/api/builds", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(buildData),
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message);
  }

  return await response.json();
}

// Uso
const newBuild = await createBuild({
  name: "Mi PC Gaming",
  budget: 1500,
  components: [
    { category: "cpu", componentId: "intel-i7-12700k" },
    { category: "gpu", componentId: "nvidia-rtx-4070" },
  ],
});
```

### Buscar Componentes

```typescript
async function searchComponents(query: string, category?: string) {
  const url = category
    ? `http://localhost:8080/api/components/search/${category}?query=${query}`
    : `http://localhost:8080/api/components/search?query=${query}`;

  const response = await fetch(url);
  return await response.json();
}

// Uso
const results = await searchComponents("intel", "cpu");
```

---

## 🎨 Tipos TypeScript para el Frontend

Puedes crear estos tipos en tu frontend:

```typescript
// types/component.ts
export interface Component {
  id: string;
  name: string;
  brand: string;
  category: ComponentCategory;
  price: number;
  previousPrice?: number;
  inStock: boolean;
  stockUnits: number;
  imageUrl?: string;
  productUrl?: string;
  attributes: Record<string, string>;
  tags: string[];
}

export type ComponentCategory =
  | "CPU"
  | "GPU"
  | "MOTHERBOARD"
  | "RAM"
  | "STORAGE"
  | "PSU"
  | "CASE";

// types/build.ts
export interface BuildSummary {
  id: string;
  name: string;
  totalPrice: number;
  budget?: number;
  components: Component[];
  compatibilityAlerts: string[];
  recommendations: string[];
}

export interface BuildRequest {
  name: string;
  budget?: number;
  components: Array<{
    category: string;
    componentId: string;
  }>;
}

// types/error.ts
export interface ErrorResponse {
  status: number;
  error: string;
  message: string;
  path: string;
  timestamp: string;
  details?: string[];
}
```

---

## 🔍 Manejo de Errores

El backend retorna errores en formato JSON estándar:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Component not found with identifier: invalid-id",
  "path": "/api/components/id/invalid-id",
  "timestamp": "2024-12-08T12:00:00Z",
  "details": null
}
```

Ejemplo de manejo en el frontend:

```typescript
async function fetchWithErrorHandling(url: string, options?: RequestInit) {
  try {
    const response = await fetch(url, options);

    if (!response.ok) {
      const error: ErrorResponse = await response.json();
      throw new Error(error.message);
    }

    return await response.json();
  } catch (error) {
    console.error("API Error:", error);
    throw error;
  }
}
```

---

## ✅ Checklist Antes de Conectar con el Frontend

- [ ] Base de datos PostgreSQL configurada y corriendo
- [ ] Archivo `.env` creado con las credenciales correctas
- [ ] Backend inicia sin errores en `http://localhost:8080`
- [ ] Puedes hacer `curl http://localhost:8080/api/components` y obtienes respuesta
- [ ] CORS está configurado para `http://localhost:3000`
- [ ] (Opcional) Datos de ejemplo cargados con `load-dataset.py`

---

## 🎯 Siguiente Paso: Conectar el Frontend

En tu proyecto de Next.js/React:

1. **Crear un servicio API**:

```typescript
// services/api.ts
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

export const api = {
  components: {
    getAll: () => fetch(`${API_BASE_URL}/api/components`).then((r) => r.json()),
    getByCategory: (category: string) =>
      fetch(`${API_BASE_URL}/api/components/${category}`).then((r) => r.json()),
    search: (query: string) =>
      fetch(`${API_BASE_URL}/api/components/search?query=${query}`).then((r) =>
        r.json()
      ),
  },
  builds: {
    create: (data: BuildRequest) =>
      fetch(`${API_BASE_URL}/api/builds`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data),
      }).then((r) => r.json()),
    getAll: () => fetch(`${API_BASE_URL}/api/builds`).then((r) => r.json()),
  },
};
```

2. **Usar en tus componentes**:

```typescript
// components/ComponentList.tsx
import { useEffect, useState } from "react";
import { api } from "@/services/api";

export default function ComponentList() {
  const [components, setComponents] = useState([]);

  useEffect(() => {
    api.components.getAll().then(setComponents).catch(console.error);
  }, []);

  return (
    <div>
      {components.map((comp) => (
        <div key={comp.id}>
          {comp.name} - ${comp.price}
        </div>
      ))}
    </div>
  );
}
```

---

## 🐛 Troubleshooting

### El backend no inicia

- Verifica que Java 17+ está instalado: `java -version`
- Verifica las credenciales de la base de datos en `.env`
- Revisa los logs en la consola para ver el error específico

### Error de conexión a la base de datos

- Verifica que PostgreSQL está corriendo
- Verifica que las credenciales en `.env` son correctas
- Si usas Neon, verifica que la connection string incluye `?sslmode=require`

### CORS Error desde el frontend

- Verifica que el frontend corre en `http://localhost:3000`
- Si usas otro puerto, actualiza `CorsConfig.java` línea 21

### No hay datos

- Ejecuta `python load-dataset.py` para cargar datos de ejemplo
- O crea componentes manualmente con POST `/api/components`

---

## 📚 Recursos Adicionales

- [Documentación Spring Boot](https://spring.io/projects/spring-boot)
- [PostgreSQL Docs](https://www.postgresql.org/docs/)
- [Neon (PostgreSQL cloud)](https://neon.tech)

---

## 🎉 ¡Listo para Usar!

Tu backend está completamente funcional y listo para conectarse con cualquier frontend. Todos los endpoints están probados, validados y documentados.

**Puedes empezar a desarrollar el frontend inmediatamente** usando los endpoints y tipos definidos en esta guía.
