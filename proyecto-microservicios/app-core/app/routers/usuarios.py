from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from app.database import get_db
from app.schemas.usuario import UsuarioCreate, UsuarioUpdate, UsuarioResponse
from app.crud import usuario as crud_usuario

router = APIRouter(prefix="/usuarios", tags=["usuarios"])

@router.post("/", response_model=UsuarioResponse, status_code=status.HTTP_201_CREATED)
def create_usuario(usuario: UsuarioCreate, db: Session = Depends(get_db)):
    db_usuario = crud_usuario.get_usuario_by_correo(db, correo=usuario.correo)
    if db_usuario:
        raise HTTPException(status_code=400, detail="El correo ya está registrado")
    if usuario.rut:
        db_usuario = crud_usuario.get_usuario_by_rut(db, rut=usuario.rut)
        if db_usuario:
            raise HTTPException(status_code=400, detail="El RUT ya está registrado")
    return crud_usuario.create_usuario(db=db, usuario=usuario)

@router.get("/", response_model=List[UsuarioResponse])
def read_usuarios(skip: int = 0, limit: int = 100, db: Session = Depends(get_db)):
    usuarios = crud_usuario.get_usuarios(db, skip=skip, limit=limit)
    return usuarios

@router.get("/{id}", response_model=UsuarioResponse)
def read_usuario(id: int, db: Session = Depends(get_db)):
    db_usuario = crud_usuario.get_usuario(db, usuario_id=id)
    if db_usuario is None:
        raise HTTPException(status_code=404, detail="Usuario no encontrado")
    return db_usuario

@router.put("/{id}", response_model=UsuarioResponse)
def update_usuario(id: int, usuario: UsuarioUpdate, db: Session = Depends(get_db)):
    db_usuario = crud_usuario.update_usuario(db, usuario_id=id, usuario=usuario)
    if db_usuario is None:
        raise HTTPException(status_code=404, detail="Usuario no encontrado")
    return db_usuario

@router.delete("/{id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_usuario(id: int, db: Session = Depends(get_db)):
    success = crud_usuario.delete_usuario(db, usuario_id=id)
    if not success:
        raise HTTPException(status_code=404, detail="Usuario no encontrado")
    return None
