# Sistema de Gestión de Usuarios y Servicios - Microservicios

![Version](https://img.shields.io/badge/version-1.0.0-blue)
![Python](https://img.shields.io/badge/python-3.12+-green)
![Docker](https://img.shields.io/badge/docker-required-blue)

Sistema empresarial completo de gestión de usuarios con suscripciones, servicios contratados, pagos, facturación y notificaciones, implementado con arquitectura de microservicios escalable y modular.

## 📋 Tabla de Contenidos

- [Características](#características)
- [Arquitectura](#arquitectura)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Requisitos Previos](#requisitos-previos)
- [Instalación y Ejecución](#instalación-y-ejecución)
- [Documentación API](#documentación-api)
- [Configuración](#configuración)
- [Desarrollo Local](#desarrollo-local)
- [Troubleshooting](#troubleshooting)
- [Tecnologías](#tecnologías)
- [Contribución](#contribución)

## 🎯 Características

- ✅ **Arquitectura de Microservicios** - 3 servicios independientes con responsabilidades bien definidas
- ✅ **Gestión de Usuarios** - CRUD completo con validaciones robustas
- ✅ **Sistema de Suscripciones** - Planes mensuales y gestión de ciclo de vida
- ✅ **Servicios Contratados** - Relación múltiple entre usuarios y servicios
- ✅ **Procesamiento de Pagos** - Integración modular para pagos y seguimiento
- ✅ **Facturación Automática** - Generación y detalles de facturas
- ✅ **Sistema de Notificaciones** - Email, SMS (simulados) y notificaciones internas
- ✅ **Métodos de Pago** - Múltiples opciones de pago por usuario
- ✅ **Health Checks** - Monitoreo de salud de servicios
- ✅ **Documentación Interactiva** - Swagger/OpenAPI automático
- ✅ **CORS Habilitado** - Listo para consumo desde frontend
- ✅ **Persistencia de Datos** - Base de datos PostgreSQL compartida
- ✅ **Código Modular y Escalable** - Fácil de mantener y extender
- ✅ **Contenedorización Completa** - Docker Compose con orquestación automática

## 🏗️ Arquitectura

### Diagrama de Componentes

```
┌─────────────────────────────────────────────────────────────────┐
│                    CLIENTE / FRONTEND                            │
└────────────────────┬──────────────────────────────────────────────┘
                     │
         ┌───────────┼───────────┐
         │           │           │
    ┌────▼────┐  ┌───▼────┐  ┌──▼──────────┐
    │ App Core│  │ Pagos  │  │Notificaciones│
    │ :8001   │  │ :8002  │  │  :8003       │
    └────┬────┘  └───┬────┘  └──┬──────────┘
         │           │           │
         └───────────┼───────────┘
                     │
            ┌────────▼──────────┐
            │ PostgreSQL 15     │
            │ (Base de Datos)   │
            └───────────────────┘
```

### Microservicios

| Servicio | Puerto | Responsabilidad |
|----------|--------|-----------------|
| **app-core** | 8001 | Usuarios, planes, servicios, suscripciones, facturas |
| **app-pagos** | 8002 | Procesamiento de pagos, seguimiento, validaciones |
| **app-notificaciones** | 8003 | Email, SMS, notificaciones internas |

## 📁 Estructura del Proyecto

```
proyecto-microservicios/
│
├── docker-compose.yml          # Orquestación de contenedores
├── .env                        # Variables de entorno
├── README.md                   # Este archivo
│
├── db/
│   └── init.sql               # Script de inicialización de BD
│
├── app-core/                  # MICROSERVICIO PRINCIPAL
│   ├── app/
│   │   ├── main.py            # Punto de entrada FastAPI
│   │   ├── database.py        # Configuración de conexión BD
│   │   ├── models/            # Modelos SQLAlchemy
│   │   ├── schemas/           # Esquemas Pydantic
│   │   ├── routers/           # Endpoints organizados por recurso
│   │   └── crud/              # Operaciones de base de datos
│   ├── requirements.txt        # Dependencias Python
│   └── Dockerfile             # Imagen Docker
│
├── app-pagos/                 # MICROSERVICIO DE PAGOS
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
└── app-notificaciones/        # MICROSERVICIO DE NOTIFICACIONES
    ├── app/
    │   ├── main.py
    │   ├── database.py
    │   ├── models/
    │   ├── schemas/
    │   ├── routers/
    │   ├── services/          # Servicios de envío (email, SMS, etc)
    │   └── crud/
    ├── requirements.txt
    └── Dockerfile
```

## ⚙️ Requisitos Previos

### Sistema

- **OS**: Linux, macOS o Windows (con WSL2)
- **RAM Mínima**: 2 GB
- **Almacenamiento**: 2 GB disponibles
- **Conexión a Internet**: Para descargar imágenes Docker

### Software Requerido

- **Docker**: v20.10 o superior ([Descargar](https://www.docker.com/products/docker-desktop))
- **Docker Compose**: v2.0 o superior (incluido en Docker Desktop)

### Verificar Instalación

```bash
docker --version
docker compose version
```

## 🚀 Instalación y Ejecución

### 1. Clonar o Descargar el Proyecto

```bash
git clone <repositorio>
# o descargar y extraer manualmente
cd proyecto-microservicios
```

### 2. Configurar Variables de Entorno

Crear archivo `.env` en la raíz del proyecto:

```env
# Base de Datos
POSTGRES_DB=gestion_usuarios_servicios
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
DB_HOST=postgres-db
DB_PORT=5432
DB_NAME=gestion_usuarios_servicios
DB_USER=postgres
DB_PASSWORD=postgres

# Aplicaciones
PYTHONUNBUFFERED=1
```

### 3. Levantar los Servicios

```bash
# Construir e iniciar todos los contenedores
docker compose up --build

# O en segundo plano
docker compose up -d --build

# Ver logs en tiempo real
docker compose logs -f
```

### 4. Verificar que los Servicios Estén Activos

```bash
# Revisar estado de contenedores
docker compose ps

# Probar conectividad
curl http://localhost:8001/health
curl http://localhost:8002/health
curl http://localhost:8003/health
```

## 📚 Documentación API

Cada microservicio expone documentación interactiva Swagger:

| Servicio | URL Swagger |
|----------|------------|
| App Core | http://localhost:8001/docs |
| App Pagos | http://localhost:8002/docs |
| App Notificaciones | http://localhost:8003/docs |

También disponible ReDoc (alternativa a Swagger):

| Servicio | URL ReDoc |
|----------|-----------|
| App Core | http://localhost:8001/redoc |
| App Pagos | http://localhost:8002/redoc |
| App Notificaciones | http://localhost:8003/redoc |

## 🔍 Endpoints Principales

### App Core - Usuarios (Puerto 8001)

```
POST   /usuarios              # Crear usuario
GET    /usuarios              # Listar usuarios con paginación
GET    /usuarios/{id}         # Obtener usuario específico
PUT    /usuarios/{id}         # Actualizar usuario
DELETE /usuarios/{id}         # Eliminar usuario
```

**Ejemplo:**
```bash
curl -X POST http://localhost:8001/usuarios \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Juan",
    "email": "juan@example.com",
    "telefono": "+34123456789"
  }'
```

### App Core - Planes Mensuales

```
POST   /planes                # Crear plan
GET    /planes                # Listar planes
GET    /planes/{id}           # Obtener plan
PUT    /planes/{id}           # Actualizar plan
DELETE /planes/{id}           # Eliminar plan
```

### App Core - Servicios

```
POST   /servicios             # Crear servicio
GET    /servicios             # Listar servicios
GET    /servicios/{id}        # Obtener servicio
PUT    /servicios/{id}        # Actualizar servicio
DELETE /servicios/{id}        # Eliminar servicio
```

### App Core - Métodos de Pago

```
POST   /metodos-pago          # Crear método de pago
GET    /metodos-pago          # Listar métodos
GET    /metodos-pago/{id}     # Obtener método
PUT    /metodos-pago/{id}     # Actualizar método
DELETE /metodos-pago/{id}     # Eliminar método
```

### App Core - Suscripciones

```
POST   /suscripciones                      # Crear suscripción
GET    /suscripciones                      # Listar suscripciones
GET    /suscripciones/{id}                 # Obtener suscripción
GET    /suscripciones/usuario/{id_usuario} # Suscripciones por usuario
PUT    /suscripciones/{id}                 # Actualizar suscripción
DELETE /suscripciones/{id}                 # Cancelar suscripción
```

### App Core - Usuarios-Servicios

```
POST   /usuarios-servicios                      # Contratar servicio
GET    /usuarios-servicios                      # Listar contratos
GET    /usuarios-servicios/{id}                 # Obtener contrato
GET    /usuarios-servicios/usuario/{id_usuario} # Servicios por usuario
PUT    /usuarios-servicios/{id}                 # Actualizar contrato
DELETE /usuarios-servicios/{id}                 # Cancelar servicio
```

### App Core - Facturas

```
POST   /facturas                    # Crear factura
GET    /facturas                    # Listar facturas
GET    /facturas/{id}               # Obtener factura
GET    /facturas/pago/{id_pago}     # Facturas por pago
PUT    /facturas/{id}               # Actualizar factura
DELETE /facturas/{id}               # Eliminar factura
```

### App Core - Detalle de Facturas

```
POST   /detalle-factura                      # Crear detalle
GET    /detalle-factura                      # Listar detalles
GET    /detalle-factura/{id}                 # Obtener detalle
GET    /detalle-factura/factura/{id_factura} # Detalles por factura
PUT    /detalle-factura/{id}                 # Actualizar detalle
DELETE /detalle-factura/{id}                 # Eliminar detalle
```

### App Pagos (Puerto 8002)

```
POST   /pagos                              # Registrar pago
GET    /pagos                              # Listar pagos
GET    /pagos/{id}                         # Obtener pago
GET    /pagos/usuario/{id_usuario}         # Pagos de usuario
GET    /pagos/periodo/{anio}/{mes}         # Pagos por período
GET    /pagos/suscripcion/{id_suscripcion} # Pagos de suscripción
PUT    /pagos/{id}                         # Actualizar pago
PATCH  /pagos/{id}/estado                  # Cambiar estado
POST   /pagos/{id}/anular                  # Anular pago
DELETE /pagos/{id}                         # Eliminar pago
```

### App Notificaciones (Puerto 8003)

```
POST   /notificaciones/email                    # Enviar email
POST   /notificaciones/sms                      # Enviar SMS
POST   /notificaciones/interna                  # Notificación interna
GET    /notificaciones                          # Listar notificaciones
GET    /notificaciones/{id}                     # Obtener notificación
GET    /notificaciones/usuario/{id_usuario}     # Por usuario
PATCH  /notificaciones/{id}/estado              # Cambiar estado
DELETE /notificaciones/{id}                     # Eliminar notificación
```

## 🔧 Health Checks

Cada servicio expone un endpoint de salud:

```bash
# App Core
curl http://localhost:8001/health

# App Pagos
curl http://localhost:8002/health

# App Notificaciones
curl http://localhost:8003/health
```

Respuesta exitosa:
```json
{
  "status": "healthy",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## 🗄️ Base de Datos

### Características

- **Motor**: PostgreSQL 15
- **Inicialización Automática**: Al levantar con `docker compose up`
- **Persistencia**: Datos se guardan entre reinicios
- **Acceso**: `localhost:5432`

### Tablas Principales

| Tabla | Descripción |
|-------|------------|
| `usuario` | Información de usuarios registrados |
| `plan_mensual` | Planes de suscripción disponibles |
| `servicio` | Servicios ofrecidos |
| `metodo_pago` | Métodos de pago de usuarios |
| `suscripcion` | Suscripciones activas de usuarios |
| `usuario_servicio` | Servicios contratados por usuario |
| `pago` | Registro de pagos realizados |
| `factura` | Facturas generadas |
| `detalle_factura` | Detalles de cada factura |
| `notificacion` | Notificaciones enviadas/pendientes |

### Conectarse a la Base de Datos

```bash
# Con psql
psql -h localhost -U postgres -d gestion_usuarios_servicios

# Con Docker
docker compose exec postgres-db psql -U postgres -d gestion_usuarios_servicios
```

## ⚙️ Configuración

### Variables de Entorno

El archivo `.env` controla:

```env
# Credenciales PostgreSQL
POSTGRES_DB=gestion_usuarios_servicios
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres

# Conexión desde aplicaciones
DB_HOST=postgres-db
DB_PORT=5432
DB_NAME=gestion_usuarios_servicios
DB_USER=postgres
DB_PASSWORD=postgres

# Python
PYTHONUNBUFFERED=1
```

### Personalizar Configuración

Para cambiar puertos, credenciales u otros parámetros:

1. Editar `.env`
2. Actualizar `docker-compose.yml` si es necesario
3. Ejecutar: `docker compose up --build -d`

## 💻 Desarrollo Local

### Sin Docker (Desarrollo)

#### 1. Crear Entorno Virtual

```bash
# Linux/macOS
python3 -m venv venv
source venv/bin/activate

# Windows
python -m venv venv
venv\Scripts\activate
```

#### 2. Instalar Dependencias

```bash
cd app-core
pip install -r requirements.txt
```

#### 3. Configurar Base de Datos

```bash
# Asegúrate de tener PostgreSQL corriendo
# Crear base de datos
psql -U postgres -c "CREATE DATABASE gestion_usuarios_servicios;"

# Ejecutar migrations (si existen)
# python -m alembic upgrade head
```

#### 4. Ejecutar Aplicación

```bash
uvicorn app.main:app --reload --port 8001
```

Repetir pasos 2-4 para `app-pagos` (puerto 8002) y `app-notificaciones` (puerto 8003)

### Con Docker Compose para Desarrollo

```bash
# Ver logs en tiempo real
docker compose logs -f app-core

# Entrar a contenedor
docker compose exec app-core bash

# Ejecutar comandos en contenedor
docker compose exec app-core python -c "import app"
```

## 🐛 Troubleshooting

### Problema: "Error de conexión a la base de datos"

**Solución:**
```bash
# Verificar que PostgreSQL está activo
docker compose ps | grep postgres

# Reiniciar servicios
docker compose restart

# Revisar logs
docker compose logs postgres-db
```

### Problema: "Puerto 8001/8002/8003 ya en uso"

**Solución:**
```bash
# Ver qué proceso usa el puerto
lsof -i :8001  # macOS/Linux
netstat -ano | findstr :8001  # Windows

# Cambiar puertos en docker-compose.yml
# O detener el proceso que usa el puerto
```

### Problema: "Contenedores no se inician"

**Solución:**
```bash
# Reconstruir completamente
docker compose down -v
docker compose up --build

# Limpiar imágenes no usadas
docker image prune -a
```

### Problema: "Datos no persisten"

**Solución:**
```bash
# Verificar volúmenes
docker volume ls

# Recrear volúmenes
docker compose down -v
docker compose up -d
```

### Problema: "Error de permisos en Linux"

**Solución:**
```bash
# Añadir usuario al grupo docker
sudo usermod -aG docker $USER

# Aplicar cambios (logout/login o ejecutar)
newgrp docker
```

## 📊 Tecnologías Utilizadas

### Backend

| Tecnología | Versión | Uso |
|-----------|---------|-----|
| Python | 3.12+ | Lenguaje principal |
| FastAPI | 0.100+ | Framework web asincrónico |
| SQLAlchemy | 2.0+ | ORM para base de datos |
| Pydantic | 2.0+ | Validación de datos |
| Uvicorn | 0.23+ | Servidor ASGI |

### Infraestructura

| Tecnología | Versión | Uso |
|-----------|---------|-----|
| PostgreSQL | 15 | Base de datos relacional |
| Docker | 20.10+ | Contenedorización |
| Docker Compose | 2.0+ | Orquestación |

### Características de FastAPI

- 📖 Documentación automática (Swagger UI)
- 🔄 Reload automático en desarrollo
- ⚡ Alto rendimiento (ASGI)
- 🛡️ Validación de datos automática
- 📊 Esquemas OpenAPI

## 📋 Datos de Ejemplo

La base de datos se inicializa con datos de ejemplo:

- 5 usuarios de prueba
- 3 planes mensuales
- 10 servicios disponibles
- 2 métodos de pago por usuario
- Suscripciones y pagos de ejemplo

**Nota**: Estos datos se crean automáticamente en la primera ejecución.

## 🔄 Comandos Útiles

```bash
# Iniciar servicios
docker compose up -d

# Ver estado
docker compose ps

# Ver logs
docker compose logs -f [servicio]

# Detener servicios
docker compose stop

# Reiniciar servicios
docker compose restart [servicio]

# Reconstruir imágenes
docker compose build --no-cache

# Eliminar todo (incluidos volúmenes)
docker compose down -v

# Ejecutar comando en contenedor
docker compose exec [servicio] [comando]

# Acceder a bash en contenedor
docker compose exec [servicio] bash
```

## 🔐 Seguridad

**Nota**: Esta es una aplicación de demostración. Para producción:

- ✅ Cambiar credenciales de base de datos
- ✅ Implementar autenticación (JWT, OAuth)
- ✅ Usar HTTPS/TLS
- ✅ Implementar rate limiting
- ✅ Validar y sanitizar inputs
- ✅ Usar secrets management
- ✅ Implementar logging y monitoreo
- ✅ Configurar CORS restringido

## 📈 Escalabilidad

Para escalar a producción:

1. **Separar bases de datos** por microservicio
2. **Implementar API Gateway** (Kong, nginx)
3. **Añadir caché** (Redis)
4. **Implementar colas** (RabbitMQ, Celery)
5. **Usar balancador de carga**
6. **Monitoreo** (Prometheus, Grafana)
7. **Logging centralizado** (ELK Stack)
8. **CI/CD** (GitHub Actions, Jenkins)

## 🤝 Contribución

1. Fork el proyecto
2. Crear rama de feature (`git checkout -b feature/AmazingFeature`)
3. Commit cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abrir Pull Request

## 📞 Soporte

Para problemas o preguntas:

1. Revisar sección [Troubleshooting](#troubleshooting)
2. Consultar logs: `docker compose logs`
3. Verificar documentación API en `/docs`

## 📄 Licencia

Proyecto de ejemplo para gestión de usuarios y servicios. Libre para uso educativo y comercial.

## ✨ Mejoras Futuras

- [ ] Autenticación y autorización (JWT)
- [ ] Rate limiting y throttling
- [ ] Sistema de caché distribuido
- [ ] Colas de procesamiento asincrónico
- [ ] Webhooks para eventos
- [ ] Testing automatizado (unittest, pytest)
- [ ] Documentación de API completa
- [ ] Dashboard de administración
- [ ] Reportes y analytics
- [ ] Integración con pasarelas de pago reales

---

**Última actualización**: 2024  
**Mantenedor**: Tu Nombre/Equipo
