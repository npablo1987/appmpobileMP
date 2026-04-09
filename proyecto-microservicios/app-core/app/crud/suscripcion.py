from sqlalchemy.orm import Session
from app.models.suscripcion import Suscripcion
from app.schemas.suscripcion import SuscripcionCreate, SuscripcionUpdate
from typing import List, Optional

def get_suscripciones(db: Session, skip: int = 0, limit: int = 100) -> List[Suscripcion]:
    return db.query(Suscripcion).offset(skip).limit(limit).all()

def get_suscripcion(db: Session, suscripcion_id: int) -> Optional[Suscripcion]:
    return db.query(Suscripcion).filter(Suscripcion.id_suscripcion == suscripcion_id).first()

def get_suscripciones_by_usuario(db: Session, usuario_id: int) -> List[Suscripcion]:
    return db.query(Suscripcion).filter(Suscripcion.id_usuario == usuario_id).all()

def create_suscripcion(db: Session, suscripcion: SuscripcionCreate) -> Suscripcion:
    db_suscripcion = Suscripcion(**suscripcion.model_dump())
    db.add(db_suscripcion)
    db.commit()
    db.refresh(db_suscripcion)
    return db_suscripcion

def update_suscripcion(db: Session, suscripcion_id: int, suscripcion: SuscripcionUpdate) -> Optional[Suscripcion]:
    db_suscripcion = get_suscripcion(db, suscripcion_id)
    if db_suscripcion:
        update_data = suscripcion.model_dump(exclude_unset=True)
        for key, value in update_data.items():
            setattr(db_suscripcion, key, value)
        db.commit()
        db.refresh(db_suscripcion)
    return db_suscripcion

def delete_suscripcion(db: Session, suscripcion_id: int) -> bool:
    db_suscripcion = get_suscripcion(db, suscripcion_id)
    if db_suscripcion:
        db.delete(db_suscripcion)
        db.commit()
        return True
    return False
