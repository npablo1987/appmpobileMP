#!/bin/bash

echo "=========================================="
echo "TEST DE GUARDADO DE TARJETA"
echo "=========================================="
echo ""

# Configuración
BASE_URL="http://localhost:8002"
MP_ACCESS_TOKEN="TEST-3532723459071547-040921-9175516fd1e225d32f68ce03f1fbf445-3326014045"
MP_PUBLIC_KEY="TEST-3361de71-ab52-4f15-bec3-9ecfaf855528"

echo "PASO 1: Verificando servicio..."
curl -s "$BASE_URL/health" | python3 -m json.tool
echo ""
echo ""

echo "PASO 2: Generando token de tarjeta de prueba..."
echo "Usando tarjeta de prueba Visa: 4168 8188 4444 7115"
echo ""

# Generar token usando la API de Mercado Pago
TOKEN_RESPONSE=$(curl -s -X POST \
  "https://api.mercadopago.com/v1/card_tokens" \
  -H "Authorization: Bearer $MP_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "card_number": "4168818844447115",
    "security_code": "123",
    "expiration_month": 11,
    "expiration_year": 2030,
    "cardholder": {
      "name": "APRO",
      "identification": {
        "type": "DNI",
        "number": "12345678"
      }
    }
  }')

echo "Respuesta de Mercado Pago:"
echo "$TOKEN_RESPONSE" | python3 -m json.tool
echo ""

# Extraer el token
CARD_TOKEN=$(echo "$TOKEN_RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin).get('id', ''))")

if [ -z "$CARD_TOKEN" ]; then
  echo "❌ Error: No se pudo generar el token de tarjeta"
  exit 1
fi

echo "✅ Token generado: $CARD_TOKEN"
echo ""
echo ""

echo "PASO 3: Guardando tarjeta en el sistema..."
echo "Endpoint: POST $BASE_URL/tarjetas/guardar"
echo ""

SAVE_RESPONSE=$(curl -s -X POST \
  "$BASE_URL/tarjetas/guardar" \
  -H "Content-Type: application/json" \
  -d "{
    \"id_usuario\": 1,
    \"email\": \"test.user@test.com\",
    \"token\": \"$CARD_TOKEN\"
  }")

echo "Respuesta del servidor:"
echo "$SAVE_RESPONSE" | python3 -m json.tool
echo ""
echo ""

echo "PASO 4: Listando tarjetas del usuario..."
echo "Endpoint: GET $BASE_URL/tarjetas/usuario/1"
echo ""

curl -s "$BASE_URL/tarjetas/usuario/1" | python3 -m json.tool
echo ""
echo ""

echo "=========================================="
echo "TEST COMPLETADO"
echo "=========================================="
