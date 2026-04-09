from pydantic import BaseModel
from typing import Optional
from datetime import date, datetime

class SuscripcionCreate(BaseModel):
    id_usuario: int
    id_plan: int
    fecha_inicio: date
    fecha_fin: Optional[date] = None
    estado_suscripcion: str = 'ACTIVA'
    renovacion_automatica: bool = True

class SuscripcionUpdate(BaseModel):
    id_plan: Optional[int] = None
    fecha_inicio: Optional[date] = None
    fecha_fin: Optional[date] = None
    estado_suscripcion: Optional[str] = None
    renovacion_automatica: Optional[bool] = None

class SuscripcionResponse(BaseModel):
    id_suscripcion: int
    id_usuario: int
    id_plan: int
    fecha_inicio: date
    fecha_fin: Optional[date] = None
    estado_suscripcion: str
    renovacion_automatica: bool
    fecha_creacion: datetime

    class Config:
        from_attributes = True
