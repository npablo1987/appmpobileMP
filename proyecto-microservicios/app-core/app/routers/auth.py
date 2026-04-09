import logging

from fastapi import APIRouter, Depends
from fastapi.responses import JSONResponse
from sqlalchemy.orm import Session

from app.database import get_db
from app.schemas.auth import LoginRequest
from app.services.auth_service import authenticate_user, get_home_user_data

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/auth", tags=["auth"])


@router.post("/login")
def login(payload: LoginRequest, db: Session = Depends(get_db)):
    try:
        status_code, response = authenticate_user(db, payload)
        return JSONResponse(status_code=status_code, content=response)
    except Exception:
        logger.exception("Error interno en endpoint de login")
        return JSONResponse(
            status_code=500,
            content={"success": False, "message": "Error interno del servidor"},
        )


@router.get("/me/{id_usuario}")
def get_me(id_usuario: int, db: Session = Depends(get_db)):
    try:
        status_code, response = get_home_user_data(db, id_usuario)
        return JSONResponse(status_code=status_code, content=response)
    except Exception:
        logger.exception("Error interno en endpoint de datos Home")
        return JSONResponse(
            status_code=500,
            content={"success": False, "message": "Error interno del servidor"},
        )
