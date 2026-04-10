#!/bin/bash

# Script para iniciar el servidor de pagos

echo "Iniciando servidor de pagos en puerto 8002..."
echo ""

# Instalar dependencias si es necesario
if [ ! -d "venv" ]; then
    echo "Creando entorno virtual..."
    python3 -m venv venv
    source venv/bin/activate
    pip install -r requirements.txt
else
    source venv/bin/activate
fi

# Iniciar el servidor
python -m uvicorn app.main:app --host 0.0.0.0 --port 8002 --reload

# Si falla, intentar sin reload
if [ $? -ne 0 ]; then
    echo "Intentando sin --reload..."
    python -m uvicorn app.main:app --host 0.0.0.0 --port 8002
fi
