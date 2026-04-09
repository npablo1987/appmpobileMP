from datetime import datetime
from pydantic import BaseModel
from typing import Optional


class LoginRequest(BaseModel):
    correo: Optional[str] = None
    clave: Optional[str] = None


class LoginUserData(BaseModel):
    id_usuario: int
    rut: Optional[str] = None
    nombres: str
    apellido_paterno: str
    apellido_materno: Optional[str] = None
    correo: str
    telefono: Optional[str] = None
    ciudad: Optional[str] = None
    estado_cuenta: str


class HomeUserData(BaseModel):
    id_usuario: int
    rut: Optional[str] = None
    nombre_completo: str
    correo: str
    telefono: Optional[str] = None
    direccion: Optional[str] = None
    ciudad: Optional[str] = None
    estado_cuenta: str
    fecha_registro: datetime


class ApiResponse(BaseModel):
    success: bool
    message: str
    data: Optional[dict] = None
