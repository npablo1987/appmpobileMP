from pydantic import BaseModel
from typing import Optional
from datetime import datetime
from decimal import Decimal

class ServicioCreate(BaseModel):
    nombre_servicio: str
    descripcion: Optional[str] = None
    costo_mensual: Decimal
    activo: bool = True

class ServicioUpdate(BaseModel):
    nombre_servicio: Optional[str] = None
    descripcion: Optional[str] = None
    costo_mensual: Optional[Decimal] = None
    activo: Optional[bool] = None

class ServicioResponse(BaseModel):
    id_servicio: int
    nombre_servicio: str
    descripcion: Optional[str] = None
    costo_mensual: Decimal
    activo: bool
    fecha_creacion: datetime

    class Config:
        from_attributes = True
