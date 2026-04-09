from pydantic import BaseModel
from typing import Optional

class MetodoPagoCreate(BaseModel):
    nombre_metodo: str
    descripcion: Optional[str] = None
    activo: bool = True

class MetodoPagoUpdate(BaseModel):
    nombre_metodo: Optional[str] = None
    descripcion: Optional[str] = None
    activo: Optional[bool] = None

class MetodoPagoResponse(BaseModel):
    id_metodo_pago: int
    nombre_metodo: str
    descripcion: Optional[str] = None
    activo: bool

    class Config:
        from_attributes = True
