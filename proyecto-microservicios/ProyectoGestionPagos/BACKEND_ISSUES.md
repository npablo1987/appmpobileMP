# Problemas de Backend - Gestión de Tarjetas

## Errores Reportados

### 1. Endpoints de Tarjetas Retornan 404
```
GET /tarjetas/usuario/1 HTTP/1.1" 404 Not Found
POST /tarjetas/guardar HTTP/1.1" 404 Not Found
```

### 2. Error de Serialización JSON
```
JsonConvertException: Illegal input: Fields [success, message] are required for type 
with serial name 'org.example.proyectogestionpagos.data.model.GuardarTarjetaResponse', 
but they were missing at path: $
```

## Causa Raíz

El servidor está retornando una respuesta 404, lo que significa que los endpoints no están siendo encontrados. Esto puede deberse a:

1. **El servidor de pagos no está corriendo** - Verificar que `app-pagos` esté ejecutándose en puerto 8002
2. **Los routers no están registrados** - Aunque el código muestra que están importados en `main.py`
3. **Problema de CORS** - Aunque CORS está habilitado para todos los orígenes

## Verificación del Backend

### Estructura de Archivos Confirmada ✅
- `/app-pagos/app/main.py` - Servidor FastAPI
- `/app-pagos/app/routers/__init__.py` - Importa routers correctamente
- `/app-pagos/app/routers/tarjetas.py` - Endpoints de tarjetas implementados
- `/app-pagos/app/routers/pagos.py` - Endpoints de pagos

### Endpoints Implementados en Backend ✅
```
POST   /tarjetas/guardar              - Guardar nueva tarjeta
GET    /tarjetas/usuario/{id_usuario} - Listar tarjetas del usuario
PATCH  /tarjetas/{id_tarjeta}/default - Marcar como predeterminada
DELETE /tarjetas/{id_tarjeta}         - Eliminar tarjeta
POST   /tarjetas/pagar                - Pagar con tarjeta guardada
```

### Modelos de Datos Confirmados ✅
- `TarjetaGuardarRequest` - Request para guardar tarjeta
- `TarjetaGuardarResponse` - Response con tarjeta guardada
- `TarjetasListResponse` - Response con lista de tarjetas
- `TarjetaDeleteResponse` - Response de eliminación
- `PagoConTarjetaGuardadaRequest` - Request para pago
- `PagoConTarjetaGuardadaResponse` - Response de pago

## Soluciones Recomendadas

### 1. Verificar que el servidor está corriendo
```bash
# En la carpeta del proyecto
cd /Users/pablovilchesvalenzuela/Desktop/Proyecto\ Mobile/proyecto-microservicios/app-pagos

# Ejecutar el servidor
python -m uvicorn app.main:app --host 0.0.0.0 --port 8002 --reload
```

### 2. Verificar la conectividad
```bash
# Desde el emulador Android
curl http://10.0.2.2:8002/health

# Debería retornar:
# {"status":"healthy","service":"app-pagos"}
```

### 3. Verificar los endpoints
```bash
# Listar tarjetas
curl http://10.0.2.2:8002/tarjetas/usuario/1

# Debería retornar:
# {"success":true,"message":"Tarjetas obtenidas correctamente","data":[]}
```

### 4. Revisar logs del servidor
Buscar mensajes como:
```
[tarjetas] Listando tarjetas id_usuario=1
[tarjetas] Guardando tarjeta id_usuario=1
```

## Configuración de URL Base

### En la aplicación móvil
**Archivo:** `Platform.android.kt`

La URL base debe ser:
```kotlin
fun getPaymentsBaseUrl(): String = "http://10.0.2.2:8002"
```

### Verificar en:
- `getPaymentsBaseUrl()` en `Platform.android.kt`
- `getPaymentsBaseUrl()` en `Platform.ios.kt` (si aplica)

## Próximos Pasos

1. **Iniciar el servidor de pagos:**
   ```bash
   cd app-pagos
   python -m uvicorn app.main:app --host 0.0.0.0 --port 8002 --reload
   ```

2. **Verificar que el servidor responde:**
   ```bash
   curl http://10.0.2.2:8002/health
   ```

3. **Ejecutar la aplicación móvil nuevamente**

4. **Monitorear los logs del servidor** para ver si las solicitudes llegan

## Notas Importantes

- El código del cliente está correctamente implementado
- Los endpoints del servidor están correctamente implementados
- El problema es que el servidor no está respondiendo a las solicitudes
- Asegúrate de que el servidor esté corriendo ANTES de ejecutar la aplicación móvil
