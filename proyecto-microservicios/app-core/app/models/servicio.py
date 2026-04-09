from sqlalchemy import Column, Integer, String, Text, TIMESTAMP, Boolean, Numeric
from sqlalchemy.sql import func
from app.database import Base

class Servicio(Base):
    __tablename__ = "servicio"

    id_servicio = Column(Integer, primary_key=True, index=True)
    nombre_servicio = Column(String(120), nullable=False, unique=True)
    descripcion = Column(Text)
    costo_mensual = Column(Numeric(12, 2), nullable=False)
    activo = Column(Boolean, nullable=False, default=True)
    fecha_creacion = Column(TIMESTAMP, nullable=False, server_default=func.current_timestamp())
