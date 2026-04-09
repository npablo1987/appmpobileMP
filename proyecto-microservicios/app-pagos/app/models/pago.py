from sqlalchemy import Column, Integer, String, Text, TIMESTAMP, Numeric, ForeignKey
from sqlalchemy.sql import func
from app.database import Base

class Pago(Base):
    __tablename__ = "pago"

    id_pago = Column(Integer, primary_key=True, index=True)
    id_usuario = Column(Integer, ForeignKey("usuario.id_usuario"), nullable=False, index=True)
    id_suscripcion = Column(Integer, ForeignKey("suscripcion.id_suscripcion"))
    id_metodo_pago = Column(Integer, ForeignKey("metodo_pago.id_metodo_pago"), nullable=False)
    fecha_pago = Column(TIMESTAMP, nullable=False, server_default=func.current_timestamp())
    periodo_anio = Column(Integer, nullable=False)
    periodo_mes = Column(Integer, nullable=False)
    monto_total = Column(Numeric(12, 2), nullable=False)
    estado_pago = Column(String(20), nullable=False, default='PENDIENTE')
    codigo_transaccion = Column(String(100))
    observacion = Column(Text)
