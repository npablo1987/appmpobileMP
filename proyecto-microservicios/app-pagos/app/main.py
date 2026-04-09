from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.routers import pagos_router

app = FastAPI(
    title="Microservicio de Pagos",
    description="API especializada en gestión de pagos y transacciones",
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
    return {"status": "healthy", "service": "app-pagos"}

app.include_router(pagos_router)
