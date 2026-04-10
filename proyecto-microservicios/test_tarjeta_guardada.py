#!/usr/bin/env python3
"""
Script de prueba para guardar tarjetas de débito y crédito usando tarjetas de prueba de Mercado Pago.
Utiliza datos reales de las tarjetas de prueba proporcionadas por Mercado Pago.
"""

import os
import sys
import logging
import requests
import json
from typing import Optional, Dict, Any

# Configurar logging
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s | %(levelname)-8s | %(message)s"
)
logger = logging.getLogger(__name__)

# Configuración
MP_ACCESS_TOKEN = os.getenv("MP_ACCESS_TOKEN", "")
API_BASE_URL = os.getenv("API_BASE_URL", "http://localhost:8002")

# Tarjetas de prueba - Datos de Mercado Pago
TEST_CARDS = {
    "visa_credit": {
        "number": "4168 8188 4444 7115",
        "number_clean": "4168818844447115",
        "cvc": "123",
        "exp_month": "11",
        "exp_year": "30",
        "cardholder": {
            "name": "APRO",  # Pago aprobado
        },
        "identification": {
            "type": "DNI",
            "number": "123456789"
        },
        "description": "Visa - Pago Aprobado"
    },
    "visa_debit": {
        "number": "4023 6535 2391 4373",
        "number_clean": "4023653523914373",
        "cvc": "123",
        "exp_month": "11",
        "exp_year": "30",
        "cardholder": {
            "name": "APRO",
        },
        "identification": {
            "type": "DNI",
            "number": "123456789"
        },
        "description": "Visa Debit - Pago Aprobado"
    },
    "mastercard_credit": {
        "number": "5416 7526 0258 2580",
        "number_clean": "5416752602582580",
        "cvc": "123",
        "exp_month": "11",
        "exp_year": "30",
        "cardholder": {
            "name": "APRO",
        },
        "identification": {
            "type": "DNI",
            "number": "123456789"
        },
        "description": "Mastercard - Pago Aprobado"
    },
    "mastercard_debit": {
        "number": "5241 0198 2664 6950",
        "number_clean": "5241019826646950",
        "cvc": "123",
        "exp_month": "11",
        "exp_year": "30",
        "cardholder": {
            "name": "APRO",
        },
        "identification": {
            "type": "DNI",
            "number": "123456789"
        },
        "description": "Mastercard Debit - Pago Aprobado"
    },
    "amex": {
        "number": "3757 781744 61804",
        "number_clean": "375778174461804",
        "cvc": "1234",
        "exp_month": "11",
        "exp_year": "30",
        "cardholder": {
            "name": "APRO",
        },
        "identification": {
            "type": "DNI",
            "number": "123456789"
        },
        "description": "American Express - Pago Aprobado"
    }
}

# Escenarios de error para pruebas adicionales
ERROR_SCENARIOS = {
    "rechazado_error_general": {
        "cardholder_name": "OTHE",
        "description": "Rechazado por error general"
    },
    "pendiente": {
        "cardholder_name": "CONT",
        "description": "Pendiente de pago"
    },
    "rechazado_validacion": {
        "cardholder_name": "CALL",
        "description": "Rechazado con validación para autorizar"
    },
    "rechazado_fondos": {
        "cardholder_name": "FUND",
        "description": "Rechazado por importe insuficiente"
    },
    "rechazado_cvc": {
        "cardholder_name": "SECU",
        "description": "Rechazado por código de seguridad inválido"
    },
}


def generate_token(card_data: Dict[str, Any]) -> Optional[str]:
    """Genera un token usando el SDK de Mercado Pago"""
    try:
        import mercadopago
        
        if not MP_ACCESS_TOKEN:
            logger.error("❌ MP_ACCESS_TOKEN no está configurado")
            return None
        
        sdk = mercadopago.SDK(MP_ACCESS_TOKEN)
        
        logger.info(f"📌 Generando token para: {card_data['description']}")
        logger.debug(f"   Número: {card_data['number']}")
        logger.debug(f"   Vencimiento: {card_data['exp_month']}/{card_data['exp_year']}")
        logger.debug(f"   Titular: {card_data['cardholder']['name']}")
        
        card_token_data = {
            "cardNumber": card_data['number_clean'],
            "securityCode": card_data['cvc'],
            "expirationMonth": int(card_data['exp_month']),
            "expirationYear": int(card_data['exp_year']),
            "cardholder": {
                "name": card_data['cardholder']['name'],
                "identification": {
                    "type": card_data['identification']['type'],
                    "number": card_data['identification']['number']
                }
            }
        }
        
        token_response = sdk.card_token().create(card_token_data)
        
        if token_response.get("status") in (200, 201):
            token = token_response.get("response", {}).get("id")
            logger.info(f"✅ Token generado exitosamente: {token[:30]}...")
            return token
        else:
            logger.error(f"❌ Error generando token: {token_response}")
            return None
            
    except Exception as e:
        logger.error(f"❌ Excepción generando token: {e}", exc_info=True)
        return None


def test_guardar_tarjeta(
    id_usuario: int = 1,
    email: str = "test@example.com",
    card_key: str = "visa_credit"
) -> bool:
    """Prueba guardar una tarjeta a través de la API"""
    
    card_data = TEST_CARDS.get(card_key)
    if not card_data:
        logger.error(f"❌ Tarjeta de prueba no encontrada: {card_key}")
        return False
    
    logger.info(f"\n{'='*80}")
    logger.info(f"🔵 INICIANDO PRUEBA: {card_data['description']}")
    logger.info(f"{'='*80}\n")
    
    # Generar token
    token = generate_token(card_data)
    if not token:
        logger.error(f"❌ No fue posible generar el token")
        return False
    
    # Llamar al endpoint para guardar la tarjeta
    try:
        logger.info(f"📌 Enviando petición al endpoint /tarjetas/guardar...")
        
        payload = {
            "id_usuario": id_usuario,
            "token": token,
            "email": email
        }
        
        response = requests.post(
            f"{API_BASE_URL}/tarjetas/guardar",
            json=payload,
            timeout=30
        )
        
        logger.info(f"   Status: {response.status_code}")
        
        if response.status_code == 201:
            data = response.json()
            if data.get("success"):
                tarjeta = data.get("data", {})
                logger.info(f"✅ Tarjeta guardada exitosamente!")
                logger.info(f"   ID: {tarjeta.get('id')}")
                logger.info(f"   Marca: {tarjeta.get('brand')}")
                logger.info(f"   Últimos 4 dígitos: {tarjeta.get('last_four_digits')}")
                logger.info(f"   Vencimiento: {tarjeta.get('expiration_month')}/{tarjeta.get('expiration_year')}")
                logger.info(f"   Titular: {tarjeta.get('holder_name')}")
                logger.info(f"   Es predeterminada: {tarjeta.get('is_default')}")
                logger.info(f"\n{'='*80}")
                logger.info(f"🟢 PRUEBA EXITOSA")
                logger.info(f"{'='*80}\n")
                return True
            else:
                logger.error(f"❌ Respuesta exitosa pero success=false: {data.get('message')}")
                return False
        else:
            logger.error(f"❌ Error en la respuesta: {response.status_code}")
            try:
                error_data = response.json()
                logger.error(f"   Detalle: {error_data}")
            except:
                logger.error(f"   Respuesta: {response.text}")
            return False
            
    except requests.exceptions.ConnectionError:
        logger.error(f"❌ No se pudo conectar a {API_BASE_URL}")
        logger.error(f"   Asegúrate de que el servicio app-pagos esté corriendo")
        return False
    except Exception as e:
        logger.error(f"❌ Error en la petición: {e}", exc_info=True)
        return False


def test_listar_tarjetas(id_usuario: int = 1) -> bool:
    """Prueba listar las tarjetas guardadas de un usuario"""
    
    try:
        logger.info(f"\n{'='*80}")
        logger.info(f"📋 Listando tarjetas del usuario {id_usuario}")
        logger.info(f"{'='*80}\n")
        
        response = requests.get(
            f"{API_BASE_URL}/tarjetas/usuario/{id_usuario}",
            timeout=30
        )
        
        logger.info(f"   Status: {response.status_code}")
        
        if response.status_code == 200:
            data = response.json()
            if data.get("success"):
                tarjetas = data.get("data", [])
                logger.info(f"✅ Se encontraron {len(tarjetas)} tarjeta(s)")
                for i, tarjeta in enumerate(tarjetas, 1):
                    logger.info(f"\n   Tarjeta {i}:")
                    logger.info(f"     ID: {tarjeta.get('id')}")
                    logger.info(f"     Marca: {tarjeta.get('brand')}")
                    logger.info(f"     Últimos 4 dígitos: {tarjeta.get('last_four_digits')}")
                    logger.info(f"     Vencimiento: {tarjeta.get('expiration_month')}/{tarjeta.get('expiration_year')}")
                    logger.info(f"     Titular: {tarjeta.get('holder_name')}")
                    logger.info(f"     Es predeterminada: {tarjeta.get('is_default')}")
                    logger.info(f"     Creada: {tarjeta.get('created_at')}")
                
                logger.info(f"\n{'='*80}")
                logger.info(f"✅ Listado exitoso")
                logger.info(f"{'='*80}\n")
                return True
            else:
                logger.error(f"❌ Error: {data.get('message')}")
                return False
        else:
            logger.error(f"❌ Error en la respuesta: {response.status_code}")
            logger.error(f"   Respuesta: {response.text}")
            return False
            
    except requests.exceptions.ConnectionError:
        logger.error(f"❌ No se pudo conectar a {API_BASE_URL}")
        return False
    except Exception as e:
        logger.error(f"❌ Error: {e}", exc_info=True)
        return False


def main():
    """Función principal"""
    
    logger.info("\n" + "="*80)
    logger.info("🧪 INICIANDO PRUEBAS DE TARJETAS DE MERCADO PAGO")
    logger.info("="*80 + "\n")
    
    # Verificar configuración
    if not MP_ACCESS_TOKEN:
        logger.error("❌ Error: MP_ACCESS_TOKEN no está configurado")
        logger.info("   Configura la variable de entorno MP_ACCESS_TOKEN con tu token de Mercado Pago")
        sys.exit(1)
    
    logger.info(f"✅ Configuración detectada:")
    logger.info(f"   API Base URL: {API_BASE_URL}")
    logger.info(f"   MP Token: {MP_ACCESS_TOKEN[:20]}...")
    logger.info("")
    
    # Variables para tracking
    usuario_id = 1
    email = "test.tarjetas@example.com"
    tarjetas_guardadas = []
    tarjetas_fallidas = []
    
    # Probar guardado de tarjetas principales
    tarjetas_a_probar = ["visa_credit", "mastercard_credit", "mastercard_debit"]
    
    for card_key in tarjetas_a_probar:
        if test_guardar_tarjeta(id_usuario=usuario_id, email=email, card_key=card_key):
            tarjetas_guardadas.append(card_key)
        else:
            tarjetas_fallidas.append(card_key)
    
    # Listar todas las tarjetas guardadas
    logger.info("\n")
    test_listar_tarjetas(id_usuario=usuario_id)
    
    # Resumen
    logger.info(f"\n{'='*80}")
    logger.info(f"📊 RESUMEN DE PRUEBAS")
    logger.info(f"{'='*80}")
    logger.info(f"✅ Tarjetas guardadas exitosamente: {len(tarjetas_guardadas)}")
    for card in tarjetas_guardadas:
        logger.info(f"   • {TEST_CARDS[card]['description']}")
    
    if tarjetas_fallidas:
        logger.info(f"❌ Tarjetas que fallaron: {len(tarjetas_fallidas)}")
        for card in tarjetas_fallidas:
            logger.info(f"   • {TEST_CARDS[card]['description']}")
    
    logger.info(f"{'='*80}\n")
    
    if tarjetas_guardadas:
        return 0
    else:
        return 1


if __name__ == "__main__":
    sys.exit(main())
