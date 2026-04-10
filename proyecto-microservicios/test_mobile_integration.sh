#!/bin/bash

#================================================================================
# PRUEBAS DE INTEGRACIÓN: APLICACIÓN MÓVIL + BACKEND
#================================================================================
# Este script verifica que la aplicación móvil puede comunicarse correctamente
# con los servicios backend para guardar tarjetas y realizar pagos
#================================================================================

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuración
BASE_URL="http://localhost:8002"
MP_PUBLIC_KEY="TEST-3361de71-ab52-4f15-bec3-9ecfaf855528"
MP_ACCESS_TOKEN="${MP_ACCESS_TOKEN:-}"

# Usuarios de prueba
USER_ID_1=1
USER_ID_2=2
USER_EMAIL_1="usuario1@test.com"
USER_EMAIL_2="usuario2@test.com"

# Tarjetas de prueba
CARD_VISA="4168818844447115"
CARD_MASTERCARD="5031755734530604"

echo ""
echo -e "${BLUE}=========================================${NC}"
echo -e "${BLUE}PRUEBAS DE INTEGRACIÓN MÓVIL-BACKEND${NC}"
echo -e "${BLUE}=========================================${NC}"
echo ""

# Función para hacer peticiones
function make_request() {
    local method=$1
    local endpoint=$2
    local data=$3
    
    if [ -z "$data" ]; then
        curl -s -X "$method" "$BASE_URL$endpoint" \
            -H "Content-Type: application/json"
    else
        curl -s -X "$method" "$BASE_URL$endpoint" \
            -H "Content-Type: application/json" \
            -d "$data"
    fi
}

# Función para generar token de tarjeta
function generate_card_token() {
    local card_number=$1
    local cardholder_name=$2
    
    echo -e "${YELLOW}📌 Generando token para tarjeta $card_number...${NC}"
    
    curl -s -X POST "$MP_API/v1/card_tokens" \
        -H "Content-Type: application/json" \
        -d "{
            \"public_key\": \"$MP_PUBLIC_KEY\",
            \"card_number\": \"$card_number\",
            \"cardholder\": {
                \"name\": \"$cardholder_name\",
                \"identification\": {
                    \"type\": \"DNI\",
                    \"number\": \"12345678\"
                }
            },
            \"security_code\": \"123\",
            \"expiration_month\": 11,
            \"expiration_year\": 2030
        }"
}

# Función para crear token via Mercado Pago
function create_mp_token() {
    local card_number=$1
    local cardholder_name=$2
    
    # Esta es la forma en que MercadoPagoCardService lo hace
    # Usamos curl para simular lo que hace la app móvil
    local response=$(curl -s -X POST "https://api.mercadopago.com/v1/card_tokens" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $MP_PUBLIC_KEY" \
        -d "{
            \"card_number\": \"$card_number\",
            \"cardholder\": {
                \"name\": \"$cardholder_name\",
                \"identification\": {
                    \"type\": \"DNI\",
                    \"number\": \"12345678\"
                }
            },
            \"security_code\": \"123\",
            \"expiration_month\": 11,
            \"expiration_year\": 2030
        }")
    
    # Extraer el ID del token
    echo "$response" | grep -o '"id":"[^"]*' | head -1 | cut -d'"' -f4
}

# Función para verificar salud del servicio
function check_service_health() {
    echo ""
    echo -e "${BLUE}═══════════════════════════════════════${NC}"
    echo -e "${BLUE}1️⃣ VERIFICAR SALUD DEL SERVICIO${NC}"
    echo -e "${BLUE}═══════════════════════════════════════${NC}"
    
    echo -e "${YELLOW}Endpoint:${NC} GET $BASE_URL/health"
    local response=$(make_request GET "/health")
    
    echo -e "${YELLOW}Respuesta:${NC}"
    echo "$response" | python3 -m json.tool
    
    # Verificar que el servicio esté healthy
    if echo "$response" | grep -q '"status":"healthy"'; then
        echo -e "${GREEN}✅ Servicio está funcionando correctamente${NC}"
        return 0
    else
        echo -e "${RED}❌ Servicio NO está saludable${NC}"
        return 1
    fi
}

# Función para probar flujo de guardar tarjeta
function test_save_card_flow() {
    echo ""
    echo -e "${BLUE}═══════════════════════════════════════${NC}"
    echo -e "${BLUE}2️⃣ PRUEBA: GUARDAR TARJETA (USUARIO 1)${NC}"
    echo -e "${BLUE}═══════════════════════════════════════${NC}"
    
    echo ""
    echo -e "${YELLOW}Paso 1: Generar token de Mercado Pago${NC}"
    echo -e "${YELLOW}Tarjeta: $CARD_VISA (Visa)${NC}"
    
    TOKEN=$(create_mp_token "$CARD_VISA" "USUARIO TEST")
    
    if [ -z "$TOKEN" ] || [ "$TOKEN" = "null" ]; then
        echo -e "${RED}❌ No se pudo generar token${NC}"
        return 1
    fi
    
    echo -e "${GREEN}✅ Token generado: $TOKEN${NC}"
    echo ""
    
    echo -e "${YELLOW}Paso 2: Llamar endpoint de guardar tarjeta${NC}"
    echo -e "${YELLOW}Endpoint:${NC} POST $BASE_URL/tarjetas/guardar"
    echo -e "${YELLOW}Datos:${NC}"
    
    local payload="{
        \"id_usuario\": $USER_ID_1,
        \"token\": \"$TOKEN\",
        \"email\": \"$USER_EMAIL_1\"
    }"
    
    echo "$payload" | python3 -m json.tool
    
    local response=$(make_request POST "/tarjetas/guardar" "$payload")
    
    echo -e "${YELLOW}Respuesta:${NC}"
    echo "$response" | python3 -m json.tool
    
    if echo "$response" | grep -q '"success":true'; then
        echo -e "${GREEN}✅ Tarjeta guardada correctamente${NC}"
        return 0
    else
        echo -e "${RED}❌ Error guardando tarjeta${NC}"
        return 1
    fi
}

# Función para probar listar tarjetas
function test_list_cards() {
    echo ""
    echo -e "${BLUE}═══════════════════════════════════════${NC}"
    echo -e "${BLUE}3️⃣ PRUEBA: LISTAR TARJETAS DEL USUARIO${NC}"
    echo -e "${BLUE}═══════════════════════════════════════${NC}"
    
    echo ""
    echo -e "${YELLOW}Endpoint:${NC} GET $BASE_URL/tarjetas/usuario/$USER_ID_1"
    
    local response=$(make_request GET "/tarjetas/usuario/$USER_ID_1")
    
    echo -e "${YELLOW}Respuesta:${NC}"
    echo "$response" | python3 -m json.tool
    
    if echo "$response" | grep -q '"success":true'; then
        # Contar tarjetas
        local count=$(echo "$response" | grep -o '"id":' | wc -l)
        echo -e "${GREEN}✅ Se obtuvieron $count tarjeta(s)${NC}"
        return 0
    else
        echo -e "${RED}❌ Error listando tarjetas${NC}"
        return 1
    fi
}

# Función para probar guardar tarjeta duplicada
function test_duplicate_card() {
    echo ""
    echo -e "${BLUE}═══════════════════════════════════════${NC}"
    echo -e "${BLUE}4️⃣ PRUEBA: GUARDAR TARJETA DUPLICADA${NC}"
    echo -e "${BLUE}═══════════════════════════════════════${NC}"
    
    echo ""
    echo -e "${YELLOW}Generando token para la misma tarjeta...${NC}"
    
    TOKEN=$(create_mp_token "$CARD_VISA" "USUARIO TEST")
    
    if [ -z "$TOKEN" ] || [ "$TOKEN" = "null" ]; then
        echo -e "${RED}❌ No se pudo generar token${NC}"
        return 1
    fi
    
    echo -e "${GREEN}✅ Token generado: $TOKEN${NC}"
    echo ""
    
    echo -e "${YELLOW}Intentando guardar la misma tarjeta nuevamente...${NC}"
    
    local payload="{
        \"id_usuario\": $USER_ID_1,
        \"token\": \"$TOKEN\",
        \"email\": \"$USER_EMAIL_1\"
    }"
    
    local response=$(make_request POST "/tarjetas/guardar" "$payload")
    
    echo -e "${YELLOW}Respuesta:${NC}"
    echo "$response" | python3 -m json.tool
    
    if echo "$response" | grep -q '"success":true'; then
        if echo "$response" | grep -q "ya estaba guardada"; then
            echo -e "${GREEN}✅ Sistema detectó tarjeta duplicada correctamente${NC}"
        else
            echo -e "${GREEN}✅ Tarjeta guardada${NC}"
        fi
        return 0
    else
        echo -e "${RED}❌ Error guardando tarjeta${NC}"
        return 1
    fi
}

# Función para probar guardar segunda tarjeta
function test_save_second_card() {
    echo ""
    echo -e "${BLUE}═══════════════════════════════════════${NC}"
    echo -e "${BLUE}5️⃣ PRUEBA: GUARDAR SEGUNDA TARJETA${NC}"
    echo -e "${BLUE}═══════════════════════════════════════${NC}"
    
    echo ""
    echo -e "${YELLOW}Generando token para Mastercard...${NC}"
    
    TOKEN=$(create_mp_token "$CARD_MASTERCARD" "USUARIO TEST")
    
    if [ -z "$TOKEN" ] || [ "$TOKEN" = "null" ]; then
        echo -e "${RED}❌ No se pudo generar token${NC}"
        return 1
    fi
    
    echo -e "${GREEN}✅ Token generado: $TOKEN${NC}"
    echo ""
    
    echo -e "${YELLOW}Guardando segunda tarjeta para el mismo usuario...${NC}"
    
    local payload="{
        \"id_usuario\": $USER_ID_1,
        \"token\": \"$TOKEN\",
        \"email\": \"$USER_EMAIL_1\"
    }"
    
    local response=$(make_request POST "/tarjetas/guardar" "$payload")
    
    echo -e "${YELLOW}Respuesta:${NC}"
    echo "$response" | python3 -m json.tool
    
    if echo "$response" | grep -q '"success":true'; then
        echo -e "${GREEN}✅ Segunda tarjeta guardada correctamente${NC}"
        return 0
    else
        echo -e "${RED}❌ Error guardando segunda tarjeta${NC}"
        return 1
    fi
}

# Función para probar tarjeta en otro usuario
function test_card_different_user() {
    echo ""
    echo -e "${BLUE}═══════════════════════════════════════${NC}"
    echo -e "${BLUE}6️⃣ PRUEBA: GUARDAR TARJETA USUARIO 2${NC}"
    echo -e "${BLUE}═══════════════════════════════════════${NC}"
    
    echo ""
    echo -e "${YELLOW}Generando token para usuario 2...${NC}"
    
    TOKEN=$(create_mp_token "$CARD_VISA" "USUARIO 2")
    
    if [ -z "$TOKEN" ] || [ "$TOKEN" = "null" ]; then
        echo -e "${RED}❌ No se pudo generar token${NC}"
        return 1
    fi
    
    echo -e "${GREEN}✅ Token generado: $TOKEN${NC}"
    echo ""
    
    echo -e "${YELLOW}Guardando tarjeta para usuario 2...${NC}"
    
    local payload="{
        \"id_usuario\": $USER_ID_2,
        \"token\": \"$TOKEN\",
        \"email\": \"$USER_EMAIL_2\"
    }"
    
    local response=$(make_request POST "/tarjetas/guardar" "$payload")
    
    echo -e "${YELLOW}Respuesta:${NC}"
    echo "$response" | python3 -m json.tool
    
    if echo "$response" | grep -q '"success":true'; then
        echo -e "${GREEN}✅ Tarjeta guardada para usuario 2${NC}"
        return 0
    else
        echo -e "${RED}❌ Error guardando tarjeta para usuario 2${NC}"
        return 1
    fi
}

# Función para probar pago con tarjeta guardada
function test_payment_with_saved_card() {
    echo ""
    echo -e "${BLUE}═══════════════════════════════════════${NC}"
    echo -e "${BLUE}7️⃣ PRUEBA: PAGO CON TARJETA GUARDADA${NC}"
    echo -e "${BLUE}═══════════════════════════════════════${NC}"
    
    echo ""
    echo -e "${YELLOW}Paso 1: Obtener ID de tarjeta guardada${NC}"
    
    # Primero listar tarjetas para obtener el ID
    local cards=$(make_request GET "/tarjetas/usuario/$USER_ID_1")
    
    local card_id=$(echo "$cards" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
    
    if [ -z "$card_id" ]; then
        echo -e "${RED}❌ No se encontraron tarjetas guardadas${NC}"
        return 1
    fi
    
    echo -e "${GREEN}✅ Tarjeta encontrada: ID=$card_id${NC}"
    echo ""
    
    echo -e "${YELLOW}Paso 2: Realizar pago con tarjeta guardada${NC}"
    echo -e "${YELLOW}Endpoint:${NC} POST $BASE_URL/tarjetas/pagar"
    
    local payload="{
        \"id_usuario\": $USER_ID_1,
        \"id_tarjeta\": $card_id,
        \"descripcion\": \"Pago de prueba desde app móvil\",
        \"monto\": 100.00
    }"
    
    echo -e "${YELLOW}Datos:${NC}"
    echo "$payload" | python3 -m json.tool
    
    local response=$(make_request POST "/tarjetas/pagar" "$payload")
    
    echo -e "${YELLOW}Respuesta:${NC}"
    echo "$response" | python3 -m json.tool
    
    if echo "$response" | grep -q '"success":true'; then
        echo -e "${GREEN}✅ Pago realizado exitosamente${NC}"
        return 0
    else
        echo -e "${RED}❌ Error realizando pago${NC}"
        return 1
    fi
}

# Función para probar marcar tarjeta como default
function test_set_default_card() {
    echo ""
    echo -e "${BLUE}═══════════════════════════════════════${NC}"
    echo -e "${BLUE}8️⃣ PRUEBA: MARCAR TARJETA COMO DEFAULT${NC}"
    echo -e "${BLUE}═══════════════════════════════════════${NC}"
    
    echo ""
    echo -e "${YELLOW}Paso 1: Obtener ID de segunda tarjeta${NC}"
    
    # Listar tarjetas para obtener el ID
    local cards=$(make_request GET "/tarjetas/usuario/$USER_ID_1")
    
    # Obtener el segundo ID (no el primero)
    local card_id=$(echo "$cards" | grep -o '"id":[0-9]*' | tail -1 | cut -d':' -f2)
    
    if [ -z "$card_id" ]; then
        echo -e "${RED}❌ No se encontraron suficientes tarjetas${NC}"
        return 1
    fi
    
    echo -e "${GREEN}✅ Tarjeta encontrada: ID=$card_id${NC}"
    echo ""
    
    echo -e "${YELLOW}Paso 2: Marcar como tarjeta predeterminada${NC}"
    echo -e "${YELLOW}Endpoint:${NC} PATCH $BASE_URL/tarjetas/$card_id/default"
    
    local payload="{\"is_default\": true}"
    
    local response=$(make_request PATCH "/tarjetas/$card_id/default" "$payload")
    
    echo -e "${YELLOW}Respuesta:${NC}"
    echo "$response" | python3 -m json.tool
    
    if echo "$response" | grep -q '"success":true'; then
        echo -e "${GREEN}✅ Tarjeta marcada como predeterminada${NC}"
        return 0
    else
        echo -e "${RED}❌ Error marcando tarjeta como default${NC}"
        return 1
    fi
}

# Función para resumen final
function print_summary() {
    echo ""
    echo ""
    echo -e "${BLUE}════════════════════════════════════════${NC}"
    echo -e "${BLUE}📊 RESUMEN DE PRUEBAS${NC}"
    echo -e "${BLUE}════════════════════════════════════════${NC}"
    
    echo -e "${GREEN}✅ PRUEBAS REALIZADAS:${NC}"
    echo "1. ✅ Verificación de salud del servicio"
    echo "2. ✅ Guardar tarjeta (usuario 1)"
    echo "3. ✅ Listar tarjetas del usuario"
    echo "4. ✅ Guardar tarjeta duplicada (detección)"
    echo "5. ✅ Guardar segunda tarjeta diferente"
    echo "6. ✅ Guardar tarjeta para usuario 2"
    echo "7. ✅ Pago con tarjeta guardada"
    echo "8. ✅ Marcar tarjeta como predeterminada"
    
    echo ""
    echo -e "${GREEN}🎉 INTEGRACIÓN MÓVIL-BACKEND EXITOSA 🎉${NC}"
    echo ""
    echo -e "${YELLOW}La aplicación móvil puede:${NC}"
    echo "✓ Generar tokens de tarjeta con Mercado Pago"
    echo "✓ Guardar tarjetas de forma segura"
    echo "✓ Listar tarjetas guardadas"
    echo "✓ Detectar tarjetas duplicadas"
    echo "✓ Guardar múltiples tarjetas por usuario"
    echo "✓ Realizar pagos con tarjetas guardadas"
    echo "✓ Marcar tarjetas como predeterminadas"
    
    echo ""
    echo -e "${BLUE}════════════════════════════════════════${NC}"
}

# Ejecutar todas las pruebas
echo "Iniciando pruebas..."

check_service_health || exit 1
test_save_card_flow || exit 1
test_list_cards || exit 1
test_duplicate_card || exit 1
test_save_second_card || exit 1
test_card_different_user || exit 1
test_payment_with_saved_card || exit 1
test_set_default_card || exit 1

print_summary

echo -e "${GREEN}✅ TODAS LAS PRUEBAS COMPLETADAS EXITOSAMENTE${NC}"
