# Sistema de Gestión de Usuarios y Servicios - Microservicios

Sistema completo de gestión de usuarios con suscripciones, servicios contratados, pagos, facturación y notificaciones, implementado con arquitectura de microservicios.

## 🏗️ Arquitectura

El proyecto está compuesto por 4 contenedores Docker:

1. **postgres-db**: Base de datos PostgreSQL compartida
2. **app-core**: Microservicio principal (Puerto 8001)
3. **app-pagos**: Microservicio de pagos (Puerto 8002)
4. **app-notificaciones**: Microservicio de notificaciones (Puerto 8003)

## 📁 Estructura del Proyecto

```
proyecto-microservicios/
│
├── docker-compose.yml
├── .env
├── README.md
│
├── db/
│   └── init.sql
│
├── app-core/
│   ├── app/
│   │   ├── main.py
│   │   ├── database.py
│   │   ├── models/
│   │   ├── schemas/
│   │   ├── routers/
│   │   └── crud/
│   ├── requirements.txt
│   └── Dockerfile
│
├── app-pagos/
│   ├── app/
│   │   ├── main.py
│   │   ├── database.py
│   │   ├── models/
│   │   ├── schemas/
│   │   ├── routers/
│   │   └── crud/
│   ├── requirements.txt
│   └── Dockerfile
│
└── app-notificaciones/
    ├── app/
    │   ├── main.py
    │   ├── database.py
    │   ├── models/
    │   ├── schemas/
    │   ├── routers/
    │   ├── services/
    │   └── crud/
    ├── requirements.txt
    └── Dockerfile
```

## 🚀 Inicio Rápido

### Prerrequisitos

- Docker
- Docker Compose

### Instalación y Ejecución

1. Clonar o descargar el proyecto

2. Navegar al directorio del proyecto:
```bash
cd proyecto-microservicios
```

3. Levantar todos los servicios:
```bash
docker compose up --build
```

4. Los servicios estarán disponibles en:
   - **App Core**: http://localhost:8001
   - **App Pagos**: http://localhost:8002
   - **App Notificaciones**: http://localhost:8003
   - **PostgreSQL**: localhost:5432

## 📚 Documentación API

Cada microservicio tiene su propia documentación Swagger interactiva:

- **App Core**: http://localhost:8001/docs
- **App Pagos**: http://localhost:8002/docs
- **App Notificaciones**: http://localhost:8003/docs

## 🔍 Endpoints Principales

### App Core (Puerto 8001)

#### Usuarios
- `POST /usuarios` - Crear usuario
- `GET /usuarios` - Listar usuarios
- `GET /usuarios/{id}` - Obtener usuario
- `PUT /usuarios/{id}` - Actualizar usuario
- `DELETE /usuarios/{id}` - Eliminar usuario

#### Planes
- `POST /planes` - Crear plan
- `GET /planes` - Listar planes
- `GET /planes/{id}` - Obtener plan
- `PUT /planes/{id}` - Actualizar plan
- `DELETE /planes/{id}` - Eliminar plan

#### Servicios
- `POST /servicios` - Crear servicio
- `GET /servicios` - Listar servicios
- `GET /servicios/{id}` - Obtener servicio
- `PUT /servicios/{id}` - Actualizar servicio
- `DELETE /servicios/{id}` - Eliminar servicio

#### Métodos de Pago
- `POST /metodos-pago` - Crear método de pago
- `GET /metodos-pago` - Listar métodos de pago
- `GET /metodos-pago/{id}` - Obtener método de pago
- `PUT /metodos-pago/{id}` - Actualizar método de pago
- `DELETE /metodos-pago/{id}` - Eliminar método de pago

#### Suscripciones
- `POST /suscripciones` - Crear suscripción
- `GET /suscripciones` - Listar suscripciones
- `GET /suscripciones/{id}` - Obtener suscripción
- `GET /suscripciones/usuario/{id_usuario}` - Suscripciones por usuario
- `PUT /suscripciones/{id}` - Actualizar suscripción
- `DELETE /suscripciones/{id}` - Eliminar suscripción

#### Usuarios-Servicios
- `POST /usuarios-servicios` - Contratar servicio
- `GET /usuarios-servicios` - Listar servicios contratados
- `GET /usuarios-servicios/{id}` - Obtener servicio contratado
- `GET /usuarios-servicios/usuario/{id_usuario}` - Servicios por usuario
- `PUT /usuarios-servicios/{id}` - Actualizar servicio contratado
- `DELETE /usuarios-servicios/{id}` - Eliminar servicio contratado

#### Facturas
- `POST /facturas` - Crear factura
- `GET /facturas` - Listar facturas
- `GET /facturas/{id}` - Obtener factura
- `GET /facturas/pago/{id_pago}` - Facturas por pago
- `PUT /facturas/{id}` - Actualizar factura
- `DELETE /facturas/{id}` - Eliminar factura

#### Detalle Factura
- `POST /detalle-factura` - Crear detalle
- `GET /detalle-factura` - Listar detalles
- `GET /detalle-factura/{id}` - Obtener detalle
- `GET /detalle-factura/factura/{id_factura}` - Detalles por factura
- `PUT /detalle-factura/{id}` - Actualizar detalle
- `DELETE /detalle-factura/{id}` - Eliminar detalle

### App Pagos (Puerto 8002)

- `POST /pagos` - Registrar pago
- `GET /pagos` - Listar pagos
- `GET /pagos/{id}` - Obtener pago
- `GET /pagos/usuario/{id_usuario}` - Pagos por usuario
- `GET /pagos/periodo/{anio}/{mes}` - Pagos por período
- `GET /pagos/suscripcion/{id_suscripcion}` - Pagos por suscripción
- `PUT /pagos/{id}` - Actualizar pago
- `PATCH /pagos/{id}/estado` - Actualizar estado del pago
- `POST /pagos/{id}/anular` - Anular pago
- `DELETE /pagos/{id}` - Eliminar pago

### App Notificaciones (Puerto 8003)

- `POST /notificaciones/email` - Enviar email (simulado)
- `POST /notificaciones/sms` - Enviar SMS (simulado)
- `POST /notificaciones/interna` - Enviar notificación interna
- `GET /notificaciones` - Listar notificaciones
- `GET /notificaciones/{id}` - Obtener notificación
- `GET /notificaciones/usuario/{id_usuario}` - Notificaciones por usuario
- `PATCH /notificaciones/{id}/estado` - Actualizar estado
- `DELETE /notificaciones/{id}` - Eliminar notificación

## 🔧 Health Checks

Cada servicio expone un endpoint de salud:

- `GET /health` en cada microservicio

## 🗄️ Base de Datos

La base de datos PostgreSQL se inicializa automáticamente con:

- Estructura completa de tablas
- Datos de ejemplo
- Índices optimizados
- Constraints y validaciones

### Tablas Principales

- `usuario`
- `plan_mensual`
- `servicio`
- `metodo_pago`
- `suscripcion`
- `usuario_servicio`
- `pago`
- `factura`
- `detalle_factura`
- `notificacion`

## ⚙️ Variables de Entorno

El archivo `.env` contiene:

```env
POSTGRES_DB=gestion_usuarios_servicios
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
DB_HOST=postgres-db
DB_PORT=5432
DB_NAME=gestion_usuarios_servicios
DB_USER=postgres
DB_PASSWORD=postgres
```

## 🛑 Detener los Servicios

```bash
docker compose down
```

Para eliminar también los volúmenes:
```bash
docker compose down -v
```

## 🔄 Reconstruir los Servicios

```bash
docker compose up --build --force-recreate
```

## 📊 Tecnologías Utilizadas

- **Python 3.12**
- **FastAPI** - Framework web
- **SQLAlchemy** - ORM
- **Pydantic** - Validación de datos
- **PostgreSQL 15** - Base de datos
- **Docker & Docker Compose** - Contenedorización
- **Uvicorn** - Servidor ASGI

## 🎯 Características

- ✅ Arquitectura de microservicios
- ✅ Separación de responsabilidades
- ✅ Base de datos compartida
- ✅ Validaciones robustas
- ✅ Documentación automática (Swagger)
- ✅ CORS habilitado
- ✅ Health checks
- ✅ Persistencia de datos
- ✅ Código modular y escalable

## 📝 Notas

- Los envíos de email y SMS son simulados (se registran en base de datos)
- La base de datos incluye datos de ejemplo para pruebas
- Todos los servicios comparten la misma base de datos PostgreSQL
- Los servicios se reinician automáticamente en caso de fallo

## 👨‍💻 Desarrollo

Para desarrollo local sin Docker:

1. Instalar dependencias en cada servicio:
```bash
cd app-core
pip install -r requirements.txt
```

2. Configurar variables de entorno

3. Ejecutar cada servicio:
```bash
uvicorn app.main:app --reload --port 8001
```

## 📄 Licencia

Proyecto de ejemplo para gestión de usuarios y servicios.
