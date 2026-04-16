from typing import Optional
from fastapi import APIRouter, Depends, Query
from pydantic import BaseModel
from app.routers.deps import get_staff_interactor, get_staff_id_from_cookie
from app.usecase.staff.interactor import StaffInteractor, staff_status
from app.usecase.staff.dto import StaffUpdateRoleDto, StaffDestroyDto

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
    interactor: StaffInteractor = Depends(get_staff_interactor),
):
    staffs = interactor.find_by_condition(keyword=keyword, roles=roles or [])
    return {"items": [_map_staff(s) for s in staffs]}


class UpdateRoleBody(BaseModel):
    role: int


@router.patch("/staffs/{staff_id}/updateRole")
def update_role(
    staff_id: int,
    body: UpdateRoleBody,
    executor_id: int = Depends(get_staff_id_from_cookie),
    interactor: StaffInteractor = Depends(get_staff_interactor),
):
    dto = StaffUpdateRoleDto(staff_id=staff_id, role=body.role, executor_id=executor_id)
    interactor.update_role(dto)
    return {"id": staff_id}


@router.patch("/staffs/{staff_id}/restore")
def restore(staff_id: int, interactor: StaffInteractor = Depends(get_staff_interactor)):
    interactor.restore(staff_id)
    return {"id": staff_id}


@router.delete("/staffs/{staff_id}/delete")
def destroy(
    staff_id: int,
    executor_id: int = Depends(get_staff_id_from_cookie),
    interactor: StaffInteractor = Depends(get_staff_interactor),
):
    dto = StaffDestroyDto(staff_id=staff_id, executor_id=executor_id)
    interactor.destroy(dto)
    return {"id": staff_id}
