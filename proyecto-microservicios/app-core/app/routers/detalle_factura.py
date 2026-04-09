from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from app.database import get_db
from app.schemas.detalle_factura import DetalleFacturaCreate, DetalleFacturaUpdate, DetalleFacturaResponse
from app.crud import detalle_factura as crud_detalle

router = APIRouter(prefix="/detalle-factura", tags=["detalle-factura"])

@router.post("/", response_model=DetalleFacturaResponse, status_code=status.HTTP_201_CREATED)
def create_detalle_factura(detalle: DetalleFacturaCreate, db: Session = Depends(get_db)):
    return crud_detalle.create_detalle_factura(db=db, detalle=detalle)

@router.get("/", response_model=List[DetalleFacturaResponse])
def read_detalles_factura(skip: int = 0, limit: int = 100, db: Session = Depends(get_db)):
    detalles = crud_detalle.get_detalles_factura(db, skip=skip, limit=limit)
    return detalles

@router.get("/{id}", response_model=DetalleFacturaResponse)
def read_detalle_factura(id: int, db: Session = Depends(get_db)):
    db_detalle = crud_detalle.get_detalle_factura(db, detalle_id=id)
    if db_detalle is None:
        raise HTTPException(status_code=404, detail="Detalle de factura no encontrado")
    return db_detalle

@router.get("/factura/{id_factura}", response_model=List[DetalleFacturaResponse])
def read_detalles_by_factura(id_factura: int, db: Session = Depends(get_db)):
    detalles = crud_detalle.get_detalles_by_factura(db, factura_id=id_factura)
    return detalles

@router.put("/{id}", response_model=DetalleFacturaResponse)
def update_detalle_factura(id: int, detalle: DetalleFacturaUpdate, db: Session = Depends(get_db)):
    db_detalle = crud_detalle.update_detalle_factura(db, detalle_id=id, detalle=detalle)
    if db_detalle is None:
        raise HTTPException(status_code=404, detail="Detalle de factura no encontrado")
    return db_detalle

@router.delete("/{id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_detalle_factura(id: int, db: Session = Depends(get_db)):
    success = crud_detalle.delete_detalle_factura(db, detalle_id=id)
    if not success:
        raise HTTPException(status_code=404, detail="Detalle de factura no encontrado")
    return None
