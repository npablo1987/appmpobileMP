import hashlib
import logging
from typing import Tuple

from pydantic import EmailStr, TypeAdapter, ValidationError
from sqlalchemy.orm import Session

from app.crud import usuario as crud_usuario
from app.schemas.auth import HomeUserData, LoginRequest, LoginUserData

logger = logging.getLogger(__name__)

EMAIL_ADAPTER = TypeAdapter(EmailStr)
ESTADOS_NO_ACTIVOS = {"SUSPENDIDA", "BLOQUEADA", "ELIMINADA"}


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
