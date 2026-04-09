from pydantic import BaseModel, field_validator
from typing import Optional
from datetime import datetime
from decimal import Decimal

class PagoCreate(BaseModel):
    id_usuario: int
    id_suscripcion: Optional[int] = None
    id_metodo_pago: int
    periodo_anio: int
    periodo_mes: int
    monto_total: Decimal
    estado_pago: str = 'PENDIENTE'
    codigo_transaccion: Optional[str] = None
    observacion: Optional[str] = None

    @field_validator('periodo_mes')
    @classmethod
    def validate_mes(cls, v):
        if not 1 <= v <= 12:
            raise ValueError('El mes debe estar entre 1 y 12')
        return v

    @field_validator('periodo_anio')
    @classmethod
    def validate_anio(cls, v):
        if v < 2020:
            raise ValueError('El año debe ser mayor o igual a 2020')
        return v

    @field_validator('monto_total')
    @classmethod
    def validate_monto(cls, v):
        if v < 0:
            raise ValueError('El monto debe ser mayor o igual a 0')
        return v

    @field_validator('estado_pago')
    @classmethod
    def validate_estado(cls, v):
        estados_validos = ['PENDIENTE', 'PAGADO', 'RECHAZADO', 'ANULADO']
        if v not in estados_validos:
            raise ValueError(f'El estado debe ser uno de: {", ".join(estados_validos)}')
        return v

class PagoUpdate(BaseModel):
    id_suscripcion: Optional[int] = None
    id_metodo_pago: Optional[int] = None
    periodo_anio: Optional[int] = None
    periodo_mes: Optional[int] = None
    monto_total: Optional[Decimal] = None
    estado_pago: Optional[str] = None
    codigo_transaccion: Optional[str] = None
    observacion: Optional[str] = None

class PagoEstadoUpdate(BaseModel):
    estado_pago: str

    @field_validator('estado_pago')
    @classmethod
    def validate_estado(cls, v):
        estados_validos = ['PENDIENTE', 'PAGADO', 'RECHAZADO', 'ANULADO']
        if v not in estados_validos:
            raise ValueError(f'El estado debe ser uno de: {", ".join(estados_validos)}')
        return v

class PagoResponse(BaseModel):
    id_pago: int
    id_usuario: int
    id_suscripcion: Optional[int] = None
    id_metodo_pago: int
    fecha_pago: datetime
    periodo_anio: int
    periodo_mes: int
    monto_total: Decimal
    estado_pago: str
    codigo_transaccion: Optional[str] = None
    observacion: Optional[str] = None

    class Config:
        from_attributes = True
