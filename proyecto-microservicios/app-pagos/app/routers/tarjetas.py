import logging
from datetime import datetime
from decimal import Decimal
from typing import List

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from app.crud import mp_customer as crud_customer
from app.crud import pago as crud_pago
from app.crud import tarjeta_guardada as crud_tarjeta
from app.database import get_db
from app.schemas.pago import PagoCreate
from app.schemas.tarjeta import (
    PagoConTarjetaGuardadaRequest,
    PagoConTarjetaGuardadaResponse,
    TarjetaDeleteResponse,
    TarjetaGuardarRequest,
    TarjetaGuardarResponse,
    TarjetaResponse,
    TarjetasListResponse,
    TarjetaSetDefaultRequest,
)
from app.services.mercadopago_service import MercadoPagoService

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/tarjetas", tags=["tarjetas"])


@router.post("/guardar", response_model=TarjetaGuardarResponse, status_code=status.HTTP_201_CREATED)
def guardar_tarjeta(payload: TarjetaGuardarRequest, db: Session = Depends(get_db)):
    logger.info(f"[tarjetas] Guardando tarjeta id_usuario={payload.id_usuario}")

    try:
        mp_service = MercadoPagoService()
    except ValueError as e:
        raise HTTPException(status_code=500, detail=str(e))

    db_customer = crud_customer.get_customer_by_usuario(db, payload.id_usuario)
    
    if not db_customer:
        mp_customer = mp_service.create_customer(email=payload.email)
        if not mp_customer:
            raise HTTPException(status_code=502, detail="No fue posible crear customer en Mercado Pago")
        
        db_customer = crud_customer.create_customer(
            db=db,
            id_usuario=payload.id_usuario,
            mp_customer_id=mp_customer["id"],
        )
        logger.info(f"[tarjetas] Customer creado mp_customer_id={mp_customer['id']}")

    mp_card = mp_service.save_card(customer_id=db_customer.mp_customer_id, token=payload.token)
    if not mp_card:
        raise HTTPException(status_code=502, detail="No fue posible guardar tarjeta en Mercado Pago")

    tarjetas_existentes = crud_tarjeta.get_tarjetas_by_usuario(db, payload.id_usuario)
    is_first_card = len(tarjetas_existentes) == 0

    db_tarjeta = crud_tarjeta.create_tarjeta(
        db=db,
        id_usuario=payload.id_usuario,
        mp_customer_id=db_customer.mp_customer_id,
        mp_card_id=mp_card["id"],
        payment_method_id=mp_card.get("payment_method", {}).get("id", ""),
        brand=mp_card.get("payment_method", {}).get("name", ""),
        last_four_digits=mp_card.get("last_four_digits", ""),
        expiration_month=mp_card.get("expiration_month", 0),
        expiration_year=mp_card.get("expiration_year", 0),
        holder_name=mp_card.get("cardholder", {}).get("name", ""),
        is_default=is_first_card,
    )

    logger.info(f"[tarjetas] Tarjeta guardada id={db_tarjeta.id} mp_card_id={db_tarjeta.mp_card_id}")

    return TarjetaGuardarResponse(
        success=True,
        message="Tarjeta guardada correctamente",
        data=TarjetaResponse.model_validate(db_tarjeta),
    )


@router.get("/usuario/{id_usuario}", response_model=TarjetasListResponse)
def listar_tarjetas(id_usuario: int, db: Session = Depends(get_db)):
    logger.info(f"[tarjetas] Listando tarjetas id_usuario={id_usuario}")
    
    tarjetas = crud_tarjeta.get_tarjetas_by_usuario(db, id_usuario)
    
    return TarjetasListResponse(
        success=True,
        message="Tarjetas obtenidas correctamente",
        data=[TarjetaResponse.model_validate(t) for t in tarjetas],
    )


@router.patch("/{id_tarjeta}/default", response_model=TarjetaGuardarResponse)
def marcar_tarjeta_default(
    id_tarjeta: int,
    payload: TarjetaSetDefaultRequest,
    db: Session = Depends(get_db),
):
    logger.info(f"[tarjetas] Marcando tarjeta como default id_tarjeta={id_tarjeta}")
    
    db_tarjeta = crud_tarjeta.get_tarjeta_by_id(db, id_tarjeta)
    if not db_tarjeta:
        raise HTTPException(status_code=404, detail="Tarjeta no encontrada")

    if payload.is_default:
        db_tarjeta = crud_tarjeta.set_default_tarjeta(db, id_tarjeta, db_tarjeta.id_usuario)
    else:
        db_tarjeta.is_default = False
        db.commit()
        db.refresh(db_tarjeta)

    return TarjetaGuardarResponse(
        success=True,
        message="Tarjeta actualizada correctamente",
        data=TarjetaResponse.model_validate(db_tarjeta),
    )


@router.delete("/{id_tarjeta}", response_model=TarjetaDeleteResponse)
def eliminar_tarjeta(id_tarjeta: int, id_usuario: int, db: Session = Depends(get_db)):
    logger.info(f"[tarjetas] Eliminando tarjeta id_tarjeta={id_tarjeta} id_usuario={id_usuario}")
    
    db_tarjeta = crud_tarjeta.get_tarjeta_by_id(db, id_tarjeta)
    if not db_tarjeta:
        raise HTTPException(status_code=404, detail="Tarjeta no encontrada")
    
    if db_tarjeta.id_usuario != id_usuario:
        raise HTTPException(status_code=403, detail="No autorizado para eliminar esta tarjeta")

    try:
        mp_service = MercadoPagoService()
        mp_service.delete_card(db_tarjeta.mp_customer_id, db_tarjeta.mp_card_id)
    except Exception as e:
        logger.warning(f"[tarjetas] Error eliminando tarjeta en MP: {e}")

    success = crud_tarjeta.delete_tarjeta(db, id_tarjeta, id_usuario)
    if not success:
        raise HTTPException(status_code=500, detail="No fue posible eliminar la tarjeta")

    return TarjetaDeleteResponse(success=True, message="Tarjeta eliminada correctamente")


@router.post("/pagar", response_model=PagoConTarjetaGuardadaResponse, status_code=status.HTTP_201_CREATED)
def pagar_con_tarjeta_guardada(payload: PagoConTarjetaGuardadaRequest, db: Session = Depends(get_db)):
    logger.info(f"[tarjetas] Pago con tarjeta guardada id_usuario={payload.id_usuario} id_tarjeta={payload.id_tarjeta} monto={payload.monto}")

    db_tarjeta = crud_tarjeta.get_tarjeta_by_id(db, payload.id_tarjeta)
    if not db_tarjeta:
        raise HTTPException(status_code=404, detail="Tarjeta no encontrada")
    
    if db_tarjeta.id_usuario != payload.id_usuario:
        raise HTTPException(status_code=403, detail="No autorizado para usar esta tarjeta")

    db_customer = crud_customer.get_customer_by_usuario(db, payload.id_usuario)
    if not db_customer:
        raise HTTPException(status_code=404, detail="Customer no encontrado")

    external_reference = crud_pago.generate_external_reference(payload.id_usuario)

    try:
        mp_service = MercadoPagoService()
    except ValueError as e:
        raise HTTPException(status_code=500, detail=str(e))

    mp_payment = mp_service.create_payment_with_saved_card(
        customer_id=db_customer.mp_customer_id,
        card_id=db_tarjeta.mp_card_id,
        payment_method_id=db_tarjeta.payment_method_id,
        transaction_amount=payload.monto,
        description=payload.descripcion,
        email=db_customer.mp_customer_id,
        external_reference=external_reference,
    )

    if not mp_payment:
        raise HTTPException(status_code=502, detail="No fue posible procesar el pago en Mercado Pago")

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
            estado_pago=crud_pago.map_mp_status(mp_payment.get("status", "")),
            codigo_transaccion=str(mp_payment.get("id")),
            observacion=external_reference,
        ),
    )

    logger.info(f"[tarjetas] Pago creado id_pago={db_pago.id_pago} mp_payment_id={mp_payment.get('id')} status={mp_payment.get('status')}")

    return PagoConTarjetaGuardadaResponse(
        success=True,
        message="Pago procesado correctamente",
        data={
            "id_pago": db_pago.id_pago,
            "mp_payment_id": mp_payment.get("id"),
            "status": mp_payment.get("status"),
            "status_detail": mp_payment.get("status_detail"),
            "external_reference": external_reference,
        },
    )
