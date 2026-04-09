from sqlalchemy import Column, Integer, String, Date, TIMESTAMP, Boolean, ForeignKey
from sqlalchemy.sql import func
from app.database import Base

class Suscripcion(Base):
    __tablename__ = "suscripcion"

    id_suscripcion = Column(Integer, primary_key=True, index=True)
    id_usuario = Column(Integer, ForeignKey("usuario.id_usuario"), nullable=False, index=True)
    id_plan = Column(Integer, ForeignKey("plan_mensual.id_plan"), nullable=False)
    fecha_inicio = Column(Date, nullable=False)
    fecha_fin = Column(Date)
    estado_suscripcion = Column(String(20), nullable=False, default='ACTIVA')
    renovacion_automatica = Column(Boolean, nullable=False, default=True)
    fecha_creacion = Column(TIMESTAMP, nullable=False, server_default=func.current_timestamp())
