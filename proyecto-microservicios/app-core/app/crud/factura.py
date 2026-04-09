from sqlalchemy.orm import Session
from app.models.factura import Factura
from app.schemas.factura import FacturaCreate, FacturaUpdate
from typing import List, Optional

def get_facturas(db: Session, skip: int = 0, limit: int = 100) -> List[Factura]:
    return db.query(Factura).offset(skip).limit(limit).all()

def get_factura(db: Session, factura_id: int) -> Optional[Factura]:
    return db.query(Factura).filter(Factura.id_factura == factura_id).first()

def get_facturas_by_pago(db: Session, pago_id: int) -> List[Factura]:
    return db.query(Factura).filter(Factura.id_pago == pago_id).all()

def get_factura_by_numero(db: Session, numero_factura: str) -> Optional[Factura]:
    return db.query(Factura).filter(Factura.numero_factura == numero_factura).first()

def create_factura(db: Session, factura: FacturaCreate) -> Factura:
    db_factura = Factura(**factura.model_dump())
    db.add(db_factura)
    db.commit()
    db.refresh(db_factura)
    return db_factura

def update_factura(db: Session, factura_id: int, factura: FacturaUpdate) -> Optional[Factura]:
    db_factura = get_factura(db, factura_id)
    if db_factura:
        update_data = factura.model_dump(exclude_unset=True)
        for key, value in update_data.items():
            setattr(db_factura, key, value)
        db.commit()
        db.refresh(db_factura)
    return db_factura

def delete_factura(db: Session, factura_id: int) -> bool:
    db_factura = get_factura(db, factura_id)
    if db_factura:
        db.delete(db_factura)
        db.commit()
        return True
    return False
