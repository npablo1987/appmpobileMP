from sqlalchemy import Column, Integer, String, Numeric, ForeignKey
from app.database import Base

class DetalleFactura(Base):
    __tablename__ = "detalle_factura"

    id_detalle_factura = Column(Integer, primary_key=True, index=True)
    id_factura = Column(Integer, ForeignKey("factura.id_factura"), nullable=False)
    tipo_item = Column(String(20), nullable=False)
    descripcion_item = Column(String(200), nullable=False)
    cantidad = Column(Integer, nullable=False, default=1)
    precio_unitario = Column(Numeric(12, 2), nullable=False)
    subtotal_item = Column(Numeric(12, 2), nullable=False)
