from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from app.database import get_db
from app.schemas.notificacion import (
    NotificacionResponse,
    NotificacionEmailCreate,
    NotificacionSMSCreate,
    NotificacionInternaCreate,
    NotificacionEstadoUpdate
)
from app.crud import notificacion as crud_notificacion
from app.services import notificacion_service

router = APIRouter(prefix="/notificaciones", tags=["notificaciones"])

@router.post("/email", response_model=NotificacionResponse, status_code=status.HTTP_201_CREATED)
def enviar_email(notificacion: NotificacionEmailCreate, db: Session = Depends(get_db)):
    return notificacion_service.enviar_email_simulado(db, notificacion)

@router.post("/sms", response_model=NotificacionResponse, status_code=status.HTTP_201_CREATED)
def enviar_sms(notificacion: NotificacionSMSCreate, db: Session = Depends(get_db)):
    return notificacion_service.enviar_sms_simulado(db, notificacion)

@router.post("/interna", response_model=NotificacionResponse, status_code=status.HTTP_201_CREATED)
def enviar_interna(notificacion: NotificacionInternaCreate, db: Session = Depends(get_db)):
    return notificacion_service.enviar_notificacion_interna(db, notificacion)

@router.get("/", response_model=List[NotificacionResponse])
def read_notificaciones(skip: int = 0, limit: int = 100, db: Session = Depends(get_db)):
    notificaciones = crud_notificacion.get_notificaciones(db, skip=skip, limit=limit)
    return notificaciones

@router.get("/{id}", response_model=NotificacionResponse)
def read_notificacion(id: int, db: Session = Depends(get_db)):
    db_notificacion = crud_notificacion.get_notificacion(db, notificacion_id=id)
    if db_notificacion is None:
        raise HTTPException(status_code=404, detail="Notificación no encontrada")
    return db_notificacion

@router.get("/usuario/{id_usuario}", response_model=List[NotificacionResponse])
def read_notificaciones_by_usuario(id_usuario: int, db: Session = Depends(get_db)):
    notificaciones = crud_notificacion.get_notificaciones_by_usuario(db, usuario_id=id_usuario)
    return notificaciones

@router.patch("/{id}/estado", response_model=NotificacionResponse)
def update_estado_notificacion(id: int, estado_update: NotificacionEstadoUpdate, db: Session = Depends(get_db)):
    db_notificacion = crud_notificacion.update_estado_notificacion(db, notificacion_id=id, estado=estado_update.estado_envio)
    if db_notificacion is None:
        raise HTTPException(status_code=404, detail="Notificación no encontrada")
    return db_notificacion

@router.delete("/{id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_notificacion(id: int, db: Session = Depends(get_db)):
    success = crud_notificacion.delete_notificacion(db, notificacion_id=id)
    if not success:
        raise HTTPException(status_code=404, detail="Notificación no encontrada")
    return None
