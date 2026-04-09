from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from app.database import get_db
from app.schemas.factura import FacturaCreate, FacturaUpdate, FacturaResponse
from app.crud import factura as crud_factura

router = APIRouter(prefix="/facturas", tags=["facturas"])

@router.post("/", response_model=FacturaResponse, status_code=status.HTTP_201_CREATED)
def create_factura(factura: FacturaCreate, db: Session = Depends(get_db)):
    db_factura = crud_factura.get_factura_by_numero(db, numero_factura=factura.numero_factura)
    if db_factura:
        raise HTTPException(status_code=400, detail="El número de factura ya existe")
    return crud_factura.create_factura(db=db, factura=factura)

@router.get("/", response_model=List[FacturaResponse])
def read_facturas(skip: int = 0, limit: int = 100, db: Session = Depends(get_db)):
    facturas = crud_factura.get_facturas(db, skip=skip, limit=limit)
    return facturas

@router.get("/{id}", response_model=FacturaResponse)
def read_factura(id: int, db: Session = Depends(get_db)):
    db_factura = crud_factura.get_factura(db, factura_id=id)
    if db_factura is None:
        raise HTTPException(status_code=404, detail="Factura no encontrada")
    return db_factura

@router.get("/pago/{id_pago}", response_model=List[FacturaResponse])
def read_facturas_by_pago(id_pago: int, db: Session = Depends(get_db)):
    facturas = crud_factura.get_facturas_by_pago(db, pago_id=id_pago)
    return facturas

@router.put("/{id}", response_model=FacturaResponse)
def update_factura(id: int, factura: FacturaUpdate, db: Session = Depends(get_db)):
    db_factura = crud_factura.update_factura(db, factura_id=id, factura=factura)
    if db_factura is None:
        raise HTTPException(status_code=404, detail="Factura no encontrada")
    return db_factura

@router.delete("/{id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_factura(id: int, db: Session = Depends(get_db)):
    success = crud_factura.delete_factura(db, factura_id=id)
    if not success:
        raise HTTPException(status_code=404, detail="Factura no encontrada")
    return None
