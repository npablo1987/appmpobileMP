from sqlalchemy.orm import Session
from app.models.metodo_pago import MetodoPago
from app.schemas.metodo_pago import MetodoPagoCreate, MetodoPagoUpdate
from typing import List, Optional

def get_metodos_pago(db: Session, skip: int = 0, limit: int = 100) -> List[MetodoPago]:
    return db.query(MetodoPago).offset(skip).limit(limit).all()

def get_metodo_pago(db: Session, metodo_id: int) -> Optional[MetodoPago]:
    return db.query(MetodoPago).filter(MetodoPago.id_metodo_pago == metodo_id).first()

def get_metodo_pago_by_nombre(db: Session, nombre_metodo: str) -> Optional[MetodoPago]:
    return db.query(MetodoPago).filter(MetodoPago.nombre_metodo == nombre_metodo).first()

def create_metodo_pago(db: Session, metodo: MetodoPagoCreate) -> MetodoPago:
    db_metodo = MetodoPago(**metodo.model_dump())
    db.add(db_metodo)
    db.commit()
    db.refresh(db_metodo)
    return db_metodo

def update_metodo_pago(db: Session, metodo_id: int, metodo: MetodoPagoUpdate) -> Optional[MetodoPago]:
    db_metodo = get_metodo_pago(db, metodo_id)
    if db_metodo:
        update_data = metodo.model_dump(exclude_unset=True)
        for key, value in update_data.items():
            setattr(db_metodo, key, value)
        db.commit()
        db.refresh(db_metodo)
    return db_metodo

def delete_metodo_pago(db: Session, metodo_id: int) -> bool:
    db_metodo = get_metodo_pago(db, metodo_id)
    if db_metodo:
        db.delete(db_metodo)
        db.commit()
        return True
    return False
