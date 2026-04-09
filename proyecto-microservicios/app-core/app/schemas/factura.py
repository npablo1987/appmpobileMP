from pydantic import BaseModel
from typing import Optional
from datetime import date
from decimal import Decimal

class FacturaCreate(BaseModel):
    id_pago: int
    numero_factura: str
    fecha_emision: date
    subtotal: Decimal
    impuesto: Decimal = Decimal('0')
    total: Decimal
    estado_factura: str = 'EMITIDA'

class FacturaUpdate(BaseModel):
    numero_factura: Optional[str] = None
    fecha_emision: Optional[date] = None
    subtotal: Optional[Decimal] = None
    impuesto: Optional[Decimal] = None
    total: Optional[Decimal] = None
    estado_factura: Optional[str] = None

class FacturaResponse(BaseModel):
    id_factura: int
    id_pago: int
    numero_factura: str
    fecha_emision: date
    subtotal: Decimal
    impuesto: Decimal
    total: Decimal
    estado_factura: str

    class Config:
        from_attributes = True
