from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from app.database import get_db
from app.schemas.metodo_pago import MetodoPagoCreate, MetodoPagoUpdate, MetodoPagoResponse
from app.crud import metodo_pago as crud_metodo

router = APIRouter(prefix="/metodos-pago", tags=["metodos-pago"])

@router.post("/", response_model=MetodoPagoResponse, status_code=status.HTTP_201_CREATED)
def create_metodo_pago(metodo: MetodoPagoCreate, db: Session = Depends(get_db)):
    db_metodo = crud_metodo.get_metodo_pago_by_nombre(db, nombre_metodo=metodo.nombre_metodo)
    if db_metodo:
        raise HTTPException(status_code=400, detail="El nombre del método de pago ya existe")
    return crud_metodo.create_metodo_pago(db=db, metodo=metodo)

@router.get("/", response_model=List[MetodoPagoResponse])
def read_metodos_pago(skip: int = 0, limit: int = 100, db: Session = Depends(get_db)):
    metodos = crud_metodo.get_metodos_pago(db, skip=skip, limit=limit)
    return metodos

@router.get("/{id}", response_model=MetodoPagoResponse)
def read_metodo_pago(id: int, db: Session = Depends(get_db)):
    db_metodo = crud_metodo.get_metodo_pago(db, metodo_id=id)
    if db_metodo is None:
        raise HTTPException(status_code=404, detail="Método de pago no encontrado")
    return db_metodo

@router.put("/{id}", response_model=MetodoPagoResponse)
def update_metodo_pago(id: int, metodo: MetodoPagoUpdate, db: Session = Depends(get_db)):
    db_metodo = crud_metodo.update_metodo_pago(db, metodo_id=id, metodo=metodo)
    if db_metodo is None:
        raise HTTPException(status_code=404, detail="Método de pago no encontrado")
    return db_metodo

@router.delete("/{id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_metodo_pago(id: int, db: Session = Depends(get_db)):
    success = crud_metodo.delete_metodo_pago(db, metodo_id=id)
    if not success:
        raise HTTPException(status_code=404, detail="Método de pago no encontrado")
    return None
