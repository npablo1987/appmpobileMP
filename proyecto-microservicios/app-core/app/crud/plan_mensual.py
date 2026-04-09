from sqlalchemy.orm import Session
from app.models.plan_mensual import PlanMensual
from app.schemas.plan_mensual import PlanMensualCreate, PlanMensualUpdate
from typing import List, Optional

def get_planes(db: Session, skip: int = 0, limit: int = 100) -> List[PlanMensual]:
    return db.query(PlanMensual).offset(skip).limit(limit).all()

def get_plan(db: Session, plan_id: int) -> Optional[PlanMensual]:
    return db.query(PlanMensual).filter(PlanMensual.id_plan == plan_id).first()

def get_plan_by_nombre(db: Session, nombre_plan: str) -> Optional[PlanMensual]:
    return db.query(PlanMensual).filter(PlanMensual.nombre_plan == nombre_plan).first()

def create_plan(db: Session, plan: PlanMensualCreate) -> PlanMensual:
    db_plan = PlanMensual(**plan.model_dump())
    db.add(db_plan)
    db.commit()
    db.refresh(db_plan)
    return db_plan

def update_plan(db: Session, plan_id: int, plan: PlanMensualUpdate) -> Optional[PlanMensual]:
    db_plan = get_plan(db, plan_id)
    if db_plan:
        update_data = plan.model_dump(exclude_unset=True)
        for key, value in update_data.items():
            setattr(db_plan, key, value)
        db.commit()
        db.refresh(db_plan)
    return db_plan

def delete_plan(db: Session, plan_id: int) -> bool:
    db_plan = get_plan(db, plan_id)
    if db_plan:
        db.delete(db_plan)
        db.commit()
        return True
    return False
