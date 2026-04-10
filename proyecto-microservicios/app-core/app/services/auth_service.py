import hashlib
import logging
from datetime import date, datetime
from decimal import Decimal
from typing import Any, Tuple

from pydantic import EmailStr, TypeAdapter, ValidationError
from sqlalchemy import text
from sqlalchemy.orm import Session

from app.crud import usuario as crud_usuario
from app.schemas.auth import HomeUserData, LoginRequest, LoginUserData

logger = logging.getLogger(__name__)

EMAIL_ADAPTER = TypeAdapter(EmailStr)
ESTADOS_NO_ACTIVOS = {"SUSPENDIDA", "BLOQUEADA", "ELIMINADA"}


def _serialize_dates(obj: Any) -> Any:
    """Convierte objetos date, datetime y Decimal a tipos serializables."""
    if isinstance(obj, (date, datetime)):
        return obj.isoformat()
    elif isinstance(obj, Decimal):
        return float(obj)
    elif isinstance(obj, dict):
        return {k: _serialize_dates(v) for k, v in obj.items()}
    elif isinstance(obj, list):
        return [_serialize_dates(item) for item in obj]
    return obj


def _normalizar_correo(correo: str) -> str:
    return correo.strip().lower()


def _verify_password(raw_password: str, stored_hash: str) -> bool:
    """
    Verifica contraseña soportando hash SHA256 o texto plano legado.
    """
    if not stored_hash:
        return False

    sha256_password = hashlib.sha256(raw_password.encode("utf-8")).hexdigest()
    return stored_hash == raw_password or stored_hash == sha256_password


def authenticate_user(db: Session, payload: LoginRequest) -> Tuple[int, dict]:
    correo = (payload.correo or "").strip()
    clave = (payload.clave or "").strip()

    logger.info("Intento de login recibido", extra={"correo": correo})

    if not correo or not clave:
        logger.warning("Login fallido por datos faltantes", extra={"correo": correo})
        return 400, {"success": False, "message": "Debe ingresar correo y clave"}

    try:
        correo_normalizado = _normalizar_correo(correo)
        EMAIL_ADAPTER.validate_python(correo_normalizado)
    except ValidationError:
        logger.warning("Login fallido por correo inválido", extra={"correo": correo})
        return 400, {"success": False, "message": "Correo con formato inválido"}

    db_usuario = crud_usuario.get_usuario_by_correo(db, correo=correo_normalizado)
    if not db_usuario:
        logger.warning("Login fallido: usuario no encontrado", extra={"correo": correo_normalizado})
        return 404, {"success": False, "message": "Usuario no encontrado"}

    logger.info("Usuario encontrado para login", extra={"id_usuario": db_usuario.id_usuario})

    estado = (db_usuario.estado_cuenta or "").upper()
    if estado != "ACTIVA" or estado in ESTADOS_NO_ACTIVOS:
        logger.warning(
            "Login bloqueado por cuenta no activa",
            extra={"id_usuario": db_usuario.id_usuario, "estado_cuenta": estado},
        )
        return 403, {"success": False, "message": "La cuenta no se encuentra activa"}

    if not _verify_password(clave, db_usuario.clave_hash):
        logger.warning(
            "Login fallido por credenciales inválidas",
            extra={"id_usuario": db_usuario.id_usuario},
        )
        return 401, {"success": False, "message": "Credenciales inválidas"}

    response_data = LoginUserData(
        id_usuario=db_usuario.id_usuario,
        rut=db_usuario.rut,
        nombres=db_usuario.nombres,
        apellido_paterno=db_usuario.apellido_paterno,
        apellido_materno=db_usuario.apellido_materno,
        correo=db_usuario.correo,
        telefono=db_usuario.telefono,
        ciudad=db_usuario.ciudad,
        estado_cuenta=db_usuario.estado_cuenta,
    ).model_dump()

    logger.info("Login exitoso", extra={"id_usuario": db_usuario.id_usuario})
    return 200, {
        "success": True,
        "message": "Inicio de sesión exitoso",
        "data": response_data,
    }


def get_home_user_data(db: Session, id_usuario: int) -> Tuple[int, dict]:
    logger.info("Solicitud de datos Home recibida", extra={"id_usuario": id_usuario})

    if id_usuario <= 0:
        logger.warning("Id usuario inválido en Home", extra={"id_usuario": id_usuario})
        return 400, {"success": False, "message": "Id de usuario inválido"}

    db_usuario = crud_usuario.get_usuario(db, usuario_id=id_usuario)
    if not db_usuario:
        logger.warning("Usuario no encontrado en Home", extra={"id_usuario": id_usuario})
        return 404, {"success": False, "message": "Usuario no encontrado"}

    if (db_usuario.estado_cuenta or "").upper() != "ACTIVA":
        logger.warning(
            "Solicitud Home rechazada por cuenta no activa",
            extra={"id_usuario": id_usuario, "estado_cuenta": db_usuario.estado_cuenta},
        )
        return 403, {"success": False, "message": "La cuenta no se encuentra activa"}

    nombre_completo = " ".join(
        item.strip()
        for item in [db_usuario.nombres, db_usuario.apellido_paterno, db_usuario.apellido_materno or ""]
        if item and item.strip()
    )

    data = HomeUserData(
        id_usuario=db_usuario.id_usuario,
        rut=db_usuario.rut,
        nombre_completo=nombre_completo,
        correo=db_usuario.correo,
        telefono=db_usuario.telefono,
        direccion=db_usuario.direccion,
        ciudad=db_usuario.ciudad,
        estado_cuenta=db_usuario.estado_cuenta,
        fecha_registro=db_usuario.fecha_registro,
    ).model_dump(mode="json")

    logger.info("Datos Home enviados correctamente", extra={"id_usuario": id_usuario})
    return 200, {
        "success": True,
        "message": "Datos del usuario obtenidos correctamente",
        "data": data,
    }


def get_billing_overview(db: Session, id_usuario: int) -> Tuple[int, dict]:
    logger.info("Inicio carga resumen de suscripciones", extra={"id_usuario": id_usuario})

    if id_usuario <= 0:
        logger.warning("Id usuario inválido para resumen", extra={"id_usuario": id_usuario})
        return 400, {"success": False, "message": "Id de usuario inválido"}

    db_usuario = crud_usuario.get_usuario(db, usuario_id=id_usuario)
    if not db_usuario:
        logger.warning("Usuario no encontrado en resumen", extra={"id_usuario": id_usuario})
        return 404, {"success": False, "message": "Usuario no encontrado"}

    if (db_usuario.estado_cuenta or "").upper() != "ACTIVA":
        logger.warning(
            "Solicitud resumen rechazada por cuenta no activa",
            extra={"id_usuario": id_usuario, "estado_cuenta": db_usuario.estado_cuenta},
        )
        return 403, {"success": False, "message": "Debe iniciar sesión nuevamente"}

    logger.info("Consultando suscripciones del usuario", extra={"id_usuario": id_usuario})
    suscripciones_query = text(
        """
        SELECT
            s.id_suscripcion,
            s.id_plan,
            s.fecha_inicio,
            s.fecha_fin,
            s.estado_suscripcion,
            s.renovacion_automatica,
            p.nombre_plan,
            p.descripcion,
            p.precio_mensual,
            p.limite_usuarios
        FROM suscripcion s
        INNER JOIN plan_mensual p ON p.id_plan = s.id_plan
        WHERE s.id_usuario = :id_usuario
        ORDER BY s.fecha_inicio DESC
        """
    )
    suscripciones_rows = db.execute(suscripciones_query, {"id_usuario": id_usuario}).mappings().all()
    suscripciones = [dict(row) for row in suscripciones_rows]

    logger.info("Consultando servicios adicionales del usuario", extra={"id_usuario": id_usuario})
    servicios_query = text(
        """
        SELECT
            us.id_usuario_servicio,
            us.id_servicio,
            us.fecha_contratacion,
            us.fecha_termino,
            us.estado,
            us.precio_pactado,
            s.nombre_servicio,
            s.descripcion,
            s.costo_mensual
        FROM usuario_servicio us
        INNER JOIN servicio s ON s.id_servicio = us.id_servicio
        WHERE us.id_usuario = :id_usuario
        ORDER BY us.fecha_contratacion DESC
        """
    )
    servicios_rows = db.execute(servicios_query, {"id_usuario": id_usuario}).mappings().all()
    servicios = [dict(row) for row in servicios_rows]

    logger.info("Consultando factura vigente del usuario", extra={"id_usuario": id_usuario})
    factura_query = text(
        """
        SELECT
            f.id_factura,
            f.numero_factura,
            f.fecha_emision,
            f.subtotal,
            f.impuesto,
            f.total,
            f.estado_factura,
            p.id_pago,
            p.periodo_anio,
            p.periodo_mes,
            p.estado_pago
        FROM factura f
        INNER JOIN pago p ON p.id_pago = f.id_pago
        WHERE p.id_usuario = :id_usuario
        ORDER BY f.fecha_emision DESC, f.id_factura DESC
        LIMIT 1
        """
    )
    factura_row = db.execute(factura_query, {"id_usuario": id_usuario}).mappings().first()
    factura_data = dict(factura_row) if factura_row else None

    detalle_items = []
    if factura_data:
        logger.info(
            "Consultando detalle de factura del usuario",
            extra={"id_usuario": id_usuario, "id_factura": factura_data["id_factura"]},
        )
        detalle_query = text(
            """
            SELECT
                id_detalle_factura,
                id_factura,
                tipo_item,
                descripcion_item,
                cantidad,
                precio_unitario,
                subtotal_item
            FROM detalle_factura
            WHERE id_factura = :id_factura
            ORDER BY id_detalle_factura ASC
            """
        )
        detalle_rows = db.execute(
            detalle_query,
            {"id_factura": factura_data["id_factura"]},
        ).mappings().all()
        detalle_items = [dict(row) for row in detalle_rows]

    logger.info(
        "Datos de resumen cargados exitosamente",
        extra={
            "id_usuario": id_usuario,
            "cantidad_suscripciones": len(suscripciones),
            "cantidad_servicios": len(servicios),
            "con_factura": factura_data is not None,
        },
    )
    
    response_data = {
        "id_usuario": id_usuario,
        "estado_cuenta": db_usuario.estado_cuenta,
        "suscripciones": suscripciones,
        "servicios_adicionales": servicios,
        "factura_actual": factura_data,
        "detalle_factura": detalle_items,
    }
    
    return 200, {
        "success": True,
        "message": "Resumen de suscripciones obtenido correctamente",
        "data": _serialize_dates(response_data),
    }
