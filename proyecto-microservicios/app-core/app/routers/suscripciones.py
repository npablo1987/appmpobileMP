from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from app.database import get_db
from app.schemas.suscripcion import SuscripcionCreate, SuscripcionUpdate, SuscripcionResponse
from app.crud import suscripcion as crud_suscripcion

router = APIRouter(prefix="/suscripciones", tags=["suscripciones"])

@router.post("/", response_model=SuscripcionResponse, status_code=status.HTTP_201_CREATED)
def create_suscripcion(suscripcion: SuscripcionCreate, db: Session = Depends(get_db)):
    return crud_suscripcion.create_suscripcion(db=db, suscripcion=suscripcion)

@router.get("/", response_model=List[SuscripcionResponse])
def read_suscripciones(skip: int = 0, limit: int = 100, db: Session = Depends(get_db)):
    suscripciones = crud_suscripcion.get_suscripciones(db, skip=skip, limit=limit)
    return suscripciones

@router.get("/{id}", response_model=SuscripcionResponse)
def read_suscripcion(id: int, db: Session = Depends(get_db)):
    db_suscripcion = crud_suscripcion.get_suscripcion(db, suscripcion_id=id)
    if db_suscripcion is None:
        raise HTTPException(status_code=404, detail="Suscripción no encontrada")
    return db_suscripcion

@router.get("/usuario/{id_usuario}", response_model=List[SuscripcionResponse])
def read_suscripciones_by_usuario(id_usuario: int, db: Session = Depends(get_db)):
    suscripciones = crud_suscripcion.get_suscripciones_by_usuario(db, usuario_id=id_usuario)
    return suscripciones

@router.put("/{id}", response_model=SuscripcionResponse)
def update_suscripcion(id: int, suscripcion: SuscripcionUpdate, db: Session = Depends(get_db)):
    db_suscripcion = crud_suscripcion.update_suscripcion(db, suscripcion_id=id, suscripcion=suscripcion)
    if db_suscripcion is None:
        raise HTTPException(status_code=404, detail="Suscripción no encontrada")
    return db_suscripcion

@router.delete("/{id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_suscripcion(id: int, db: Session = Depends(get_db)):
    success = crud_suscripcion.delete_suscripcion(db, suscripcion_id=id)
    if not success:
        raise HTTPException(status_code=404, detail="Suscripción no encontrada")
    return None
