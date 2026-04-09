from sqlalchemy.orm import Session
from app.models.servicio import Servicio
from app.schemas.servicio import ServicioCreate, ServicioUpdate
from typing import List, Optional

def get_servicios(db: Session, skip: int = 0, limit: int = 100) -> List[Servicio]:
    return db.query(Servicio).offset(skip).limit(limit).all()

def get_servicio(db: Session, servicio_id: int) -> Optional[Servicio]:
    return db.query(Servicio).filter(Servicio.id_servicio == servicio_id).first()

def get_servicio_by_nombre(db: Session, nombre_servicio: str) -> Optional[Servicio]:
    return db.query(Servicio).filter(Servicio.nombre_servicio == nombre_servicio).first()

def create_servicio(db: Session, servicio: ServicioCreate) -> Servicio:
    db_servicio = Servicio(**servicio.model_dump())
    db.add(db_servicio)
    db.commit()
    db.refresh(db_servicio)
    return db_servicio

def update_servicio(db: Session, servicio_id: int, servicio: ServicioUpdate) -> Optional[Servicio]:
    db_servicio = get_servicio(db, servicio_id)
    if db_servicio:
        update_data = servicio.model_dump(exclude_unset=True)
        for key, value in update_data.items():
            setattr(db_servicio, key, value)
        db.commit()
        db.refresh(db_servicio)
    return db_servicio

def delete_servicio(db: Session, servicio_id: int) -> bool:
    db_servicio = get_servicio(db, servicio_id)
    if db_servicio:
        db.delete(db_servicio)
        db.commit()
        return True
    return False
