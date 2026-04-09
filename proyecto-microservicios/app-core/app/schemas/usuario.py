from pydantic import BaseModel, EmailStr
from typing import Optional
from datetime import datetime

class UsuarioCreate(BaseModel):
    rut: Optional[str] = None
    nombres: str
    apellido_paterno: str
    apellido_materno: Optional[str] = None
    correo: EmailStr
    telefono: Optional[str] = None
    direccion: Optional[str] = None
    ciudad: Optional[str] = None
    estado_cuenta: str = 'ACTIVA'
    clave_hash: str
    observacion: Optional[str] = None

class UsuarioUpdate(BaseModel):
    rut: Optional[str] = None
    nombres: Optional[str] = None
    apellido_paterno: Optional[str] = None
    apellido_materno: Optional[str] = None
    correo: Optional[EmailStr] = None
    telefono: Optional[str] = None
    direccion: Optional[str] = None
    ciudad: Optional[str] = None
    estado_cuenta: Optional[str] = None
    clave_hash: Optional[str] = None
    observacion: Optional[str] = None

class UsuarioResponse(BaseModel):
    id_usuario: int
    rut: Optional[str] = None
    nombres: str
    apellido_paterno: str
    apellido_materno: Optional[str] = None
    correo: str
    telefono: Optional[str] = None
    direccion: Optional[str] = None
    ciudad: Optional[str] = None
    fecha_registro: datetime
    estado_cuenta: str
    observacion: Optional[str] = None

    class Config:
        from_attributes = True
