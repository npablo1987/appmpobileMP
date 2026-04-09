from pydantic import BaseModel
from typing import Optional
from decimal import Decimal

class DetalleFacturaCreate(BaseModel):
    id_factura: int
    tipo_item: str
    descripcion_item: str
    cantidad: int = 1
    precio_unitario: Decimal
    subtotal_item: Decimal

class DetalleFacturaUpdate(BaseModel):
    tipo_item: Optional[str] = None
    descripcion_item: Optional[str] = None
    cantidad: Optional[int] = None
    precio_unitario: Optional[Decimal] = None
    subtotal_item: Optional[Decimal] = None

class DetalleFacturaResponse(BaseModel):
    id_detalle_factura: int
    id_factura: int
    tipo_item: str
    descripcion_item: str
    cantidad: int
    precio_unitario: Decimal
    subtotal_item: Decimal

    class Config:
        from_attributes = True
