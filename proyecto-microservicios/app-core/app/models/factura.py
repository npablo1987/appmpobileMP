from sqlalchemy import Column, Integer, String, Date, Numeric, ForeignKey
from app.database import Base

class Factura(Base):
    __tablename__ = "factura"

    id_factura = Column(Integer, primary_key=True, index=True)
    id_pago = Column(Integer, ForeignKey("pago.id_pago"), nullable=False)
    numero_factura = Column(String(50), nullable=False, unique=True)
    fecha_emision = Column(Date, nullable=False)
    subtotal = Column(Numeric(12, 2), nullable=False)
    impuesto = Column(Numeric(12, 2), nullable=False, default=0)
    total = Column(Numeric(12, 2), nullable=False)
    estado_factura = Column(String(20), nullable=False, default='EMITIDA')
