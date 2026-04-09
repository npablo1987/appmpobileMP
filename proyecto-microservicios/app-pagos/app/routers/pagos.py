from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from app.database import get_db
from app.schemas.pago import PagoCreate, PagoUpdate, PagoResponse, PagoEstadoUpdate
from app.crud import pago as crud_pago

router = APIRouter(prefix="/pagos", tags=["pagos"])

@router.post("/", response_model=PagoResponse, status_code=status.HTTP_201_CREATED)
def create_pago(pago: PagoCreate, db: Session = Depends(get_db)):
    return crud_pago.create_pago(db=db, pago=pago)

@router.get("/", response_model=List[PagoResponse])
def read_pagos(skip: int = 0, limit: int = 100, db: Session = Depends(get_db)):
    pagos = crud_pago.get_pagos(db, skip=skip, limit=limit)
    return pagos

@router.get("/{id}", response_model=PagoResponse)
def read_pago(id: int, db: Session = Depends(get_db)):
    db_pago = crud_pago.get_pago(db, pago_id=id)
    if db_pago is None:
        raise HTTPException(status_code=404, detail="Pago no encontrado")
    return db_pago

@router.get("/usuario/{id_usuario}", response_model=List[PagoResponse])
def read_pagos_by_usuario(id_usuario: int, db: Session = Depends(get_db)):
    pagos = crud_pago.get_pagos_by_usuario(db, usuario_id=id_usuario)
    return pagos

@router.get("/periodo/{anio}/{mes}", response_model=List[PagoResponse])
def read_pagos_by_periodo(anio: int, mes: int, db: Session = Depends(get_db)):
    if not 1 <= mes <= 12:
        raise HTTPException(status_code=400, detail="El mes debe estar entre 1 y 12")
    if anio < 2020:
        raise HTTPException(status_code=400, detail="El año debe ser mayor o igual a 2020")
    pagos = crud_pago.get_pagos_by_periodo(db, anio=anio, mes=mes)
    return pagos

@router.get("/suscripcion/{id_suscripcion}", response_model=List[PagoResponse])
def read_pagos_by_suscripcion(id_suscripcion: int, db: Session = Depends(get_db)):
    pagos = crud_pago.get_pagos_by_suscripcion(db, suscripcion_id=id_suscripcion)
    return pagos

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
