from sqlalchemy import Column, Integer, String, Text, TIMESTAMP, ForeignKey
from sqlalchemy.sql import func
from app.database import Base

class Notificacion(Base):
    __tablename__ = "notificacion"

    id_notificacion = Column(Integer, primary_key=True, index=True)
    id_usuario = Column(Integer, ForeignKey("usuario.id_usuario"), nullable=False, index=True)
    tipo_notificacion = Column(String(20), nullable=False)
    destino = Column(String(200), nullable=False)
    asunto = Column(String(200))
    mensaje = Column(Text, nullable=False)
    estado_envio = Column(String(20), nullable=False, default='PENDIENTE')
    fecha_envio = Column(TIMESTAMP, nullable=False, server_default=func.current_timestamp())
    observacion = Column(Text)
