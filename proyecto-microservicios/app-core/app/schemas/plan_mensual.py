from pydantic import BaseModel
from typing import Optional
from datetime import datetime
from decimal import Decimal

class PlanMensualCreate(BaseModel):
    nombre_plan: str
    descripcion: Optional[str] = None
    precio_mensual: Decimal
    limite_usuarios: int = 1
    activo: bool = True

class PlanMensualUpdate(BaseModel):
    nombre_plan: Optional[str] = None
    descripcion: Optional[str] = None
    precio_mensual: Optional[Decimal] = None
    limite_usuarios: Optional[int] = None
    activo: Optional[bool] = None

class PlanMensualResponse(BaseModel):
    id_plan: int
    nombre_plan: str
    descripcion: Optional[str] = None
    precio_mensual: Decimal
    limite_usuarios: int
    activo: bool
    fecha_creacion: datetime

    class Config:
        from_attributes = True
