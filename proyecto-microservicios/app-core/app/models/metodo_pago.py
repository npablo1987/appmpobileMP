from sqlalchemy import Column, Integer, String, Boolean
from app.database import Base

class MetodoPago(Base):
    __tablename__ = "metodo_pago"

    id_metodo_pago = Column(Integer, primary_key=True, index=True)
    nombre_metodo = Column(String(50), nullable=False, unique=True)
    descripcion = Column(String(150))
    activo = Column(Boolean, nullable=False, default=True)
