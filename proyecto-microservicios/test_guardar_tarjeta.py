#!/usr/bin/env python3
"""
Script para probar el guardado de tarjeta con los datos de prueba de Mercado Pago
Tarjeta Visa de prueba: 4168 8188 4444 7115
Estado de pago: APRO (Aprobado)
"""

import os
import sys
import requests
import json
from datetime import datetime

# Configuración
BASE_URL = os.getenv("API_BASE_URL", "http://localhost:8002")
MP_ACCESS_TOKEN = os.getenv("MP_ACCESS_TOKEN", "")

print(f"\n{'='*80}")
print(f"🔵 TEST DE GUARDADO DE TARJETA - {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
print(f"{'='*80}\n")

# Step 1: Verificar conectividad
print("PASO 1: Verificando conectividad con el servicio de pagos...")
print(f"URL: {BASE_URL}\n")

try:
    health_response = requests.get(f"{BASE_URL}/health")
    print(f"✅ Status Code: {health_response.status_code}")
    print(f"📋 Respuesta:\n{json.dumps(health_response.json(), indent=2)}\n")
except Exception as e:
    print(f"❌ Error conectando: {e}")
    print(f"⚠️  Asegúrate de que el servicio esté corriendo:\n")
    print(f"   cd /Users/pablovilchesvalenzuela/Desktop/'Proyecto Mobile'/proyecto-microservicios")
    print(f"   docker-compose up app-pagos\n")
    sys.exit(1)

# Step 2: Verificar token de MP
print("PASO 2: Verificando configuración de Mercado Pago...")
if not MP_ACCESS_TOKEN:
    print(f"❌ Variable MP_ACCESS_TOKEN no está configurada")
    print(f"⚠️  Necesitas exportar tu token de Mercado Pago:\n")
    print(f"   export MP_ACCESS_TOKEN='tu_token_aqui'\n")
    sys.exit(1)
print(f"✅ Token de Mercado Pago configurado: {MP_ACCESS_TOKEN[:20]}...\n")

# Step 3: Generar token de tarjeta
print("PASO 3: Generando token de tarjeta con datos de prueba...")
print("Datos de la tarjeta:")
print("  - Número: 4168 8188 4444 7115 (Visa)")
print("  - Código de seguridad: 123")
print("  - Fecha de vencimiento: 11/30")
print("  - Titular: APRO (Pago Aprobado)")
print("  - Documento: 123456789\n")

import mercadopago

try:
    mp = mercadopago.SDK(MP_ACCESS_TOKEN)
    
    # Crear token
    card_token_data = {
        "cardNumber": "4168818844447115",
        "securityCode": "123",
        "expirationMonth": 11,
        "expirationYear": 30,
        "cardholder": {
            "name": "APRO",
            "identification": {
                "type": "DNI",
                "number": "123456789"
            }
        }
    }
    
    print("📌 Enviando datos de la tarjeta a Mercado Pago...")
    token_response = mp.card_token().create(card_token_data)
    
    print(f"Status: {token_response.get('status')}")
    
    if token_response.get('status') in (200, 201):
        token = token_response.get('response', {}).get('id')
        print(f"✅ Token generado exitosamente: {token}\n")
    else:
        print(f"❌ Error generando token:")
        print(json.dumps(token_response, indent=2))
        sys.exit(1)
        
except Exception as e:
    print(f"❌ Error: {e}")
    import traceback
    traceback.print_exc()
    sys.exit(1)

# Step 4: Guardar tarjeta en nuestro sistema
print("PASO 4: Guardando tarjeta en nuestro sistema...")
print(f"Endpoint: POST {BASE_URL}/tarjetas/guardar\n")

# Datos del usuario (CAMBIAR ESTOS VALORES SI ES NECESARIO)
usuario_id = 1
email = "test.apro@mercadopago.com"

payload = {
    "id_usuario": usuario_id,
    "email": email,
    "token": token
}

print(f"Payload:")
print(json.dumps({
    "id_usuario": payload["id_usuario"],
    "email": payload["email"],
    "token": f"{payload['token'][:20]}... (longitud={len(payload['token'])})"
}, indent=2))
print()

try:
    response = requests.post(
        f"{BASE_URL}/tarjetas/guardar",
        json=payload
    )
    
    print(f"Status Code: {response.status_code}")
    data = response.json()
    print(f"\nRespuesta completa:")
    print(json.dumps(data, indent=2))
    
    if response.status_code == 201 and data.get('success'):
        print(f"\n✅ Tarjeta guardada exitosamente!")
        print(f"\nDatos de la tarjeta guardada:")
        tarjeta_data = data.get('data', {})
        print(f"  - ID: {tarjeta_data.get('id')}")
        print(f"  - Últimos 4 dígitos: {tarjeta_data.get('last_four_digits')}")
        print(f"  - Marca: {tarjeta_data.get('brand')}")
        print(f"  - Vencimiento: {tarjeta_data.get('expiration_month')}/{tarjeta_data.get('expiration_year')}")
        print(f"  - Titular: {tarjeta_data.get('holder_name')}")
        print(f"  - Por defecto: {tarjeta_data.get('is_default')}")
    else:
        print(f"\n❌ Error guardando tarjeta: {data.get('detail', 'Error desconocido')}")
        sys.exit(1)
        
except Exception as e:
    print(f"❌ Error en la solicitud: {e}")
    import traceback
    traceback.print_exc()
    sys.exit(1)

# Step 5: Verificar tarjeta en BD
print(f"\n\nPASO 5: Verificando que la tarjeta esté en la BD...")
print(f"Endpoint: GET {BASE_URL}/tarjetas?id_usuario={usuario_id}\n")

try:
    response = requests.get(
        f"{BASE_URL}/tarjetas",
        params={"id_usuario": usuario_id}
    )
    
    print(f"Status Code: {response.status_code}")
    data = response.json()
    
    if response.status_code == 200:
        tarjetas = data.get('data', [])
        print(f"\n✅ Tarjetas del usuario:")
        for i, tarjeta in enumerate(tarjetas, 1):
            print(f"\n  Tarjeta {i}:")
            print(f"    - ID: {tarjeta.get('id')}")
            print(f"    - Últimos 4 dígitos: {tarjeta.get('last_four_digits')}")
            print(f"    - Marca: {tarjeta.get('brand')}")
            print(f"    - Vencimiento: {tarjeta.get('expiration_month')}/{tarjeta.get('expiration_year')}")
            print(f"    - Por defecto: {tarjeta.get('is_default')}")
            print(f"    - Creada en: {tarjeta.get('created_at')}")
    else:
        print(f"❌ Error obteniendo tarjetas: {data}")
        sys.exit(1)
        
except Exception as e:
    print(f"❌ Error en la solicitud: {e}")
    import traceback
    traceback.print_exc()
    sys.exit(1)

print(f"\n\n{'='*80}")
print(f"✅ TEST COMPLETADO EXITOSAMENTE")
print(f"{'='*80}\n")
