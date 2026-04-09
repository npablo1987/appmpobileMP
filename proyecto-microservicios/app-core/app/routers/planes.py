from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from app.database import get_db
from app.schemas.plan_mensual import PlanMensualCreate, PlanMensualUpdate, PlanMensualResponse
from app.crud import plan_mensual as crud_plan

router = APIRouter(prefix="/planes", tags=["planes"])

@router.post("/", response_model=PlanMensualResponse, status_code=status.HTTP_201_CREATED)
def create_plan(plan: PlanMensualCreate, db: Session = Depends(get_db)):
    db_plan = crud_plan.get_plan_by_nombre(db, nombre_plan=plan.nombre_plan)
    if db_plan:
        raise HTTPException(status_code=400, detail="El nombre del plan ya existe")
    return crud_plan.create_plan(db=db, plan=plan)

@router.get("/", response_model=List[PlanMensualResponse])
def read_planes(skip: int = 0, limit: int = 100, db: Session = Depends(get_db)):
    planes = crud_plan.get_planes(db, skip=skip, limit=limit)
    return planes

@router.get("/{id}", response_model=PlanMensualResponse)
def read_plan(id: int, db: Session = Depends(get_db)):
    db_plan = crud_plan.get_plan(db, plan_id=id)
    if db_plan is None:
        raise HTTPException(status_code=404, detail="Plan no encontrado")
    return db_plan

@router.put("/{id}", response_model=PlanMensualResponse)
def update_plan(id: int, plan: PlanMensualUpdate, db: Session = Depends(get_db)):
    db_plan = crud_plan.update_plan(db, plan_id=id, plan=plan)
    if db_plan is None:
        raise HTTPException(status_code=404, detail="Plan no encontrado")
    return db_plan

@router.delete("/{id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_plan(id: int, db: Session = Depends(get_db)):
    success = crud_plan.delete_plan(db, plan_id=id)
    if not success:
        raise HTTPException(status_code=404, detail="Plan no encontrado")
    return None
