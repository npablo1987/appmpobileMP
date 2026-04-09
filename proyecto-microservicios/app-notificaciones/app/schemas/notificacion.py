from pydantic import BaseModel, EmailStr, field_validator
from typing import Optional
from datetime import datetime

class NotificacionCreate(BaseModel):
    id_usuario: int
    tipo_notificacion: str
    destino: str
    asunto: Optional[str] = None
    mensaje: str
    estado_envio: str = 'PENDIENTE'
    observacion: Optional[str] = None

    @field_validator('tipo_notificacion')
    @classmethod
    def validate_tipo(cls, v):
        tipos_validos = ['EMAIL', 'SMS', 'INTERNA']
        if v not in tipos_validos:
            raise ValueError(f'El tipo debe ser uno de: {", ".join(tipos_validos)}')
        return v

    @field_validator('estado_envio')
    @classmethod
    def validate_estado(cls, v):
        estados_validos = ['PENDIENTE', 'ENVIADA', 'ERROR']
        if v not in estados_validos:
            raise ValueError(f'El estado debe ser uno de: {", ".join(estados_validos)}')
        return v

class NotificacionEmailCreate(BaseModel):
    id_usuario: int
    destino: EmailStr
    asunto: str
    mensaje: str

class NotificacionSMSCreate(BaseModel):
    id_usuario: int
    destino: str
    mensaje: str

class NotificacionInternaCreate(BaseModel):
    id_usuario: int
    asunto: str
    mensaje: str

class NotificacionUpdate(BaseModel):
    tipo_notificacion: Optional[str] = None
    destino: Optional[str] = None
    asunto: Optional[str] = None
    mensaje: Optional[str] = None
    estado_envio: Optional[str] = None
    observacion: Optional[str] = None

class NotificacionEstadoUpdate(BaseModel):
    estado_envio: str

    @field_validator('estado_envio')
    @classmethod
    def validate_estado(cls, v):
        estados_validos = ['PENDIENTE', 'ENVIADA', 'ERROR']
        if v not in estados_validos:
            raise ValueError(f'El estado debe ser uno de: {", ".join(estados_validos)}')
        return v

class NotificacionResponse(BaseModel):
    id_notificacion: int
    id_usuario: int
    tipo_notificacion: str
    destino: str
    asunto: Optional[str] = None
    mensaje: str
    estado_envio: str
    fecha_envio: datetime
    observacion: Optional[str] = None

    class Config:
        from_attributes = True
