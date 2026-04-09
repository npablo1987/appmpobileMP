from sqlalchemy import Column, Integer, String, Date, Numeric, ForeignKey
from app.database import Base

class UsuarioServicio(Base):
    __tablename__ = "usuario_servicio"

    id_usuario_servicio = Column(Integer, primary_key=True, index=True)
    id_usuario = Column(Integer, ForeignKey("usuario.id_usuario"), nullable=False, index=True)
    id_servicio = Column(Integer, ForeignKey("servicio.id_servicio"), nullable=False)
    fecha_contratacion = Column(Date, nullable=False)
    fecha_termino = Column(Date)
    estado = Column(String(20), nullable=False, default='ACTIVO')
    precio_pactado = Column(Numeric(12, 2), nullable=False)
