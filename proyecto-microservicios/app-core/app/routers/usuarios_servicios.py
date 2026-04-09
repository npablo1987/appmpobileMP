from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from app.database import get_db
from app.schemas.usuario_servicio import UsuarioServicioCreate, UsuarioServicioUpdate, UsuarioServicioResponse
from app.crud import usuario_servicio as crud_usuario_servicio

router = APIRouter(prefix="/usuarios-servicios", tags=["usuarios-servicios"])

@router.post("/", response_model=UsuarioServicioResponse, status_code=status.HTTP_201_CREATED)
def create_usuario_servicio(usuario_servicio: UsuarioServicioCreate, db: Session = Depends(get_db)):
    return crud_usuario_servicio.create_usuario_servicio(db=db, usuario_servicio=usuario_servicio)

@router.get("/", response_model=List[UsuarioServicioResponse])
def read_usuarios_servicios(skip: int = 0, limit: int = 100, db: Session = Depends(get_db)):
    usuarios_servicios = crud_usuario_servicio.get_usuarios_servicios(db, skip=skip, limit=limit)
    return usuarios_servicios

@router.get("/{id}", response_model=UsuarioServicioResponse)
def read_usuario_servicio(id: int, db: Session = Depends(get_db)):
    db_usuario_servicio = crud_usuario_servicio.get_usuario_servicio(db, usuario_servicio_id=id)
    if db_usuario_servicio is None:
        raise HTTPException(status_code=404, detail="Usuario-Servicio no encontrado")
    return db_usuario_servicio

@router.get("/usuario/{id_usuario}", response_model=List[UsuarioServicioResponse])
def read_usuarios_servicios_by_usuario(id_usuario: int, db: Session = Depends(get_db)):
    usuarios_servicios = crud_usuario_servicio.get_usuarios_servicios_by_usuario(db, usuario_id=id_usuario)
    return usuarios_servicios

@router.put("/{id}", response_model=UsuarioServicioResponse)
def update_usuario_servicio(id: int, usuario_servicio: UsuarioServicioUpdate, db: Session = Depends(get_db)):
    db_usuario_servicio = crud_usuario_servicio.update_usuario_servicio(db, usuario_servicio_id=id, usuario_servicio=usuario_servicio)
    if db_usuario_servicio is None:
        raise HTTPException(status_code=404, detail="Usuario-Servicio no encontrado")
    return db_usuario_servicio

@router.delete("/{id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_usuario_servicio(id: int, db: Session = Depends(get_db)):
    success = crud_usuario_servicio.delete_usuario_servicio(db, usuario_servicio_id=id)
    if not success:
        raise HTTPException(status_code=404, detail="Usuario-Servicio no encontrado")
    return None
