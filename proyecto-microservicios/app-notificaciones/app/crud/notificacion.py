from sqlalchemy.orm import Session
from app.models.notificacion import Notificacion
from app.schemas.notificacion import NotificacionCreate, NotificacionUpdate
from typing import List, Optional

def get_notificaciones(db: Session, skip: int = 0, limit: int = 100) -> List[Notificacion]:
    return db.query(Notificacion).offset(skip).limit(limit).all()

def get_notificacion(db: Session, notificacion_id: int) -> Optional[Notificacion]:
    return db.query(Notificacion).filter(Notificacion.id_notificacion == notificacion_id).first()

def get_notificaciones_by_usuario(db: Session, usuario_id: int) -> List[Notificacion]:
    return db.query(Notificacion).filter(Notificacion.id_usuario == usuario_id).all()

def get_notificaciones_by_tipo(db: Session, tipo: str) -> List[Notificacion]:
    return db.query(Notificacion).filter(Notificacion.tipo_notificacion == tipo).all()

def get_notificaciones_by_estado(db: Session, estado: str) -> List[Notificacion]:
    return db.query(Notificacion).filter(Notificacion.estado_envio == estado).all()

def create_notificacion(db: Session, notificacion: NotificacionCreate) -> Notificacion:
    db_notificacion = Notificacion(**notificacion.model_dump())
    db.add(db_notificacion)
    db.commit()
    db.refresh(db_notificacion)
    return db_notificacion

def update_notificacion(db: Session, notificacion_id: int, notificacion: NotificacionUpdate) -> Optional[Notificacion]:
    db_notificacion = get_notificacion(db, notificacion_id)
    if db_notificacion:
        update_data = notificacion.model_dump(exclude_unset=True)
        for key, value in update_data.items():
            setattr(db_notificacion, key, value)
        db.commit()
        db.refresh(db_notificacion)
    return db_notificacion

def update_estado_notificacion(db: Session, notificacion_id: int, estado: str) -> Optional[Notificacion]:
    db_notificacion = get_notificacion(db, notificacion_id)
    if db_notificacion:
        db_notificacion.estado_envio = estado
        db.commit()
        db.refresh(db_notificacion)
    return db_notificacion

def delete_notificacion(db: Session, notificacion_id: int) -> bool:
    db_notificacion = get_notificacion(db, notificacion_id)
    if db_notificacion:
        db.delete(db_notificacion)
        db.commit()
        return True
    return False
