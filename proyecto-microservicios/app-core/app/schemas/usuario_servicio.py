from pydantic import BaseModel
from typing import Optional
from datetime import date
from decimal import Decimal

class UsuarioServicioCreate(BaseModel):
    id_usuario: int
    id_servicio: int
    fecha_contratacion: date
    fecha_termino: Optional[date] = None
    estado: str = 'ACTIVO'
    precio_pactado: Decimal

class UsuarioServicioUpdate(BaseModel):
    fecha_contratacion: Optional[date] = None
    fecha_termino: Optional[date] = None
    estado: Optional[str] = None
    precio_pactado: Optional[Decimal] = None

class UsuarioServicioResponse(BaseModel):
    id_usuario_servicio: int
    id_usuario: int
    id_servicio: int
    fecha_contratacion: date
    fecha_termino: Optional[date] = None
    estado: str
    precio_pactado: Decimal

    class Config:
        from_attributes = True
