from sqlalchemy import Column, Integer, String, Text, TIMESTAMP, Boolean, Numeric
from sqlalchemy.sql import func
from app.database import Base

class PlanMensual(Base):
    __tablename__ = "plan_mensual"

    id_plan = Column(Integer, primary_key=True, index=True)
    nombre_plan = Column(String(100), nullable=False, unique=True)
    descripcion = Column(Text)
    precio_mensual = Column(Numeric(12, 2), nullable=False)
    limite_usuarios = Column(Integer, default=1)
    activo = Column(Boolean, nullable=False, default=True)
    fecha_creacion = Column(TIMESTAMP, nullable=False, server_default=func.current_timestamp())
