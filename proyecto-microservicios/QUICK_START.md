# ⚡ Quick Start - Sistema de Pagos

## 🎯 Resumen Ejecutivo

El sistema de pagos está **100% funcional**. Solo necesitas:

1. **Obtener un Access Token de Mercado Pago** (5 minutos)
2. **Configurarlo en `.env`** (1 minuto)
3. **Reiniciar el servidor** (1 minuto)
4. **Probar desde la app móvil** (5 minutos)

---

## 🚀 Paso 1: Iniciar el Servidor

Abre una terminal y ejecuta:

```bash
cd /Users/pablovilchesvalenzuela/Desktop/Proyecto\ Mobile/proyecto-microservicios/app-pagos
bash run.sh
```

**Deberías ver:**
```
🚀 Iniciando servidor...
   URL: http://localhost:8002
   Health: http://localhost:8002/health

INFO:     Uvicorn running on http://0.0.0.0:8002
INFO:     Application startup complete.
```

---

## 🔑 Paso 2: Obtener Access Token de Mercado Pago

### Opción A: Si tienes cuenta en Mercado Pago
1. Ve a https://www.mercadopago.com.ar/developers/panel
2. Inicia sesión
3. Ve a **Credenciales**
4. Copia el **Access Token** de SANDBOX (importante: SANDBOX, no producción)
5. Pégalo en el archivo `.env`:

```bash
# Edita este archivo:
/Users/pablovilchesvalenzuela/Desktop/Proyecto\ Mobile/proyecto-microservicios/app-pagos/.env

# Cambia esta línea:
MP_ACCESS_TOKEN=APP_USR-1234567890-abcdefghijklmnopqrstuvwxyz
```

### Opción B: Si no tienes cuenta
1. Ve a https://www.mercadopago.com.ar/developers/panel
2. Haz clic en "Crear cuenta"
3. Completa el registro
4. Sigue los pasos de la Opción A

---

## 🔄 Paso 3: Reiniciar el Servidor

En la terminal donde corre el servidor:
1. Presiona **Ctrl+C** para detenerlo
2. Ejecuta nuevamente:
   ```bash
   bash run.sh
   ```

---

## ✅ Paso 4: Verificar que Todo Funciona

En otra terminal, ejecuta:

```bash
curl http://localhost:8002/health | jq .
```

**Deberías ver:**
```json
{
  "status": "healthy",
  "service": "app-pagos",
  "database": "ok",
  "mercadopago_token": "ok"
}
```

Si ves `"mercadopago_token": "ok"`, ¡todo está listo!

---

## 📱 Paso 5: Probar desde la App Móvil

1. **Abre la aplicación móvil** en el emulador
2. **Navega a:** Home → Agregar Tarjeta
3. **Completa el formulario** con estos datos:
   ```
   Número de tarjeta: 4168 8188 4444 7115
   Nombre del titular: APRO
   Mes de vencimiento: 11
   Año de vencimiento: 2030
   CVV: 123
   Email: usuario@email.com
   ```
4. **Presiona:** "Guardar tarjeta"
5. **Observa los logs** en la terminal del servidor

---

## 📊 Monitorear Logs

En la terminal donde corre el servidor, verás logs como:

```
================================================================================
[tarjetas] 🔵 INICIO GUARDAR TARJETA - id_usuario=1
[tarjetas] Email: usuario@email.com
[tarjetas] Token: eyJ0eXAiOiJKV1QiLCJhbGc...
================================================================================
[tarjetas] 📌 Inicializando MercadoPagoService...
[tarjetas] ✅ MercadoPagoService inicializado correctamente
[tarjetas] 📌 Buscando customer en BD para id_usuario=1
[tarjetas] ⚠️  Customer NO existe en BD, creando en Mercado Pago...
[tarjetas] ✅ Customer creado/recuperado: mp_customer_id=123456789
[tarjetas] 📌 Procediendo a guardar tarjeta en Mercado Pago...
[tarjetas] ✅ Tarjeta guardada en Mercado Pago correctamente
[tarjetas] 📌 Insertando tarjeta en BD...
[tarjetas] ✅ Tarjeta guardada en BD: id=1, mp_card_id=abc123
================================================================================
[tarjetas] 🟢 TARJETA GUARDADA EXITOSAMENTE
================================================================================
```

---

## 🧪 Otras Tarjetas de Prueba

Si quieres probar otros escenarios:

### Pago Rechazado
```
Número: 5416 7526 0258 2580
Titular: OTHE
Mes/Año: 11/30
CVV: 123
```

### Pago Pendiente
```
Número: 3757 781744 61804
Titular: CONT
Mes/Año: 11/30
CVV: 1234
```

---

## 🔗 URLs Útiles

- **API:** http://localhost:8002
- **Health Check:** http://localhost:8002/health
- **Swagger UI:** http://localhost:8002/docs
- **ReDoc:** http://localhost:8002/redoc

---

## 🆘 Troubleshooting

### "mercadopago_token": "missing MP_ACCESS_TOKEN"
- Verifica que el archivo `.env` exista
- Verifica que contenga `MP_ACCESS_TOKEN=...`
- Reinicia el servidor

### "Address already in use"
```bash
# Mata el proceso en puerto 8002
lsof -ti:8002 | xargs kill -9

# Reinicia
bash run.sh
```

### "No fue posible conectar con el servidor" (desde app móvil)
- Verifica que el servidor esté corriendo
- Verifica que uses `http://10.0.2.2:8002` desde emulador Android
- Verifica que no haya firewall bloqueando el puerto

---

## 📚 Documentación Completa

Para más detalles, lee:
- **SETUP_COMPLETE.md** - Estado completo del proyecto
- **TESTING_GUIDE.md** - Guía de pruebas detallada
- **MERCADO_PAGO_SETUP.md** - Configuración de Mercado Pago
- **BACKEND_ISSUES.md** - Problemas y soluciones

---

## ✨ ¡Listo!

El sistema está completamente funcional. Solo necesitas el Access Token y podrás:

✅ Guardar tarjetas de crédito  
✅ Listar tarjetas guardadas  
✅ Marcar tarjeta como predeterminada  
✅ Eliminar tarjetas  
✅ Pagar con tarjeta guardada  

¡Que disfrutes! 🚀

