from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from app.database import get_db
from app.schemas.servicio import ServicioCreate, ServicioUpdate, ServicioResponse
from app.crud import servicio as crud_servicio

router = APIRouter(prefix="/servicios", tags=["servicios"])

@router.post("/", response_model=ServicioResponse, status_code=status.HTTP_201_CREATED)
def create_servicio(servicio: ServicioCreate, db: Session = Depends(get_db)):
    db_servicio = crud_servicio.get_servicio_by_nombre(db, nombre_servicio=servicio.nombre_servicio)
    if db_servicio:
        raise HTTPException(status_code=400, detail="El nombre del servicio ya existe")
    return crud_servicio.create_servicio(db=db, servicio=servicio)

@router.get("/", response_model=List[ServicioResponse])
def read_servicios(skip: int = 0, limit: int = 100, db: Session = Depends(get_db)):
    servicios = crud_servicio.get_servicios(db, skip=skip, limit=limit)
    return servicios

@router.get("/{id}", response_model=ServicioResponse)
def read_servicio(id: int, db: Session = Depends(get_db)):
    db_servicio = crud_servicio.get_servicio(db, servicio_id=id)
    if db_servicio is None:
        raise HTTPException(status_code=404, detail="Servicio no encontrado")
    return db_servicio

@router.put("/{id}", response_model=ServicioResponse)
def update_servicio(id: int, servicio: ServicioUpdate, db: Session = Depends(get_db)):
    db_servicio = crud_servicio.update_servicio(db, servicio_id=id, servicio=servicio)
    if db_servicio is None:
        raise HTTPException(status_code=404, detail="Servicio no encontrado")
    return db_servicio

@router.delete("/{id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_servicio(id: int, db: Session = Depends(get_db)):
    success = crud_servicio.delete_servicio(db, servicio_id=id)
    if not success:
        raise HTTPException(status_code=404, detail="Servicio no encontrado")
    return None
