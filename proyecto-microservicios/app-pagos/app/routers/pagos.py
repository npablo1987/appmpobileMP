import logging
import os
from datetime import datetime
from decimal import Decimal
from typing import List

import mercadopago
import requests
from fastapi import APIRouter, Depends, HTTPException, Query, Request, status
from sqlalchemy.orm import Session

from app.crud import pago as crud_pago
from app.database import get_db
from app.schemas.pago import (
    PagoCreate,
    PagoCreateCheckoutRequest,
    PagoCreateCheckoutResponse,
    PagoDirectoRequest,
    PagoDirectoResponse,
    PagoEstadoResponse,
    PagoEstadoUpdate,
    PagoResponse,
    PagoUpdate,
    WebhookAckResponse,
)

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/pagos", tags=["pagos"])

MP_ACCESS_TOKEN = os.getenv("MP_ACCESS_TOKEN", "")
MP_SUCCESS_URL = os.getenv("MP_SUCCESS_URL", "https://www.mercadopago.cl")
MP_FAILURE_URL = os.getenv("MP_FAILURE_URL", "https://www.mercadopago.cl")
MP_PENDING_URL = os.getenv("MP_PENDING_URL", "https://www.mercadopago.cl")
MP_WEBHOOK_URL = os.getenv("MP_WEBHOOK_URL")


@router.post("/crear", response_model=PagoCreateCheckoutResponse, status_code=status.HTTP_201_CREATED)
def crear_pago_checkout(payload: PagoCreateCheckoutRequest, db: Session = Depends(get_db)):
    if not MP_ACCESS_TOKEN:
        raise HTTPException(status_code=500, detail="MP_ACCESS_TOKEN no configurado")

    logger.info("[pagos] Creación de pago iniciada id_usuario=%s monto=%s", payload.id_usuario, payload.monto)

    external_reference = crud_pago.generate_external_reference(payload.id_usuario)
    sdk = mercadopago.SDK(MP_ACCESS_TOKEN)
    preference_data = {
        "items": [
            {
                "title": payload.descripcion,
                "quantity": 1,
                "currency_id": "CLP",
                "unit_price": float(payload.monto),
            }
        ],
        "payer": {"email": payload.email_pagador},
        "external_reference": external_reference,
        "back_urls": {
            "success": MP_SUCCESS_URL,
            "failure": MP_FAILURE_URL,
            "pending": MP_PENDING_URL,
        },
        "auto_return": "approved",
        "notification_url": MP_WEBHOOK_URL,
    }

    logger.info("[pagos] Envío a Mercado Pago external_reference=%s", external_reference)
    preference_response = sdk.preference().create(preference_data)
    logger.info("[pagos] Respuesta Mercado Pago status=%s", preference_response.get("status"))

    body = preference_response.get("response", {})
    init_point = body.get("init_point")
    preference_id = body.get("id")
    if preference_response.get("status") not in (200, 201) or not init_point:
        raise HTTPException(status_code=502, detail="No fue posible crear preferencia en Mercado Pago")

    now = datetime.utcnow()
    db_pago = crud_pago.create_pago(
        db=db,
        pago=PagoCreate(
            id_usuario=payload.id_usuario,
            id_suscripcion=None,
            id_metodo_pago=1,
            periodo_anio=now.year,
            periodo_mes=now.month,
            monto_total=Decimal(payload.monto),
            estado_pago="PENDIENTE",
            codigo_transaccion=preference_id,
            observacion=external_reference,
        ),
    )

    return PagoCreateCheckoutResponse(
        success=True,
        message="Pago creado correctamente",
        data={
            "id_pago": db_pago.id_pago,
            "url_pago": init_point,
            "external_reference": external_reference,
        },
    )


@router.get("/{id_pago}/estado", response_model=PagoEstadoResponse)
def consultar_estado_pago(id_pago: int, db: Session = Depends(get_db)):
    db_pago = crud_pago.get_pago(db, pago_id=id_pago)
    if not db_pago:
        raise HTTPException(status_code=404, detail="Pago no encontrado")

    estado = db_pago.estado_pago
    codigo = db_pago.codigo_transaccion
    if MP_ACCESS_TOKEN and codigo:
        sdk = mercadopago.SDK(MP_ACCESS_TOKEN)
        search = sdk.payment().search({"criteria": "external_reference", "external_reference": db_pago.observacion})
        results = search.get("response", {}).get("results", [])
        if results:
            estado = crud_pago.map_mp_status(results[0].get("status", ""))
            db_pago = crud_pago.update_estado_pago(db, pago_id=id_pago, estado=estado)
            tx_id = str(results[0].get("id")) if results[0].get("id") else None
            if tx_id:
                db_pago = crud_pago.update_codigo_transaccion(db, pago_id=id_pago, codigo=tx_id)

    return PagoEstadoResponse(id_pago=id_pago, estado=estado)


@router.post("/directo/procesar", response_model=PagoDirectoResponse, status_code=status.HTTP_201_CREATED)
def procesar_pago_directo(payload: PagoDirectoRequest, db: Session = Depends(get_db)):
    """
    Procesa un pago directo con tarjeta (sin guardarla).
    Devuelve id_pago para polling de estado con máximo 2 minutos de espera.
    """
    if not MP_ACCESS_TOKEN:
        raise HTTPException(status_code=500, detail="MP_ACCESS_TOKEN no configurado")

    logger.info(f"\n{'='*80}")
    logger.info(f"[pagos] 🔵 INICIO PAGO DIRECTO - id_usuario={payload.id_usuario} monto={payload.monto}")
    logger.info(f"[pagos] Tarjeta: {payload.numero_tarjeta[-4:]} Titular: {payload.nombre_titular}")
    logger.info(f"{'='*80}\n")

    try:
        # Generar token de tarjeta con MP
        logger.info("[pagos] 📌 Generando token de tarjeta con Mercado Pago...")
        token_url = "https://api.mercadopago.com/v1/card_tokens"
        token_payload = {
            "public_key": os.getenv("MP_PUBLIC_KEY", ""),
            "card_number": payload.numero_tarjeta,
            "expiration_month": payload.mes_vencimiento,
            "expiration_year": payload.anio_vencimiento,
            "security_code": payload.cvv,
            "cardholder": {
                "name": payload.nombre_titular,
                "identification": {
                    "type": "DNI",
                    "number": "12345678"
                }
            }
        }

        token_response = requests.post(token_url, json=token_payload)
        if token_response.status_code != 201:
            logger.error(f"[pagos] ❌ Error generando token: {token_response.text}")
            raise HTTPException(status_code=502, detail="No fue posible tokenizar la tarjeta")

        token_data = token_response.json()
        card_token = token_data.get("id")
        if not card_token:
            logger.error(f"[pagos] ❌ Token vacío en respuesta: {token_data}")
            raise HTTPException(status_code=502, detail="No fue posible tokenizar la tarjeta")

        logger.info(f"[pagos] ✅ Token generado: {card_token[:20]}...")

        # Crear pago en Mercado Pago
        logger.info("[pagos] 📌 Creando pago en Mercado Pago...")
        external_reference = crud_pago.generate_external_reference(payload.id_usuario)
        
        payment_url = "https://api.mercadopago.com/v1/payments"
        payment_payload = {
            "token": card_token,
            "amount": float(payload.monto),
            "currency_id": "CLP",
            "description": payload.descripcion,
            "external_reference": external_reference,
            "payer": {
                "email": payload.email,
                "first_name": payload.nombre_titular.split()[0],
                "last_name": " ".join(payload.nombre_titular.split()[1:]) if len(payload.nombre_titular.split()) > 1 else "Usuario"
            }
        }

        headers = {
            "Authorization": f"Bearer {MP_ACCESS_TOKEN}",
            "Content-Type": "application/json"
        }

        payment_response = requests.post(payment_url, json=payment_payload, headers=headers)
        payment_data = payment_response.json()

        logger.info(f"[pagos] Respuesta MP status: {payment_response.status_code}")

        if payment_response.status_code not in (200, 201):
            logger.error(f"[pagos] ❌ Error en pago: {payment_data}")
            error_msg = payment_data.get("message", "Error procesando pago")
            raise HTTPException(status_code=502, detail=error_msg)

        mp_payment_id = payment_data.get("id")
        mp_status = payment_data.get("status")
        logger.info(f"[pagos] ✅ Pago creado: id={mp_payment_id} status={mp_status}")

        # Guardar en BD
        logger.info("[pagos] 📌 Guardando pago en BD...")
        now = datetime.utcnow()
        db_pago = crud_pago.create_pago(
            db=db,
            pago=PagoCreate(
                id_usuario=payload.id_usuario,
                id_suscripcion=None,
                id_metodo_pago=1,
                periodo_anio=now.year,
                periodo_mes=now.month,
                monto_total=Decimal(str(payload.monto)),
                estado_pago=crud_pago.map_mp_status(mp_status),
                codigo_transaccion=str(mp_payment_id),
                observacion=external_reference,
            ),
        )
        logger.info(f"[pagos] ✅ Pago guardado en BD: id_pago={db_pago.id_pago}")
        logger.info(f"{'='*80}\n")

        return PagoDirectoResponse(
            success=True,
            message="Pago procesado correctamente",
            data={
                "id_pago": db_pago.id_pago,
                "estado": crud_pago.map_mp_status(mp_status),
                "mp_payment_id": mp_payment_id,
                "external_reference": external_reference,
            },
        )

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"[pagos] ❌ Error inesperado: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/webhook", response_model=WebhookAckResponse)
async def recibir_webhook(
    request: Request,
    type: str | None = Query(default=None),
    data_id: str | None = Query(default=None, alias="data.id"),
    db: Session = Depends(get_db),
):
    payload = await request.json() if request.headers.get("content-type", "").startswith("application/json") else {}
    logger.info("[pagos] Webhook recibido query_type=%s query_data_id=%s body=%s", type, data_id, payload)

    event_type = type or payload.get("type")
    event_data_id = data_id or payload.get("data", {}).get("id")
    if event_type != "payment" or not event_data_id:
        return WebhookAckResponse(success=True, message="Evento ignorado")

    if not MP_ACCESS_TOKEN:
        raise HTTPException(status_code=500, detail="MP_ACCESS_TOKEN no configurado")

    sdk = mercadopago.SDK(MP_ACCESS_TOKEN)
    payment_resp = sdk.payment().get(event_data_id)
    payment = payment_resp.get("response", {})
    external_reference = payment.get("external_reference")
    if not external_reference:
        return WebhookAckResponse(success=True, message="Pago sin external_reference")

    db_pago = crud_pago.get_pago_by_external_reference(db, external_reference)
    if not db_pago:
        return WebhookAckResponse(success=True, message="Pago no encontrado")

    nuevo_estado = crud_pago.map_mp_status(payment.get("status", ""))
    if db_pago.estado_pago != nuevo_estado:
        crud_pago.update_estado_pago(db, pago_id=db_pago.id_pago, estado=nuevo_estado)
        logger.info("[pagos] Estado actualizado id_pago=%s nuevo_estado=%s", db_pago.id_pago, nuevo_estado)

    crud_pago.update_codigo_transaccion(db, pago_id=db_pago.id_pago, codigo=str(payment.get("id")))

    return WebhookAckResponse(success=True, message="Webhook procesado")


@router.get("/", response_model=List[PagoResponse])
def read_pagos(skip: int = 0, limit: int = 100, db: Session = Depends(get_db)):
    return crud_pago.get_pagos(db, skip=skip, limit=limit)


@router.get("/usuario/{id_usuario}", response_model=List[PagoResponse])
def read_pagos_by_usuario(id_usuario: int, db: Session = Depends(get_db)):
    return crud_pago.get_pagos_by_usuario(db, usuario_id=id_usuario)


@router.get("/periodo/{anio}/{mes}", response_model=List[PagoResponse])
def read_pagos_by_periodo(anio: int, mes: int, db: Session = Depends(get_db)):
    if not 1 <= mes <= 12:
        raise HTTPException(status_code=400, detail="El mes debe estar entre 1 y 12")
    if anio < 2020:
        raise HTTPException(status_code=400, detail="El año debe ser mayor o igual a 2020")
    return crud_pago.get_pagos_by_periodo(db, anio=anio, mes=mes)


@router.get("/suscripcion/{id_suscripcion}", response_model=List[PagoResponse])
def read_pagos_by_suscripcion(id_suscripcion: int, db: Session = Depends(get_db)):
    return crud_pago.get_pagos_by_suscripcion(db, suscripcion_id=id_suscripcion)


@router.get("/{id}", response_model=PagoResponse)
def read_pago(id: int, db: Session = Depends(get_db)):
    db_pago = crud_pago.get_pago(db, pago_id=id)
    if db_pago is None:
        raise HTTPException(status_code=404, detail="Pago no encontrado")
    return db_pago


@router.put("/{id}", response_model=PagoResponse)
def update_pago(id: int, pago: PagoUpdate, db: Session = Depends(get_db)):
    db_pago = crud_pago.update_pago(db, pago_id=id, pago=pago)
    if db_pago is None:
        raise HTTPException(status_code=404, detail="Pago no encontrado")
    return db_pago


@router.patch("/{id}/estado", response_model=PagoResponse)
def update_estado_pago(id: int, estado_update: PagoEstadoUpdate, db: Session = Depends(get_db)):
    db_pago = crud_pago.update_estado_pago(db, pago_id=id, estado=estado_update.estado_pago)
    if db_pago is None:
        raise HTTPException(status_code=404, detail="Pago no encontrado")
    return db_pago


@router.post("/{id}/anular", response_model=PagoResponse)
def anular_pago(id: int, db: Session = Depends(get_db)):
    db_pago = crud_pago.anular_pago(db, pago_id=id)
    if db_pago is None:
        raise HTTPException(status_code=404, detail="Pago no encontrado")
    return db_pago


@router.delete("/{id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_pago(id: int, db: Session = Depends(get_db)):
    success = crud_pago.delete_pago(db, pago_id=id)
    if not success:
        raise HTTPException(status_code=404, detail="Pago no encontrado")
    return None
