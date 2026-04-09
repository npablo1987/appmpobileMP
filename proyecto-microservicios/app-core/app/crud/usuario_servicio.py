from sqlalchemy.orm import Session
from app.models.usuario_servicio import UsuarioServicio
from app.schemas.usuario_servicio import UsuarioServicioCreate, UsuarioServicioUpdate
from typing import List, Optional

def get_usuarios_servicios(db: Session, skip: int = 0, limit: int = 100) -> List[UsuarioServicio]:
    return db.query(UsuarioServicio).offset(skip).limit(limit).all()

def get_usuario_servicio(db: Session, usuario_servicio_id: int) -> Optional[UsuarioServicio]:
    return db.query(UsuarioServicio).filter(UsuarioServicio.id_usuario_servicio == usuario_servicio_id).first()

def get_usuarios_servicios_by_usuario(db: Session, usuario_id: int) -> List[UsuarioServicio]:
    return db.query(UsuarioServicio).filter(UsuarioServicio.id_usuario == usuario_id).all()

def create_usuario_servicio(db: Session, usuario_servicio: UsuarioServicioCreate) -> UsuarioServicio:
    db_usuario_servicio = UsuarioServicio(**usuario_servicio.model_dump())
    db.add(db_usuario_servicio)
    db.commit()
    db.refresh(db_usuario_servicio)
    return db_usuario_servicio

def update_usuario_servicio(db: Session, usuario_servicio_id: int, usuario_servicio: UsuarioServicioUpdate) -> Optional[UsuarioServicio]:
    db_usuario_servicio = get_usuario_servicio(db, usuario_servicio_id)
    if db_usuario_servicio:
        update_data = usuario_servicio.model_dump(exclude_unset=True)
        for key, value in update_data.items():
            setattr(db_usuario_servicio, key, value)
        db.commit()
        db.refresh(db_usuario_servicio)
    return db_usuario_servicio

def delete_usuario_servicio(db: Session, usuario_servicio_id: int) -> bool:
    db_usuario_servicio = get_usuario_servicio(db, usuario_servicio_id)
    if db_usuario_servicio:
        db.delete(db_usuario_servicio)
        db.commit()
        return True
    return False
