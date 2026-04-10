#!/bin/bash

BASE_URL="http://localhost:8002"
MP_ACCESS_TOKEN="TEST-3532723459071547-040921-9175516fd1e225d32f68ce03f1fbf445-3326014045"

echo "=========================================="
echo "TESTS DE ESCENARIOS DE TARJETAS"
echo "=========================================="
echo ""

# Función para generar token
generate_token() {
    local card_number=$1
    local card_name=$2
    
    TOKEN_RESPONSE=$(curl -s -X POST \
      "https://api.mercadopago.com/v1/card_tokens" \
      -H "Authorization: Bearer $MP_ACCESS_TOKEN" \
      -H "Content-Type: application/json" \
      -d "{
        \"card_number\": \"$card_number\",
        \"security_code\": \"123\",
        \"expiration_month\": 11,
        \"expiration_year\": 2030,
        \"cardholder\": {
          \"name\": \"$card_name\",
          \"identification\": {
            \"type\": \"DNI\",
            \"number\": \"12345678\"
          }
        }
      }")
    
    echo "$TOKEN_RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin).get('id', ''))" 2>/dev/null
}

# Función para guardar tarjeta
save_card() {
    local token=$1
    local user_id=$2
    local email=$3
    
    curl -s -X POST \
      "$BASE_URL/tarjetas/guardar" \
      -H "Content-Type: application/json" \
      -d "{
        \"id_usuario\": $user_id,
        \"email\": \"$email\",
        \"token\": \"$token\"
      }"
}

echo "TEST 1: Guardar tarjeta duplicada (misma tarjeta)"
echo "----------------------------------------"
echo "Generando token para tarjeta Visa 4168 8188 4444 7115..."
TOKEN1=$(generate_token "4168818844447115" "APRO")
echo "Token: $TOKEN1"
echo ""

echo "Intentando guardar tarjeta duplicada..."
RESPONSE=$(save_card "$TOKEN1" 1 "test.user@test.com")
echo "Respuesta:"
echo "$RESPONSE" | python3 -m json.tool
echo ""
echo ""

echo "TEST 2: Guardar segunda tarjeta diferente (Mastercard)"
echo "----------------------------------------"
echo "Generando token para tarjeta Mastercard 5031 7557 3453 0604..."
TOKEN2=$(generate_token "5031755734530604" "APRO")
echo "Token: $TOKEN2"
echo ""

echo "Guardando segunda tarjeta..."
RESPONSE=$(save_card "$TOKEN2" 1 "test.user@test.com")
echo "Respuesta:"
echo "$RESPONSE" | python3 -m json.tool
echo ""
echo ""

echo "TEST 3: Listar todas las tarjetas del usuario"
echo "----------------------------------------"
curl -s "$BASE_URL/tarjetas/usuario/1" | python3 -m json.tool
echo ""
echo ""

echo "TEST 4: Guardar tarjeta para otro usuario"
echo "----------------------------------------"
echo "Generando token para tarjeta Visa..."
TOKEN3=$(generate_token "4168818844447115" "APRO")
echo "Token: $TOKEN3"
echo ""

echo "Guardando tarjeta para usuario 2..."
RESPONSE=$(save_card "$TOKEN3" 2 "test.user2@test.com")
echo "Respuesta:"
echo "$RESPONSE" | python3 -m json.tool
echo ""
echo ""

echo "TEST 5: Listar tarjetas del usuario 2"
echo "----------------------------------------"
curl -s "$BASE_URL/tarjetas/usuario/2" | python3 -m json.tool
echo ""
echo ""

echo "=========================================="
echo "TESTS COMPLETADOS"
echo "=========================================="
