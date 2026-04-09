import logging
from sqlalchemy.orm import Session
from app.schemas.notificacion import (
    NotificacionEmailCreate,
    NotificacionSMSCreate,
    NotificacionInternaCreate,
    NotificacionCreate
)
from app.crud import notificacion as crud_notificacion

logger = logging.getLogger(__name__)

def enviar_email_simulado(db: Session, notificacion: NotificacionEmailCreate):
    logger.info(f"Simulando envío de EMAIL a {notificacion.destino}")
    logger.info(f"Asunto: {notificacion.asunto}")
    logger.info(f"Mensaje: {notificacion.mensaje}")
    
    notif_create = NotificacionCreate(
        id_usuario=notificacion.id_usuario,
        tipo_notificacion='EMAIL',
        destino=notificacion.destino,
        asunto=notificacion.asunto,
        mensaje=notificacion.mensaje,
        estado_envio='ENVIADA',
        observacion='Email enviado de forma simulada'
    )
    
    return crud_notificacion.create_notificacion(db, notif_create)

def enviar_sms_simulado(db: Session, notificacion: NotificacionSMSCreate):
    logger.info(f"Simulando envío de SMS a {notificacion.destino}")
    logger.info(f"Mensaje: {notificacion.mensaje}")
    
    notif_create = NotificacionCreate(
        id_usuario=notificacion.id_usuario,
        tipo_notificacion='SMS',
        destino=notificacion.destino,
        asunto=None,
        mensaje=notificacion.mensaje,
        estado_envio='ENVIADA',
        observacion='SMS enviado de forma simulada'
    )
    
    return crud_notificacion.create_notificacion(db, notif_create)

def enviar_notificacion_interna(db: Session, notificacion: NotificacionInternaCreate):
    logger.info(f"Creando notificación interna para usuario {notificacion.id_usuario}")
    logger.info(f"Asunto: {notificacion.asunto}")
    logger.info(f"Mensaje: {notificacion.mensaje}")
    
    notif_create = NotificacionCreate(
        id_usuario=notificacion.id_usuario,
        tipo_notificacion='INTERNA',
        destino='SISTEMA',
        asunto=notificacion.asunto,
        mensaje=notificacion.mensaje,
        estado_envio='ENVIADA',
        observacion='Notificación interna del sistema'
    )
    
    return crud_notificacion.create_notificacion(db, notif_create)
