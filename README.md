# PCBuilder+ Backend 🖥️

**Backend REST API para PCBuilder+** - Sistema de recomendación y ensamblaje de PCs con validación de compatibilidad.

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

---

## 🚀 Estado del Proyecto

✅ **Backend 100% Funcional** - Listo para conectarse con el frontend

### Características Implementadas

- ✅ **27 Endpoints REST** completamente funcionales
- ✅ **Manejo global de excepciones** con respuestas JSON estandarizadas
- ✅ **Validación automática** de requests con Spring Validation
- ✅ **DTOs** para separación de capas (dominio vs API)
- ✅ **CORS configurado** para desarrollo con frontend
- ✅ **PostgreSQL** como base de datos con soporte para Neon
- ✅ **Validación de compatibilidad** entre componentes
- ✅ **Sistema de recomendaciones** automático
- ✅ **Búsqueda y filtrado avanzado** de componentes

---

## 📋 Contenido

- [Inicio Rápido](#-inicio-rápido)
- [API Endpoints](#-api-endpoints)
- [Arquitectura](#-arquitectura)
- [Base de Datos](#-base-de-datos)
- [Desarrollo](#-desarrollo)

---

## 🏃 Inicio Rápido

### Requisitos

- Java 17 o superior
- Maven 3.8+ (incluido)
- PostgreSQL 15+ o cuenta en [Neon](https://neon.tech)
- Python 3.8+ (opcional, para cargar datos)

### 1. Clonar y Configurar

```bash
cd backend

# Crear archivo .env con tus credenciales de base de datos
cp .env.example .env
# Editar .env con tus datos
```

### 2. Configurar Base de Datos

```bash
# PostgreSQL Local
createdb pcbuilder

# El esquema se crea automáticamente al iniciar
```

### 3. Iniciar el Backend

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Mac/Linux
./mvnw spring-boot:run
```

El servidor estará disponible en `http://localhost:8080`

### 4. (Opcional) Cargar Datos de Ejemplo

```bash
pip install -r requirements.txt
python load-dataset.py
```

### 5. Probar la API

```bash
curl http://localhost:8080/api/components
curl http://localhost:8080/api/components/cpu
```

**📖 Para instrucciones detalladas, ver [GETTING_STARTED.md](GETTING_STARTED.md)**

---

## 🌐 API Endpoints

### Components API (`/api/components`)

#### CRUD

```
GET    /api/components                    - Listar todos
GET    /api/components/{category}         - Por categoría
GET    /api/components/{category}/{id}    - Uno específico
POST   /api/components                    - Crear
PUT    /api/components/{id}               - Actualizar
DELETE /api/components/{id}               - Eliminar
```

#### Búsqueda y Filtrado

```
GET    /api/components/search?query=...           - Buscar por nombre
GET    /api/components/filter?brand=...           - Filtrar por criterios
GET    /api/components/{cat}/filter?minPrice=...  - Filtrar por precio
GET    /api/components/brands                     - Listar marcas
```

### Builds API (`/api/builds`)

#### CRUD

```
POST   /api/builds                        - Crear build
GET    /api/builds/{id}                   - Obtener por ID
GET    /api/builds                        - Listar todos
PUT    /api/builds/{id}                   - Actualizar
DELETE /api/builds/{id}                   - Eliminar
```

#### Gestión de Componentes

```
PUT    /api/builds/{id}/components               - Agregar componente
DELETE /api/builds/{id}/components/{category}    - Remover componente
```

#### Validación

```
POST   /api/builds/{id}/validate          - Validar compatibilidad
GET    /api/builds/{id}/alerts            - Ver alertas
GET    /api/builds/{id}/recommendations   - Ver recomendaciones
```

---

## 🏗️ Arquitectura

```
src/main/java/com/pcBuilder/backend/
├── controller/          # REST Controllers
├── service/             # Lógica de negocio
├── repository/          # Acceso a datos (JPA)
├── model/               # Entidades y objetos de dominio
├── dto/                 # Data Transfer Objects
├── mapper/              # Conversión Domain ↔ DTO
├── exception/           # Excepciones personalizadas
└── config/              # Configuración (CORS, etc.)
```

### Capas

1. **Controller**: Maneja HTTP requests/responses, valida entrada
2. **Service**: Lógica de negocio, validación de compatibilidad
3. **Repository**: Acceso a PostgreSQL vía Spring Data JPA
4. **Mapper**: Convierte entre objetos de dominio y DTOs
5. **Exception Handler**: Manejo centralizado de errores

---

## 🗄️ Base de Datos

### Esquema

```
components              - Componentes de hardware
├── component_attributes - Atributos técnicos (specs)
└── component_tags      - Tags para búsqueda

builds                  - Builds de usuarios
├── build_components    - Componentes seleccionados
├── build_alerts        - Alertas de compatibilidad
└── build_recommendations - Recomendaciones

scraping_logs          - Logs del scraper (futuro)
```

### Categorías Soportadas

- `CPU` - Procesadores
- `GPU` - Tarjetas gráficas
- `MOTHERBOARD` - Placas base
- `RAM` - Memoria RAM
- `STORAGE` - Almacenamiento (SSD/HDD)
- `PSU` - Fuentes de poder
- `CASE` - Gabinetes

---

## 🛠️ Desarrollo

### Estructura del Proyecto

```
backend/
├── src/main/java/           # Código fuente
├── src/main/resources/      # Configuración
│   ├── application.properties
│   └── schema.sql
├── src/test/java/           # Tests
├── pom.xml                  # Dependencias Maven
├── .env                     # Variables de entorno (no comitear)
├── GETTING_STARTED.md       # Guía de inicio
└── IMPLEMENTATION_SUMMARY.md # Resumen técnico
```

### Tecnologías

- **Spring Boot 3.3.4** - Framework principal
- **Spring Data JPA** - ORM para PostgreSQL
- **Spring Validation** - Validación de datos
- **Lombok** - Reduce boilerplate
- **PostgreSQL** - Base de datos
- **JSoup** - Web scraping (futuro)

### Variables de Entorno

Crear `.env` con:

```env
# Base de datos
PGHOST=localhost
PGPORT=5432
PGDATABASE=pcbuilder
PGUSER=postgres
PGPASSWORD=tu_password

# Servidor
SERVER_PORT=8080
```

### Ejecutar Tests

```bash
./mvnw test
```

### Build para Producción

```bash
./mvnw clean package
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

---

## 📝 Validación de Compatibilidad

El sistema valida automáticamente:

1. **Compatibilidad de Socket** (CPU ↔ Motherboard)
2. **Tipo de RAM** (DDR4/DDR5)
3. **Consumo de Energía vs PSU**
4. **Tamaño del Gabinete**

Y genera recomendaciones basadas en:

- Componentes faltantes
- Presupuesto disponible
- Balance de configuración

---

## 🤝 Integración con Frontend

### Ejemplo de Uso (TypeScript)

```typescript
// Obtener componentes por categoría
const response = await fetch("http://localhost:8080/api/components/cpu");
const cpus = await response.json();

// Crear un build
const build = await fetch("http://localhost:8080/api/builds", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({
    name: "Mi PC Gaming",
    budget: 1500,
    components: [
      { category: "cpu", componentId: "intel-i7-12700k" },
      { category: "gpu", componentId: "nvidia-rtx-4070" },
    ],
  }),
});
```

Ver [GETTING_STARTED.md](GETTING_STARTED.md) para ejemplos completos.

---

## 📚 Documentación Adicional

- [GETTING_STARTED.md](GETTING_STARTED.md) - Guía completa de inicio
- [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md) - Detalles técnicos de implementación
- [schema.sql](src/main/resources/schema.sql) - Esquema de base de datos

---

## 🔜 Próximas Características

- [ ] Web scraping automático con JSoup
- [ ] Autenticación con Spring Security
- [ ] API Documentation con Swagger/OpenAPI
- [ ] WebSocket para updates en tiempo real
- [ ] Caché con Redis
- [ ] Monitoreo con Spring Actuator

---

## 📄 Licencia

Este proyecto está bajo la licencia MIT.

---

## 👨‍💻 Autor

Desarrollado para PCBuilder+ - Sistema inteligente de ensamblaje de PCs

---

## 🆘 Soporte

¿Problemas? Ver [Troubleshooting](GETTING_STARTED.md#-troubleshooting) en la guía de inicio.
