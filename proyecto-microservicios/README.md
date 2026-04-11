# Sistema de Gestión de Pagos — Microservicios + App Móvil

![Version](https://img.shields.io/badge/version-2.0.0-blue)
![Python](https://img.shields.io/badge/python-3.11+-green)
![Kotlin](https://img.shields.io/badge/kotlin-multiplatform-purple)
![Docker](https://img.shields.io/badge/docker-required-blue)
![MercadoPago](https://img.shields.io/badge/MercadoPago-API%20v1-009ee3)

Sistema completo de gestión de pagos con arquitectura de microservicios en Python/FastAPI y aplicación móvil multiplataforma en Kotlin Multiplatform (Android + iOS). Integra MercadoPago como pasarela de pagos principal, soportando pago directo con tarjeta, pago a través del navegador (checkout externo) y almacenamiento seguro de tarjetas para cobros futuros.

---

## Tabla de Contenidos

- [Arquitectura General](#arquitectura-general)
- [Microservicios](#microservicios)
- [Integración con MercadoPago](#integración-con-mercadopago)
- [Aplicación Móvil](#aplicación-móvil)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Requisitos Previos](#requisitos-previos)
- [Variables de Entorno](#variables-de-entorno)
- [Instalación y Ejecución](#instalación-y-ejecución)
- [Documentación API](#documentación-api)
- [Endpoints de Pagos](#endpoints-de-pagos)
- [Base de Datos](#base-de-datos)
- [Troubleshooting](#troubleshooting)
- [Tecnologías](#tecnologías)
- [Seguridad](#seguridad)

---

## Arquitectura General

```
┌──────────────────────────────────────────────────────────────────────┐
│               APP MÓVIL (Android / iOS)                              │
│           Kotlin Multiplatform Compose                                │
└────────────┬──────────────────────────────┬─────────────────────────┘
             │                              │
     ┌───────▼──────┐              ┌────────▼───────┐
     │  App Core    │              │  App Pagos     │          App Notificaciones
     │  :8001       │              │  :8002         │               :8003
     │  Usuarios    │              │  Pagos         │         Notificaciones
     │  Planes      │              │  Tarjetas      │         Email / SMS
     │  Servicios   │              │  MercadoPago   │
     │  Facturas    │              │                │
     └───────┬──────┘              └────────┬───────┘
             │                              │
             └──────────────┬───────────────┘
                            │
               ┌────────────▼──────────────┐
               │     PostgreSQL 15          │
               │   (Base de Datos)          │
               └───────────────────────────┘
                            │
               ┌────────────▼──────────────┐
               │     MercadoPago API v1     │
               │  api.mercadopago.com/v1    │
               └───────────────────────────┘
```

### Microservicios

| Servicio              | Puerto | Responsabilidad                                                  |
|-----------------------|--------|------------------------------------------------------------------|
| **app-core**          | 8001   | Usuarios, planes, servicios, suscripciones, facturas, métodos de pago |
| **app-pagos**         | 8002   | Procesamiento de pagos con MercadoPago, tarjetas guardadas       |
| **app-notificaciones**| 8003   | Notificaciones internas, email y SMS                             |

---

## Integración con MercadoPago

El microservicio `app-pagos` implementa tres flujos de pago independientes usando la API REST de MercadoPago v1. Todos los flujos utilizan el `MP_ACCESS_TOKEN` configurado en el entorno.

### Flujo 1 — Pago Directo con Tarjeta

Permite al usuario ingresar los datos de su tarjeta directamente en la app. El backend tokeniza la tarjeta de forma segura y procesa el cobro sin exponer los datos en el cliente.

```
App Móvil                   app-pagos                       MercadoPago API
   │                             │                                 │
   │  POST /pagos/directo/       │                                 │
   │  procesar                   │                                 │
   │  {numero_tarjeta, cvv,      │                                 │
   │   vencimiento, email,       │                                 │
   │   monto, descripcion}       │                                 │
   │────────────────────────────►│                                 │
   │                             │  POST /v1/card_tokens           │
   │                             │  {card_number, expiration,      │
   │                             │   security_code, cardholder}   │
   │                             │────────────────────────────────►│
   │                             │◄────────────────────────────────│
   │                             │  {id: "card_token_xxx"}         │
   │                             │                                 │
   │                             │  POST /v1/payments              │
   │                             │  {token, amount, email,         │
   │                             │   installments, idempotency}    │
   │                             │────────────────────────────────►│
   │                             │◄────────────────────────────────│
   │                             │  {id, status: "approved",       │
   │                             │   status_detail: ...}           │
   │                             │                                 │
   │◄────────────────────────────│                                 │
   │  {id_pago, estado, mp_id,   │                                 │
   │   external_reference}       │                                 │
```

**Detalles técnicos:**
- La tarjeta nunca viaja a terceros directamente; se tokeniza servidor-a-servidor (`POST /v1/card_tokens`) usando el `Bearer MP_ACCESS_TOKEN`.
- Se genera un `X-Idempotency-Key` único (UUID v4) por transacción para evitar cobros duplicados.
- MP infiere el `payment_method_id` automáticamente desde el token. No se envía `currency_id` para pagos en CLP.
- El estado retornado por MP (`approved`, `in_process`, `rejected`) se mapea a los estados internos `PAGADO`, `PENDIENTE`, `RECHAZADO`.
- Si el pago queda `PAGADO`, se persiste inmediatamente en la base de datos.

---

### Flujo 2 — Checkout desde Navegador (Preference)

Redirige al usuario al checkout hosted de MercadoPago. Útil cuando no se quiere procesar datos de tarjeta en el servidor.

```
App Móvil                   app-pagos                       MercadoPago API
   │                             │                                 │
   │  POST /pagos/crear          │                                 │
   │  {id_usuario, monto,        │                                 │
   │   descripcion, email}       │                                 │
   │────────────────────────────►│                                 │
   │                             │  POST /checkout/preferences     │
   │                             │  {items, payer, back_urls,      │
   │                             │   notification_url,             │
   │                             │   external_reference}           │
   │                             │────────────────────────────────►│
   │                             │◄────────────────────────────────│
   │                             │  {id, init_point: "https://..."}│
   │                             │                                 │
   │◄────────────────────────────│                                 │
   │  {id_pago, url_pago,        │                                 │
   │   preference_id,            │                                 │
   │   external_reference}       │                                 │
   │                             │                                 │
   │  [usuario paga en browser]  │                                 │
   │                             │◄── POST /pagos/webhook ────────-│
   │                             │    {type: "payment",            │
   │                             │     data.id: "mp_payment_id"}   │
   │                             │                                 │
   │  GET /pagos/{id}/estado     │  GET /v1/payments/{id}          │
   │  [polling desde app]        │  [consulta estado en MP]        │
   │◄────────────────────────────│────────────────────────────────►│
   │  {estado: "PAGADO"}         │                                 │
```

**Detalles técnicos:**
- Se crea una preferencia con `back_urls` para `success`, `failure` y `pending`.
- El campo `auto_return: "approved"` redirige automáticamente al usuario tras un pago aprobado.
- La operación se registra en memoria con estado `PENDIENTE` y un `external_reference` único.
- El webhook de MercadoPago actualiza el estado y persiste el pago en BD si es aprobado.
- La app móvil puede consultar el estado vía `GET /pagos/{id}/estado` (polling) o esperar la notificación del webhook.
- Las operaciones tienen un timeout configurable de **120 segundos**; pasado ese tiempo pasan a estado `EXPIRADO`.

---

### Flujo 3 — Guardar Tarjeta

Asocia una tarjeta de crédito/débito a un Customer de MercadoPago para cobros futuros sin reingreso de datos.

```
App Móvil                   app-pagos                       MercadoPago API
   │                             │                                 │
   │  POST /tarjetas/guardar     │                                 │
   │  {id_usuario, email, token} │                                 │
   │────────────────────────────►│                                 │
   │                             │  [¿Existe customer en BD?]      │
   │                             │  No →                           │
   │                             │  POST /v1/customers             │
   │                             │  {email}                        │
   │                             │────────────────────────────────►│
   │                             │◄────────────────────────────────│
   │                             │  {id: "mp_customer_id"}         │
   │                             │  [guarda en BD local]           │
   │                             │                                 │
   │                             │  POST /v1/customers/{id}/cards  │
   │                             │  {token: "card_token_xxx"}      │
   │                             │────────────────────────────────►│
   │                             │◄────────────────────────────────│
   │                             │  {id, last_four_digits,         │
   │                             │   expiration_month/year,        │
   │                             │   payment_method, cardholder}   │
   │                             │  [guarda en BD local]           │
   │                             │                                 │
   │◄────────────────────────────│                                 │
   │  {id, brand, last_4,        │                                 │
   │   expiration, is_default}   │                                 │
```

**Detalles técnicos:**
- El `token` de tarjeta debe ser generado previamente en el frontend usando el SDK de MercadoPago (no se envían datos PAN en crudo).
- Si el usuario no tiene un Customer en MP, se crea automáticamente y se persiste el `mp_customer_id` en BD.
- Si el Customer ya existe (error 400 código 101), se recupera mediante búsqueda por email.
- Si la tarjeta ya está asociada al Customer (ya existe), se retorna la tarjeta existente sin duplicar.
- La primera tarjeta guardada se marca automáticamente como `is_default = true`.
- Los datos almacenados localmente son: `mp_card_id`, `payment_method_id`, `brand`, `last_four_digits`, `expiration_month`, `expiration_year`, `holder_name`.

---

### Flujo 4 — Pago con Tarjeta Guardada

Ejecuta un cobro utilizando una tarjeta previamente guardada, sin necesidad de reingresar datos.

```
App Móvil                   app-pagos                       MercadoPago API
   │                             │                                 │
   │  POST /tarjetas/pagar       │                                 │
   │  {id_usuario, id_tarjeta,   │                                 │
   │   monto, descripcion}       │                                 │
   │────────────────────────────►│                                 │
   │                             │  [valida tarjeta y ownership]   │
   │                             │  [obtiene mp_customer_id]       │
   │                             │                                 │
   │                             │  POST /v1/payments              │
   │                             │  {token: mp_card_id,            │
   │                             │   transaction_amount,           │
   │                             │   payment_method_id,            │
   │                             │   payer.id: mp_customer_id,     │
   │                             │   installments: 1}              │
   │                             │────────────────────────────────►│
   │                             │◄────────────────────────────────│
   │                             │  {id, status, status_detail}    │
   │                             │  [persiste en BD]               │
   │                             │                                 │
   │◄────────────────────────────│                                 │
   │  {id_pago, mp_payment_id,   │                                 │
   │   status, status_detail,    │                                 │
   │   external_reference}       │                                 │
```

**Detalles técnicos:**
- Se valida que la tarjeta pertenezca al usuario que realiza el pago (ownership check).
- El pago se procesa en **1 cuota** (`installments: 1`).
- El resultado se persiste directamente en la BD con el estado mapeado desde MercadoPago.

---

### Flujo 5 — Webhook de MercadoPago

MercadoPago notifica eventos de pago al backend mediante webhooks HTTP.

```
MercadoPago API                  app-pagos (POST /pagos/webhook)
       │                                       │
       │  POST /pagos/webhook                  │
       │  ?type=payment&data.id=12345          │
       │──────────────────────────────────────►│
       │                                       │  [GET /v1/payments/12345]
       │                                       │  consulta estado real en MP
       │◄──────────────────────────────────────│
       │  {status: "approved",                 │
       │   external_reference: "ref_xxx"}      │
       │                                       │
       │                                       │  [busca operacion por external_reference]
       │                                       │  [actualiza estado en memoria y en BD]
       │                                       │  [persiste pago si estado == "approved"]
       │                                       │
       │◄──────────────────────────────────────│
       │  HTTP 200 {success: true}             │
```

**Detalles técnicos:**
- El webhook acepta tanto parámetros por querystring como en el body JSON.
- Los eventos que no son de tipo `payment` son ignorados con respuesta 200.
- Se busca la operación en memoria por `external_reference` y se sincroniza con BD.
- Si ya existe en BD, se actualiza el `estado_pago` y el `codigo_transaccion` (ID de pago en MP).

---

### Cancelación de Pagos

Los pagos pendientes pueden cancelarse desde la app:

```
POST /pagos/{id_pago}/cancelar
```

- Si el pago tiene un `mp_payment_id`, se intenta cancelar también en MercadoPago vía `PUT /v1/payments/{id}`.
- Si el pago ya está en estado final (`PAGADO`, `RECHAZADO`, `EXPIRADO`, `CANCELADO`), se retorna el estado actual sin modificar.

---

### Estados de Pago

| Estado Interno | Estado MercadoPago | Descripción                         |
|----------------|--------------------|-------------------------------------|
| `PAGADO`       | `approved`         | Pago aprobado y cobrado             |
| `PENDIENTE`    | `in_process`       | En revisión o esperando acción      |
| `RECHAZADO`    | `rejected`         | Pago rechazado por el emisor        |
| `CANCELADO`    | `cancelled`        | Cancelado por el usuario            |
| `EXPIRADO`     | —                  | Timeout interno (120 segundos)      |
| `ANULADO`      | —                  | Anulado manualmente                 |

---

## Aplicación Móvil

Proyecto en **Kotlin Multiplatform Compose** ubicado en `ProyectoGestionPagos/`. Comparte código de UI y lógica de negocio entre Android e iOS.

### Pantallas de Pago

| Pantalla                  | Descripción                                                       |
|---------------------------|-------------------------------------------------------------------|
| `PaymentScreen`           | Selección del método de pago (directo, browser, tarjeta guardada) |
| `PaymentDirectScreen`     | Ingreso manual de datos de tarjeta para pago directo              |
| `AddCardScreen`           | Tokenización y guardado de nueva tarjeta                          |
| `SavedCardsScreen`        | Lista de tarjetas guardadas del usuario                           |
| `PayWithSavedCardScreen`  | Confirmar pago con tarjeta ya guardada                            |
| `PaymentSuccessScreen`    | Confirmación visual del pago aprobado                             |

### ViewModels

- `PaymentFlowViewModel` — Orquesta los flujos de pago directos y por browser. Gestiona polling de estado y timeouts.
- `CardViewModel` — Gestiona guardar, listar y eliminar tarjetas. Interactúa con `/tarjetas/*`.

---

## Estructura del Proyecto

```
proyecto-microservicios/
│
├── .env                         # Variables de entorno (NO versionar)
├── docker-compose.yml           # Orquestación de contenedores
├── README.md
│
├── db/
│   ├── init.sql                 # Inicialización de esquema PostgreSQL
│   └── migrations/
│       └── 002_tarjetas_guardadas.sql
│
├── app-core/                    # Microservicio principal (Puerto 8001)
│   ├── app/
│   │   ├── main.py
│   │   ├── database.py
│   │   ├── models/              # Modelos SQLAlchemy
│   │   ├── schemas/             # Esquemas Pydantic
│   │   ├── routers/             # Endpoints por recurso
│   │   ├── crud/                # Operaciones de base de datos
│   │   └── services/
│   ├── Dockerfile
│   └── requirements.txt
│
├── app-pagos/                   # Microservicio de pagos (Puerto 8002)
│   ├── app/
│   │   ├── main.py
│   │   ├── database.py
│   │   ├── models/
│   │   │   ├── pago.py
│   │   │   ├── tarjeta_guardada.py
│   │   │   └── mp_customer.py
│   │   ├── schemas/
│   │   │   ├── pago.py
│   │   │   └── tarjeta.py
│   │   ├── routers/
│   │   │   ├── pagos.py         # Flujos de pago (directo, checkout, webhook)
│   │   │   └── tarjetas.py      # CRUD de tarjetas guardadas
│   │   ├── crud/
│   │   │   ├── pago.py
│   │   │   ├── tarjeta_guardada.py
│   │   │   └── mp_customer.py
│   │   └── services/
│   │       └── mercadopago_service.py  # Integración con la API de MercadoPago
│   ├── Dockerfile
│   └── requirements.txt
│
├── app-notificaciones/          # Microservicio de notificaciones (Puerto 8003)
│   ├── app/
│   │   ├── main.py
│   │   ├── database.py
│   │   ├── models/
│   │   ├── schemas/
│   │   ├── routers/
│   │   ├── crud/
│   │   └── services/
│   ├── Dockerfile
│   └── requirements.txt
│
└── ProyectoGestionPagos/        # App móvil KMP (Android + iOS)
    ├── composeApp/
    │   └── src/
    │       ├── commonMain/      # Código compartido (UI + ViewModels + Network)
    │       ├── androidMain/     # Implementaciones específicas Android
    │       └── iosMain/         # Implementaciones específicas iOS
    ├── iosApp/                  # Punto de entrada iOS
    ├── build.gradle.kts
    └── settings.gradle.kts
```

---

## Requisitos Previos

| Software       | Versión mínima | Notas                                      |
|----------------|----------------|--------------------------------------------|
| Docker         | 20.10          | [Docker Desktop](https://www.docker.com/)  |
| Docker Compose | 2.0            | Incluido en Docker Desktop                 |
| Android Studio | Koala+         | Para compilar la app Android/iOS           |
| JDK            | 17             | Requerido por Kotlin Multiplatform         |

```bash
# Verificar instalaciones
docker --version
docker compose version
```

---

## Variables de Entorno

Crear el archivo `.env` en la raíz de `proyecto-microservicios/` con el siguiente contenido:

```env
# ─── PostgreSQL ──────────────────────────────────────────────────────────────
POSTGRES_DB=gestion_usuarios_servicios
POSTGRES_USER=postgres
POSTGRES_PASSWORD=tu_password_seguro

# ─── Conexión a BD desde microservicios ──────────────────────────────────────
DB_HOST=postgres-db
DB_PORT=5432
DB_NAME=gestion_usuarios_servicios
DB_USER=postgres
DB_PASSWORD=tu_password_seguro

# ─── MercadoPago ─────────────────────────────────────────────────────────────
# Obtener en: https://www.mercadopago.com/developers/panel/credentials
MP_ACCESS_TOKEN=APP_USR-xxxxxxxxxxxxxxxxxxxx   # Access Token de TEST o PROD
MP_PUBLIC_KEY=APP_USR-xxxxxxxxxxxxxxxxxxxx      # Public Key (usada en el frontend)

# URLs de retorno después del checkout
MP_SUCCESS_URL=https://tudominio.com/pago/exitoso
MP_FAILURE_URL=https://tudominio.com/pago/fallido
MP_PENDING_URL=https://tudominio.com/pago/pendiente

# URL pública donde MercadoPago enviará los webhooks
# Debe ser accesible desde internet (usar ngrok en desarrollo)
MP_WEBHOOK_URL=https://tudominio.com/pagos/webhook
```

> **Credenciales de prueba**: Obtén tu `ACCESS_TOKEN` y `PUBLIC_KEY` de sandbox en el
> [Panel de Desarrolladores de MercadoPago](https://www.mercadopago.com/developers/panel/credentials).
> Las credenciales de TEST comienzan con `TEST-` y las de producción con `APP_USR-`.

---

## Instalación y Ejecución

### 1. Clonar el repositorio

```bash
git clone <repositorio>
cd proyecto-microservicios
```

### 2. Configurar variables de entorno

```bash
cp .env.example .env
# Editar .env con tus credenciales de MercadoPago y base de datos
```

### 3. Levantar los servicios

```bash
# Construir e iniciar en background
docker compose up -d --build

# Ver logs en tiempo real
docker compose logs -f

# Ver solo el microservicio de pagos
docker compose logs -f app-pagos
```

### 4. Verificar que los servicios estén activos

```bash
curl http://localhost:8001/health   # App Core
curl http://localhost:8002/health   # App Pagos
curl http://localhost:8003/health   # App Notificaciones
```

### 5. Webhook local (desarrollo)

Para recibir webhooks de MercadoPago en local, usar ngrok:

```bash
ngrok http 8002
# Copiar la URL https://xxxx.ngrok.io y asignarla a MP_WEBHOOK_URL en .env
# Reiniciar: docker compose up -d
```

---

## Documentación API

| Servicio           | Swagger UI                      | ReDoc                          |
|--------------------|---------------------------------|--------------------------------|
| App Core           | http://localhost:8001/docs      | http://localhost:8001/redoc    |
| App Pagos          | http://localhost:8002/docs      | http://localhost:8002/redoc    |
| App Notificaciones | http://localhost:8003/docs      | http://localhost:8003/redoc    |

---

## Endpoints de Pagos

### Pago Directo con Tarjeta

```
POST /pagos/directo/procesar
```
```json
{
  "id_usuario": 1,
  "numero_tarjeta": "4111111111111111",
  "mes_vencimiento": 11,
  "anio_vencimiento": 2030,
  "cvv": "123",
  "nombre_titular": "JUAN PEREZ",
  "email": "juan@example.com",
  "descripcion": "Suscripción mensual Plan Pro",
  "monto": 9990
}
```

### Crear Pago por Checkout (Browser)

```
POST /pagos/crear
```
```json
{
  "id_usuario": 1,
  "email_pagador": "juan@example.com",
  "descripcion": "Suscripción mensual Plan Pro",
  "monto": 9990
}
```

### Consultar Estado de Pago

```
GET /pagos/{id_pago}/estado
```

### Cancelar Pago

```
POST /pagos/{id_pago}/cancelar
```

### Guardar Tarjeta

```
POST /tarjetas/guardar
```
```json
{
  "id_usuario": 1,
  "email": "juan@example.com",
  "token": "card_token_generado_por_sdk_mp"
}
```

### Listar Tarjetas Guardadas

```
GET /tarjetas/usuario/{id_usuario}
```

### Pagar con Tarjeta Guardada

```
POST /tarjetas/pagar
```
```json
{
  "id_usuario": 1,
  "id_tarjeta": 3,
  "monto": 9990,
  "descripcion": "Suscripción mensual Plan Pro"
}
```

### Marcar Tarjeta como Default

```
PATCH /tarjetas/{id_tarjeta}/default
```

### Eliminar Tarjeta Guardada

```
DELETE /tarjetas/{id_tarjeta}?id_usuario={id_usuario}
```

### Webhook (uso interno — llamado por MercadoPago)

```
POST /pagos/webhook
```

---

## Base de Datos

### Tablas Principales

| Tabla               | Descripción                                                    |
|---------------------|----------------------------------------------------------------|
| `usuario`           | Usuarios registrados                                           |
| `plan_mensual`      | Planes de suscripción                                          |
| `servicio`          | Catálogo de servicios                                          |
| `metodo_pago`       | Métodos de pago genéricos (interno)                            |
| `suscripcion`       | Suscripciones activas                                          |
| `usuario_servicio`  | Servicios contratados por usuario                              |
| `pago`              | Registro de pagos procesados vía MercadoPago                   |
| `factura`           | Facturas generadas                                             |
| `detalle_factura`   | Líneas de detalle de cada factura                              |
| `mp_customer`       | Relación usuario ↔ Customer ID de MercadoPago                 |
| `tarjeta_guardada`  | Tarjetas tokenizadas asociadas a Customers de MercadoPago      |
| `notificacion`      | Notificaciones emitidas                                        |

### Conectarse a la Base de Datos

```bash
# Vía Docker Compose
docker compose exec postgres-db psql -U postgres -d gestion_usuarios_servicios

# Vía psql local
psql -h localhost -U postgres -d gestion_usuarios_servicios
```

---

## Troubleshooting

### Error: `MP_ACCESS_TOKEN no está configurado`

Verifica que el archivo `.env` existe en la raíz y contiene la variable `MP_ACCESS_TOKEN`.

```bash
docker compose exec app-pagos env | grep MP_ACCESS_TOKEN
```

### Error 400 al crear pago: `invalid_token` / `bad_request`

El token de tarjeta es de un solo uso y expira en minutos. Genera un nuevo token desde el SDK de MercadoPago antes de cada pago directo.

### Error 400: Customer ya existe en MercadoPago

El servicio lo maneja automáticamente. Si `create_customer` retorna error 400 con código 101 ("already exists"), se recupera el customer existente por email via `search_customer_by_email`.

### Los webhooks no llegan

1. Verifica que `MP_WEBHOOK_URL` apunte a una URL públicamente accesible.
2. Usa `ngrok http 8002` en desarrollo para exponer el servicio local.
3. Registra la URL en el Panel de Desarrolladores de MercadoPago.

### Contenedores no inician

```bash
docker compose down -v
docker compose up --build
```

### Puerto 8001/8002/8003 en uso

```bash
lsof -i :8002        # macOS/Linux
netstat -ano | findstr :8002  # Windows
```

---

## Tecnologías

### Backend

| Tecnología     | Uso                                              |
|----------------|--------------------------------------------------|
| Python 3.11+   | Lenguaje principal                               |
| FastAPI        | Framework web async (REST + WebSocket)           |
| SQLAlchemy 2.0 | ORM para PostgreSQL                              |
| Pydantic 2.0   | Validación y serialización de datos              |
| Uvicorn        | Servidor ASGI                                    |
| mercadopago SDK| Cliente oficial de la API de MercadoPago         |
| requests       | Llamadas HTTP directas a la API REST de MP       |
| python-dotenv  | Carga de variables de entorno                    |

### Infraestructura

| Tecnología      | Uso                        |
|-----------------|----------------------------|
| PostgreSQL 15   | Base de datos relacional   |
| Docker          | Contenedorización          |
| Docker Compose  | Orquestación local         |

### App Móvil

| Tecnología                   | Uso                                     |
|------------------------------|-----------------------------------------|
| Kotlin Multiplatform (KMP)   | Código compartido Android + iOS         |
| Compose Multiplatform        | UI declarativa multiplataforma          |
| Ktor Client                  | Comunicación HTTP con los microservicios|

---

## Seguridad

- Los `ACCESS_TOKEN` y `PUBLIC_KEY` de MercadoPago **nunca deben commitearse** al repositorio. Usar siempre el archivo `.env` (incluido en `.gitignore`).
- Los datos PAN de tarjeta no se almacenan. El backend solo guarda el `mp_card_id` y los últimos 4 dígitos.
- Cada transacción de pago directo usa una `X-Idempotency-Key` única para prevenir cobros duplicados.
- Los endpoints de tarjetas validan que el `id_usuario` sea el propietario antes de cualquier operación.
- Para producción: implementar autenticación JWT, HTTPS/TLS y rate limiting sobre los endpoints de pagos.

---

## Comandos Útiles

```bash
# Levantar servicios
docker compose up -d

# Ver estado
docker compose ps

# Ver logs de un servicio específico
docker compose logs -f app-pagos

# Reconstruir imagen de un servicio
docker compose build --no-cache app-pagos

# Reiniciar un servicio
docker compose restart app-pagos

# Detener y eliminar todo (incluidos volúmenes)
docker compose down -v

# Ejecutar comando dentro de un contenedor
docker compose exec app-pagos bash
```

---

**Última actualización**: Abril 2026
