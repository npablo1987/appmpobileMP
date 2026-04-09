from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.routers import (
    usuarios_router,
    planes_router,
    servicios_router,
    metodos_pago_router,
    suscripciones_router,
    usuarios_servicios_router,
    facturas_router,
    detalle_factura_router
)

app = FastAPI(
    title="Microservicio Core - Gestión de Usuarios y Servicios",
    description="API principal para gestión de usuarios, planes, servicios, suscripciones y facturación",
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
    return {"status": "healthy", "service": "app-core"}

app.include_router(usuarios_router)
app.include_router(planes_router)
app.include_router(servicios_router)
app.include_router(metodos_pago_router)
app.include_router(suscripciones_router)
app.include_router(usuarios_servicios_router)
app.include_router(facturas_router)
app.include_router(detalle_factura_router)
