#!/bin/bash

MP_ACCESS_TOKEN="TEST-3532723459071547-040921-9175516fd1e225d32f68ce03f1fbf445-3326014045"

echo "=========================================="
echo "TEST DIRECTO A API DE MERCADO PAGO"
echo "=========================================="
echo ""

echo "TEST 1: Crear customer en Mercado Pago"
echo "----------------------------------------"

CUSTOMER_RESPONSE=$(curl -s -X POST \
  "https://api.mercadopago.com/v1/customers" \
  -H "Authorization: Bearer $MP_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test.user@test.com",
    "first_name": "Test",
    "last_name": "User"
  }')

echo "Respuesta:"
echo "$CUSTOMER_RESPONSE" | python3 -m json.tool
echo ""

# Extraer customer ID
CUSTOMER_ID=$(echo "$CUSTOMER_RESPONSE" | python3 -c "import sys, json; data=json.load(sys.stdin); print(data.get('id', ''))" 2>/dev/null)

if [ -z "$CUSTOMER_ID" ]; then
  echo "❌ No se pudo crear el customer. Intentando buscar customer existente..."
  echo ""
  
  SEARCH_RESPONSE=$(curl -s -X GET \
    "https://api.mercadopago.com/v1/customers/search?email=test.user@test.com" \
    -H "Authorization: Bearer $MP_ACCESS_TOKEN")
  
  echo "Búsqueda de customer:"
  echo "$SEARCH_RESPONSE" | python3 -m json.tool
  echo ""
  
  CUSTOMER_ID=$(echo "$SEARCH_RESPONSE" | python3 -c "import sys, json; data=json.load(sys.stdin); results=data.get('results', []); print(results[0].get('id', '') if results else '')" 2>/dev/null)
fi

if [ -z "$CUSTOMER_ID" ]; then
  echo "❌ Error: No se pudo obtener customer ID"
  exit 1
fi

echo "✅ Customer ID: $CUSTOMER_ID"
echo ""
echo ""

echo "TEST 2: Crear token de tarjeta"
echo "----------------------------------------"

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

echo "Respuesta:"
echo "$TOKEN_RESPONSE" | python3 -m json.tool
echo ""

CARD_TOKEN=$(echo "$TOKEN_RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin).get('id', ''))" 2>/dev/null)

if [ -z "$CARD_TOKEN" ]; then
  echo "❌ Error: No se pudo crear el token"
  exit 1
fi

echo "✅ Token: $CARD_TOKEN"
echo ""
echo ""

echo "TEST 3: Guardar tarjeta para el customer"
echo "----------------------------------------"

CARD_RESPONSE=$(curl -s -X POST \
  "https://api.mercadopago.com/v1/customers/$CUSTOMER_ID/cards" \
  -H "Authorization: Bearer $MP_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"token\": \"$CARD_TOKEN\"
  }")

echo "Respuesta:"
echo "$CARD_RESPONSE" | python3 -m json.tool
echo ""

CARD_ID=$(echo "$CARD_RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin).get('id', ''))" 2>/dev/null)

if [ -z "$CARD_ID" ]; then
  echo "❌ Error: No se pudo guardar la tarjeta"
else
  echo "✅ Tarjeta guardada con ID: $CARD_ID"
fi

echo ""
echo "=========================================="
echo "TEST COMPLETADO"
echo "=========================================="
