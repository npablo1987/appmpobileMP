#!/usr/bin/env python3
"""
Script para probar la creación de customer en Mercado Pago directamente
"""

import os
import sys
import mercadopago
import json

MP_ACCESS_TOKEN = "TEST-3532723459071547-040921-9175516fd1e225d32f68ce03f1fbf445-3326014045"

print("="*80)
print("TEST DE CREACIÓN DE CUSTOMER EN MERCADO PAGO")
print("="*80)
print()

print(f"Token: {MP_ACCESS_TOKEN[:30]}...")
print()

try:
    sdk = mercadopago.SDK(MP_ACCESS_TOKEN)
    
    # Test 1: Crear customer
    print("TEST 1: Creando customer...")
    customer_data = {
        "email": "test.apro@mercadopago.com",
        "first_name": "Test",
        "last_name": "User"
    }
    
    print(f"Datos del customer:")
    print(json.dumps(customer_data, indent=2))
    print()
    
    response = sdk.customer().create(customer_data)
    
    print(f"Status: {response.get('status')}")
    print(f"Respuesta completa:")
    print(json.dumps(response, indent=2))
    print()
    
    if response.get("status") in (200, 201):
        customer = response.get("response", {})
        customer_id = customer.get("id")
        print(f"✅ Customer creado exitosamente!")
        print(f"   ID: {customer_id}")
        print()
        
        # Test 2: Crear token de tarjeta
        print("TEST 2: Creando token de tarjeta...")
        card_token_data = {
            "cardNumber": "4168818844447115",
            "securityCode": "123",
            "expirationMonth": 11,
            "expirationYear": 2030,
            "cardholder": {
                "name": "APRO",
                "identification": {
                    "type": "DNI",
                    "number": "12345678"
                }
            }
        }
        
        token_response = sdk.card_token().create(card_token_data)
        print(f"Status: {token_response.get('status')}")
        
        if token_response.get('status') in (200, 201):
            token = token_response.get('response', {}).get('id')
            print(f"✅ Token creado: {token}")
            print()
            
            # Test 3: Guardar tarjeta
            print("TEST 3: Guardando tarjeta para el customer...")
            card_data = {"token": token}
            
            card_response = sdk.card().create(customer_id, card_data)
            print(f"Status: {card_response.get('status')}")
            print(f"Respuesta completa:")
            print(json.dumps(card_response, indent=2))
            print()
            
            if card_response.get("status") in (200, 201):
                card = card_response.get("response", {})
                print(f"✅ Tarjeta guardada exitosamente!")
                print(f"   Card ID: {card.get('id')}")
                print(f"   Últimos 4 dígitos: {card.get('last_four_digits')}")
                print(f"   Marca: {card.get('payment_method', {}).get('name')}")
            else:
                print(f"❌ Error guardando tarjeta")
        else:
            print(f"❌ Error creando token")
            print(json.dumps(token_response, indent=2))
    elif response.get("status") == 400:
        # Verificar si el customer ya existe
        cause = response.get("response", {}).get("cause", [])
        print(f"Cause: {cause}")
        
        if cause and any(c.get("code") == "101" for c in cause):
            print("⚠️  El customer ya existe, intentando buscar...")
            
            search_response = sdk.customer().search({"email": "test.apro@mercadopago.com"})
            print(f"Search status: {search_response.get('status')}")
            print(f"Search response:")
            print(json.dumps(search_response, indent=2))
        else:
            print(f"❌ Error 400 desconocido")
    else:
        print(f"❌ Error creando customer")
        
except Exception as e:
    print(f"❌ Excepción: {e}")
    import traceback
    traceback.print_exc()

print()
print("="*80)
print("TEST COMPLETADO")
print("="*80)
