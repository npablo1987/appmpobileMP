# 🧪 GUÍA DE PRUEBA DE GUARDADO DE TARJETA

## PASO 1: Ir al directorio del proyecto
```bash
cd /Users/pablovilchesvalenzuela/Desktop/'Proyecto Mobile'/proyecto-microservicios
```

## PASO 2: Verificar que tienes el token de Mercado Pago
Si ya está configurado en tu docker-compose.yml o en .env, continúa al paso 3.
Si no, debes exportar tu token:
```bash
export MP_ACCESS_TOKEN='tu_token_de_acceso_de_mercado_pago'
```

## PASO 3: Iniciar los servicios (si no están corriendo)
```bash
docker-compose up -d postgres app-pagos
```

Espera a que el servicio esté listo. Deberías ver algo como:
```
app-pagos-1  | INFO:     Application startup complete
```

## PASO 4: Verificar que el servicio esté corriendo
```bash
curl http://localhost:8003/health
```

Deberías ver una respuesta JSON con el estado del servicio.

## PASO 5: Ejecutar el test de guardado de tarjeta
```bash
python3 test_guardar_tarjeta.py
```

El script hará lo siguiente:
1. Verificará conectividad con el servicio
2. Verificará que el token de MP esté configurado
3. Generará un token para la tarjeta de prueba Visa
4. Enviará el token a tu API para guardar la tarjeta
5. Verificará que la tarjeta se guardó correctamente en la BD

## ⚠️ INFORMACIÓN DE LA TARJETA DE PRUEBA
- **Número:** 4168 8188 4444 7115 (Visa)
- **CVC:** 123
- **Vencimiento:** 11/30
- **Titular (para aprobación):** APRO
- **Documento:** 123456789

## 📊 OBSERVAR LOS LOGS DEL SERVICIO
Mientras corres el test, puedes ver los logs del contenedor en otra terminal:
```bash
docker-compose logs -f app-pagos
```

Allí verás todos los detalles de qué está pasando en cada paso.

## 🔍 VER LA TARJETA EN LA BASE DE DATOS
Una vez guardada, puedes verificar que está en la BD:

### Opción 1: A través de la API
```bash
curl http://localhost:8003/tarjetas?id_usuario=1
```

### Opción 2: Directamente en PostgreSQL
```bash
docker-compose exec postgres psql -U postgres -d microservicios_db -c "SELECT id, id_usuario, last_four_digits, brand, is_default FROM tarjeta_guardada;"
```

## ❌ SOLUCIONAR PROBLEMAS

### Si el servicio no inicia:
```bash
# Ver logs del servicio
docker-compose logs app-pagos

# Reconstruir el contenedor
docker-compose down
docker-compose build app-pagos
docker-compose up app-pagos
```

### Si el token no es válido:
- Obtén un nuevo token en: https://www.mercadopago.com.ar/developers/panel/credentials
- Asegúrate de usar la versión en modo prueba, no producción

### Si la tarjeta no se guarda:
- Verifica el log del servicio (ver comando arriba)
- Busca mensajes de error que indiquen qué salió mal
- Verifica que el usuario_id exista en la BD o permite creación automática de customers
