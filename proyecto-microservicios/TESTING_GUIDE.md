# Guía de Prueba - Tarjetas de Mercado Pago

## 🚀 Paso 1: Iniciar el Servidor de Pagos

Abre una terminal y ejecuta:

```bash
cd /Users/pablovilchesvalenzuela/Desktop/Proyecto\ Mobile/proyecto-microservicios/app-pagos
bash run.sh
```

**Esperado:** Deberías ver algo como:
```
Iniciando servidor de pagos en puerto 8002...
INFO:     Uvicorn running on http://0.0.0.0:8002
```

---

## 🧪 Paso 2: Verificar que el Servidor Responde

En otra terminal, ejecuta:

```bash
curl http://localhost:8002/health
```

**Esperado:** Respuesta JSON:
```json
{
  "status": "healthy",
  "service": "app-pagos",
  "database": "ok",
  "mercadopago_token": "ok"
}
```

Si ves `"status": "degraded"`, revisa los logs del servidor.

---

## 💳 Paso 3: Tarjetas de Prueba Disponibles

### Pago Aprobado (APRO)
- **Número:** 4168 8188 4444 7115 (Visa)
- **Titular:** APRO
- **Mes/Año:** 11/30
- **CVV:** 123
- **Documento:** 123456789

### Pago Rechazado - Error General (OTHE)
- **Número:** 5416 7526 0258 2580 (Mastercard)
- **Titular:** OTHE
- **Mes/Año:** 11/30
- **CVV:** 123
- **Documento:** 123456789

### Pago Pendiente (CONT)
- **Número:** 3757 781744 61804 (American Express)
- **Titular:** CONT
- **Mes/Año:** 11/30
- **CVV:** 1234

---

## 📱 Paso 4: Probar en la Aplicación Móvil

1. **Abre la aplicación móvil** en el emulador
2. **Navega a:** Home → Agregar Tarjeta
3. **Completa el formulario** con los datos de una tarjeta de prueba
4. **Presiona:** "Guardar tarjeta"

### Monitorear Logs

En la terminal donde corre el servidor, deberías ver:

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

## ✅ Paso 5: Verificar Tarjeta Guardada

En otra terminal, ejecuta:

```bash
curl http://localhost:8002/tarjetas/usuario/1
```

**Esperado:** Respuesta JSON con la tarjeta guardada:
```json
{
  "success": true,
  "message": "Tarjetas obtenidas correctamente",
  "data": [
    {
      "id": 1,
      "id_usuario": 1,
      "mp_customer_id": "123456789",
      "mp_card_id": "abc123",
      "payment_method_id": "visa",
      "brand": "Visa",
      "last_four_digits": "7115",
      "expiration_month": 11,
      "expiration_year": 30,
      "holder_name": "APRO",
      "is_default": true,
      "created_at": "2024-01-15T10:30:00",
      "updated_at": "2024-01-15T10:30:00"
    }
  ]
}
```

---

## 🔍 Troubleshooting

### Error: "No fue posible conectar con el servidor"
- Verifica que el servidor esté corriendo en puerto 8002
- Ejecuta: `curl http://localhost:8002/health`

### Error: "No fue posible generar token"
- Verifica que los datos de la tarjeta sean correctos
- Asegúrate de usar una tarjeta de prueba válida

### Error: "No fue posible guardar tarjeta en Mercado Pago"
- Verifica que `MP_ACCESS_TOKEN` esté configurado en `.env`
- Revisa los logs del servidor para más detalles

### Error 404 en endpoints
- Asegúrate de que el servidor está corriendo
- Verifica que la URL base sea correcta: `http://10.0.2.2:8002` (desde emulador)

---

## 📊 Escenarios de Prueba

| Escenario | Titular | Resultado Esperado |
|-----------|---------|-------------------|
| Pago Aprobado | APRO | ✅ Tarjeta guardada |
| Error General | OTHE | ❌ Rechazado |
| Pago Pendiente | CONT | ⏳ Pendiente |
| Validación PIN | CALL | ❌ Requiere autorización |
| Fondos Insuficientes | FUND | ❌ Fondos insuficientes |
| CVV Inválido | SECU | ❌ CVV inválido |
| Fecha Vencida | EXPI | ❌ Tarjeta vencida |
| Error Formulario | FORM | ❌ Error en formulario |

---

## 🎯 Checklist de Validación

- [ ] Servidor de pagos iniciado correctamente
- [ ] Health check responde con status "healthy"
- [ ] Tarjeta de prueba guardada exitosamente
- [ ] Tarjeta aparece en lista de tarjetas guardadas
- [ ] Logs muestran flujo completo sin errores
- [ ] Aplicación móvil muestra mensaje de éxito
- [ ] Tarjeta se puede marcar como predeterminada
- [ ] Tarjeta se puede eliminar

