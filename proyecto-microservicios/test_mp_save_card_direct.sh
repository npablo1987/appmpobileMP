#!/bin/bash

MP_ACCESS_TOKEN="TEST-3532723459071547-040921-9175516fd1e225d32f68ce03f1fbf445-3326014045"
CUSTOMER_ID="3328527972-ktukyvBiIBFh7E"

echo "=========================================="
echo "TEST DIRECTO: GUARDAR TARJETA EN MP"
echo "=========================================="
echo ""

echo "Customer ID: $CUSTOMER_ID"
echo ""

echo "Generando nuevo token de tarjeta Mastercard..."
TOKEN_RESPONSE=$(curl -s -X POST \
  "https://api.mercadopago.com/v1/card_tokens" \
  -H "Authorization: Bearer $MP_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "card_number": "5031755734530604",
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

echo "Respuesta token:"
echo "$TOKEN_RESPONSE" | python3 -m json.tool
echo ""

TOKEN=$(echo "$TOKEN_RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin).get('id', ''))" 2>/dev/null)

if [ -z "$TOKEN" ]; then
  echo "❌ Error generando token"
  exit 1
fi

echo "✅ Token: $TOKEN"
echo ""
echo ""

echo "Guardando tarjeta para customer..."
CARD_RESPONSE=$(curl -s -X POST \
  "https://api.mercadopago.com/v1/customers/$CUSTOMER_ID/cards" \
  -H "Authorization: Bearer $MP_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"token\": \"$TOKEN\"
  }")

echo "Respuesta:"
echo "$CARD_RESPONSE" | python3 -m json.tool
echo ""

echo "=========================================="
