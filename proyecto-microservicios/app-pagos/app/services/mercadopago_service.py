import logging
import os
from typing import Optional, Dict, Any
import mercadopago

logger = logging.getLogger(__name__)

MP_ACCESS_TOKEN = os.getenv("MP_ACCESS_TOKEN", "")


class MercadoPagoService:
    def __init__(self):
        if not MP_ACCESS_TOKEN:
            raise ValueError("MP_ACCESS_TOKEN no está configurado")
        self.sdk = mercadopago.SDK(MP_ACCESS_TOKEN)

    def create_customer(self, email: str, first_name: str = "", last_name: str = "") -> Optional[Dict[str, Any]]:
        try:
            customer_data = {
                "email": email,
            }
            if first_name:
                customer_data["first_name"] = first_name
            if last_name:
                customer_data["last_name"] = last_name

            logger.info(f"[MP] Creando customer email={email}")
            response = self.sdk.customer().create(customer_data)
            
            if response.get("status") in (200, 201):
                customer = response.get("response", {})
                logger.info(f"[MP] Customer creado id={customer.get('id')}")
                return customer
            else:
                logger.error(f"[MP] Error creando customer: {response}")
                return None
        except Exception as e:
            logger.error(f"[MP] Excepción creando customer: {e}")
            return None

    def get_customer(self, customer_id: str) -> Optional[Dict[str, Any]]:
        try:
            logger.info(f"[MP] Obteniendo customer id={customer_id}")
            response = self.sdk.customer().get(customer_id)
            
            if response.get("status") == 200:
                return response.get("response", {})
            else:
                logger.error(f"[MP] Error obteniendo customer: {response}")
                return None
        except Exception as e:
            logger.error(f"[MP] Excepción obteniendo customer: {e}")
            return None

    def save_card(self, customer_id: str, token: str) -> Optional[Dict[str, Any]]:
        try:
            card_data = {"token": token}
            logger.info(f"[MP] Guardando tarjeta customer_id={customer_id}")
            response = self.sdk.card().create(customer_id, card_data)
            
            if response.get("status") in (200, 201):
                card = response.get("response", {})
                logger.info(f"[MP] Tarjeta guardada id={card.get('id')}")
                return card
            else:
                logger.error(f"[MP] Error guardando tarjeta: {response}")
                return None
        except Exception as e:
            logger.error(f"[MP] Excepción guardando tarjeta: {e}")
            return None

    def get_card(self, customer_id: str, card_id: str) -> Optional[Dict[str, Any]]:
        try:
            logger.info(f"[MP] Obteniendo tarjeta customer_id={customer_id} card_id={card_id}")
            response = self.sdk.card().get(customer_id, card_id)
            
            if response.get("status") == 200:
                return response.get("response", {})
            else:
                logger.error(f"[MP] Error obteniendo tarjeta: {response}")
                return None
        except Exception as e:
            logger.error(f"[MP] Excepción obteniendo tarjeta: {e}")
            return None

    def delete_card(self, customer_id: str, card_id: str) -> bool:
        try:
            logger.info(f"[MP] Eliminando tarjeta customer_id={customer_id} card_id={card_id}")
            response = self.sdk.card().delete(customer_id, card_id)
            
            if response.get("status") in (200, 204):
                logger.info(f"[MP] Tarjeta eliminada")
                return True
            else:
                logger.error(f"[MP] Error eliminando tarjeta: {response}")
                return False
        except Exception as e:
            logger.error(f"[MP] Excepción eliminando tarjeta: {e}")
            return False

    def create_payment_with_saved_card(
        self,
        customer_id: str,
        card_id: str,
        payment_method_id: str,
        transaction_amount: float,
        description: str,
        email: str,
        external_reference: str,
    ) -> Optional[Dict[str, Any]]:
        try:
            payment_data = {
                "transaction_amount": transaction_amount,
                "description": description,
                "payment_method_id": payment_method_id,
                "payer": {
                    "id": customer_id,
                    "email": email,
                },
                "token": card_id,
                "external_reference": external_reference,
                "installments": 1,
                "capture": True,
            }

            logger.info(f"[MP] Creando pago con tarjeta guardada customer_id={customer_id} card_id={card_id} monto={transaction_amount}")
            response = self.sdk.payment().create(payment_data)
            
            if response.get("status") in (200, 201):
                payment = response.get("response", {})
                logger.info(f"[MP] Pago creado id={payment.get('id')} status={payment.get('status')}")
                return payment
            else:
                logger.error(f"[MP] Error creando pago: {response}")
                return None
        except Exception as e:
            logger.error(f"[MP] Excepción creando pago: {e}")
            return None

    def get_payment(self, payment_id: str) -> Optional[Dict[str, Any]]:
        try:
            logger.info(f"[MP] Obteniendo pago id={payment_id}")
            response = self.sdk.payment().get(payment_id)
            
            if response.get("status") == 200:
                return response.get("response", {})
            else:
                logger.error(f"[MP] Error obteniendo pago: {response}")
                return None
        except Exception as e:
            logger.error(f"[MP] Excepción obteniendo pago: {e}")
            return None
