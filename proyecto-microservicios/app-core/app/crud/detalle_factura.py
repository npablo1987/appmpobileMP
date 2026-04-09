from sqlalchemy.orm import Session
from app.models.detalle_factura import DetalleFactura
from app.schemas.detalle_factura import DetalleFacturaCreate, DetalleFacturaUpdate
from typing import List, Optional

def get_detalles_factura(db: Session, skip: int = 0, limit: int = 100) -> List[DetalleFactura]:
    return db.query(DetalleFactura).offset(skip).limit(limit).all()

def get_detalle_factura(db: Session, detalle_id: int) -> Optional[DetalleFactura]:
    return db.query(DetalleFactura).filter(DetalleFactura.id_detalle_factura == detalle_id).first()

def get_detalles_by_factura(db: Session, factura_id: int) -> List[DetalleFactura]:
    return db.query(DetalleFactura).filter(DetalleFactura.id_factura == factura_id).all()

def create_detalle_factura(db: Session, detalle: DetalleFacturaCreate) -> DetalleFactura:
    db_detalle = DetalleFactura(**detalle.model_dump())
    db.add(db_detalle)
    db.commit()
    db.refresh(db_detalle)
    return db_detalle

def update_detalle_factura(db: Session, detalle_id: int, detalle: DetalleFacturaUpdate) -> Optional[DetalleFactura]:
    db_detalle = get_detalle_factura(db, detalle_id)
    if db_detalle:
        update_data = detalle.model_dump(exclude_unset=True)
        for key, value in update_data.items():
            setattr(db_detalle, key, value)
        db.commit()
        db.refresh(db_detalle)
    return db_detalle

def delete_detalle_factura(db: Session, detalle_id: int) -> bool:
    db_detalle = get_detalle_factura(db, detalle_id)
    if db_detalle:
        db.delete(db_detalle)
        db.commit()
        return True
    return False
