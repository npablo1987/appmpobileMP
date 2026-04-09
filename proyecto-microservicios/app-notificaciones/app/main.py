from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.routers import notificaciones_router
import logging

logging.basicConfig(level=logging.INFO)

app = FastAPI(
    title="Microservicio de Notificaciones",
    description="API especializada en gestión y envío de notificaciones (Email, SMS, Internas)",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.get("/health")
def health_check():
    return {"status": "healthy", "service": "app-notificaciones"}

app.include_router(notificaciones_router)
