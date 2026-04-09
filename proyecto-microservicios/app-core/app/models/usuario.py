from sqlalchemy import Column, Integer, String, Text, TIMESTAMP
from sqlalchemy.sql import func
from app.database import Base

class Usuario(Base):
    __tablename__ = "usuario"

    id_usuario = Column(Integer, primary_key=True, index=True)
    rut = Column(String(15), unique=True, index=True)
    nombres = Column(String(100), nullable=False)
    apellido_paterno = Column(String(100), nullable=False)
    apellido_materno = Column(String(100))
    correo = Column(String(150), nullable=False, unique=True, index=True)
    telefono = Column(String(20))
    direccion = Column(String(200))
    ciudad = Column(String(100))
    fecha_registro = Column(TIMESTAMP, nullable=False, server_default=func.current_timestamp())
    estado_cuenta = Column(String(20), nullable=False, default='ACTIVA')
    clave_hash = Column(String(255), nullable=False)
    observacion = Column(Text)
