# Configuración de Mercado Pago - Modo Sandbox

## 🔑 Obtener Access Token

### Paso 1: Crear Cuenta en Mercado Pago
1. Ve a https://www.mercadopago.com.ar/developers/panel
2. Inicia sesión o crea una cuenta
3. Selecciona tu aplicación (o crea una nueva)

### Paso 2: Obtener el Access Token
1. En el panel de desarrolladores, ve a **Credenciales**
2. Copia el **Access Token** de SANDBOX (para pruebas)
3. Pégalo en el archivo `.env`:

```bash
MP_ACCESS_TOKEN=APP_USR-1234567890-abcdefghijklmnopqrstuvwxyz
```

### Paso 3: Reiniciar el Servidor
```bash
# Mata el servidor actual
lsof -ti:8002 | xargs kill -9

# Reinicia
cd /Users/pablovilchesvalenzuela/Desktop/Proyecto\ Mobile/proyecto-microservicios/app-pagos
bash run.sh
```

---

## 🧪 Modo Sandbox vs Producción

### Sandbox (Pruebas)
- **URL:** https://sandbox.mercadopago.com.ar
- **Tarjetas de prueba:** Disponibles
- **Dinero real:** No se cobra
- **Recomendado para:** Desarrollo y pruebas

### Producción
- **URL:** https://www.mercadopago.com.ar
- **Tarjetas reales:** Requeridas
- **Dinero real:** Se cobra
- **Recomendado para:** Producción

---

## 💳 Tarjetas de Prueba Sandbox

### Pago Aprobado
```
Número:     4168 8188 4444 7115 (Visa)
Titular:    APRO
Mes/Año:    11/30
CVV:        123
Documento:  123456789
```

### Pago Rechazado - Error General
```
Número:     5416 7526 0258 2580 (Mastercard)
Titular:    OTHE
Mes/Año:    11/30
CVV:        123
Documento:  123456789
```

### Pago Pendiente
```
Número:     3757 781744 61804 (American Express)
Titular:    CONT
Mes/Año:    11/30
CVV:        1234
Documento:  -
```

---

## ✅ Verificar Configuración

### Test 1: Health Check
```bash
curl http://localhost:8002/health | jq .
```

**Esperado:**
```json
{
  "status": "healthy",
  "service": "app-pagos",
  "database": "ok",
  "mercadopago_token": "ok"
}
```

### Test 2: Listar Tarjetas
```bash
curl http://localhost:8002/tarjetas/usuario/1 | jq .
```

**Esperado:**
```json
{
  "success": true,
  "message": "Tarjetas obtenidas correctamente",
  "data": []
}
```

---

## 🔍 Troubleshooting

### Error: "MP_ACCESS_TOKEN no está configurado"
1. Verifica que el archivo `.env` existe
2. Verifica que contiene `MP_ACCESS_TOKEN=...`
3. Reinicia el servidor

### Error: "Oops! Something went wrong..."
1. Verifica que el token sea válido
2. Verifica que estés usando el token de SANDBOX
3. Revisa los logs del servidor para más detalles

### Error: "No fue posible crear customer en Mercado Pago"
1. Verifica la conexión a internet
2. Verifica que el token sea válido
3. Revisa los logs del servidor

---

## 📝 Notas Importantes

- Los tokens de SANDBOX comienzan con `APP_USR-`
- Los tokens de PRODUCCIÓN también comienzan con `APP_USR-` pero son diferentes
- **NUNCA** uses tokens de producción en desarrollo
- Los tokens de prueba son únicos por aplicación
- Los tokens expiran después de 180 días de inactividad

---

## 🎯 Próximos Pasos

1. ✅ Obtener Access Token de SANDBOX
2. ✅ Configurar `.env` con el token
3. ✅ Reiniciar el servidor
4. ✅ Ejecutar health check
5. ✅ Probar guardar tarjeta desde la app móvil

