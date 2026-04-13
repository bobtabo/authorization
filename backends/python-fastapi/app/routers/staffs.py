from typing import Optional
from fastapi import APIRouter, Depends, Query
from pydantic import BaseModel
from app.routers.deps import get_staff_service, get_staff_id_from_cookie
from app.services.staff_service import StaffService, staff_status

router = APIRouter()


def _map_staff(s) -> dict:
    return {
        "id": s.id,
        "name": s.name,
        "email": s.email,
        "role": s.role,
        "status": staff_status(s),
        "created_at": s.created_at.strftime("%Y-%m-%d %H:%M") if s.created_at else None,
        "updated_at": s.updated_at.strftime("%Y-%m-%d %H:%M") if s.updated_at else None,
    }


@router.get("/staffs")
def index(
    keyword: Optional[str] = Query(default=None),
    roles: Optional[list[int]] = Query(default=None),
    svc: StaffService = Depends(get_staff_service),
):
    staffs = svc.find_by_condition(keyword=keyword, roles=roles or [])
    return {"items": [_map_staff(s) for s in staffs]}


class UpdateRoleBody(BaseModel):
    role: int


@router.patch("/staffs/{staff_id}/updateRole")
def update_role(
    staff_id: int,
    body: UpdateRoleBody,
    executor_id: int = Depends(get_staff_id_from_cookie),
    svc: StaffService = Depends(get_staff_service),
):
    svc.update_role(staff_id, body.role, executor_id)
    return {"id": staff_id}


@router.patch("/staffs/{staff_id}/restore")
def restore(staff_id: int, svc: StaffService = Depends(get_staff_service)):
    svc.restore(staff_id)
    return {"id": staff_id}


@router.delete("/staffs/{staff_id}/delete")
def destroy(
    staff_id: int,
    executor_id: int = Depends(get_staff_id_from_cookie),
    svc: StaffService = Depends(get_staff_service),
):
    svc.destroy(staff_id, executor_id)
    return {"id": staff_id}
