#!/bin/bash

# Colores
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}PRUEBAS: PAGO DIRECTO SIN GUARDAR TARJETA${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Base URL
BASE_URL="http://localhost:8002"
MP_PUBLIC_KEY="TEST-3361de71-ab52-4f15-bec3-9ecfaf855528"

# Test 1: Pago directo APROBADO
echo -e "${YELLOW}TEST 1: Pago directo APROBADO${NC}"
echo "Estado esperado: PAGADO"
echo ""

PAGO_RESPONSE=$(curl -s -X POST "$BASE_URL/pagos/directo/procesar" \
  -H "Content-Type: application/json" \
  -d '{
    "id_usuario": 1,
    "numero_tarjeta": "4168818844447115",
    "mes_vencimiento": 11,
    "anio_vencimiento": 2030,
    "cvv": "123",
    "nombre_titular": "APRO",
    "email": "test.user@test.com",
    "descripcion": "Test pago aprobado",
    "monto": 100.00
  }')

echo "$PAGO_RESPONSE" | python3 -m json.tool
ID_PAGO=$(echo "$PAGO_RESPONSE" | grep -o '"id_pago":[0-9]*' | head -1 | cut -d':' -f2)
echo ""

if [ -z "$ID_PAGO" ]; then
  echo -e "${RED}❌ Error: No se obtuvo ID de pago${NC}"
else
  echo -e "${GREEN}✅ Pago creado: ID=$ID_PAGO${NC}"
  echo ""
  
  echo "Consultando estado cada 3 segundos (máx 2 minutos)..."
  ELAPSED=0
  MAX_TIME=120
  
  while [ $ELAPSED -lt $MAX_TIME ]; do
    ESTADO=$(curl -s "$BASE_URL/pagos/$ID_PAGO/estado")
    ESTADO_VALOR=$(echo "$ESTADO" | grep -o '"estado":"[^"]*"' | cut -d':' -f2 | tr -d '"')
    
    echo "[$ELAPSED s] Estado: $ESTADO_VALOR"
    
    if [[ "$ESTADO_VALOR" == "PAGADO" || "$ESTADO_VALOR" == "RECHAZADO" ]]; then
      break
    fi
    
    sleep 3
    ELAPSED=$((ELAPSED + 3))
  done
  
  echo ""
  echo "Estado final:"
  echo "$ESTADO" | python3 -m json.tool
  echo ""
fi

# Test 2: Pago directo RECHAZADO
echo -e "${YELLOW}TEST 2: Pago directo RECHAZADO${NC}"
echo "Estado esperado: RECHAZADO"
echo ""

PAGO_RESPONSE=$(curl -s -X POST "$BASE_URL/pagos/directo/procesar" \
  -H "Content-Type: application/json" \
  -d '{
    "id_usuario": 2,
    "numero_tarjeta": "5416752602582580",
    "mes_vencimiento": 11,
    "anio_vencimiento": 2030,
    "cvv": "123",
    "nombre_titular": "OTHE",
    "email": "test.user2@test.com",
    "descripcion": "Test pago rechazado",
    "monto": 100.00
  }')

echo "$PAGO_RESPONSE" | python3 -m json.tool
ID_PAGO=$(echo "$PAGO_RESPONSE" | grep -o '"id_pago":[0-9]*' | head -1 | cut -d':' -f2)
echo ""

if [ -z "$ID_PAGO" ]; then
  echo -e "${RED}❌ Error: No se obtuvo ID de pago${NC}"
else
  echo -e "${GREEN}✅ Pago creado: ID=$ID_PAGO${NC}"
  echo ""
  
  echo "Consultando estado cada 3 segundos (máx 2 minutos)..."
  ELAPSED=0
  MAX_TIME=120
  
  while [ $ELAPSED -lt $MAX_TIME ]; do
    ESTADO=$(curl -s "$BASE_URL/pagos/$ID_PAGO/estado")
    ESTADO_VALOR=$(echo "$ESTADO" | grep -o '"estado":"[^"]*"' | cut -d':' -f2 | tr -d '"')
    
    echo "[$ELAPSED s] Estado: $ESTADO_VALOR"
    
    if [[ "$ESTADO_VALOR" == "PAGADO" || "$ESTADO_VALOR" == "RECHAZADO" ]]; then
      break
    fi
    
    sleep 3
    ELAPSED=$((ELAPSED + 3))
  done
  
  echo ""
  echo "Estado final:"
  echo "$ESTADO" | python3 -m json.tool
  echo ""
fi

# Test 3: Validación de formulario
echo -e "${YELLOW}TEST 3: Validación - CVV inválido${NC}"
PAGO_RESPONSE=$(curl -s -X POST "$BASE_URL/pagos/directo/procesar" \
  -H "Content-Type: application/json" \
  -d '{
    "id_usuario": 1,
    "numero_tarjeta": "4168818844447115",
    "mes_vencimiento": 11,
    "anio_vencimiento": 2030,
    "cvv": "12",
    "nombre_titular": "TEST",
    "email": "test@test.com",
    "descripcion": "Test",
    "monto": 100.00
  }')

echo "$PAGO_RESPONSE" | python3 -m json.tool
echo ""

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}PRUEBAS COMPLETADAS${NC}"
echo -e "${GREEN}========================================${NC}"
