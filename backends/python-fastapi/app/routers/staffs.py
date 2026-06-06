"""
スタッフルーターモジュール。

Author: Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
"""
import math
from typing import Optional
from fastapi import APIRouter, Depends, Query
from pydantic import BaseModel
from app.routers.deps import get_staff_interactor, get_staff_id_from_cookie
from app.usecase.staff.interactor import StaffInteractor
from app.usecase.staff.dto import StaffUpdateRoleDto, StaffDestroyDto

router = APIRouter()

_DEFAULT_PAGE_COUNT = 5


def _build_pager(count: int, limit: int, offset: int, record_count: int) -> dict:
    if limit <= 0:
        limit = 20
    page_count = max(1, math.ceil(count / limit))
    last_page_offset = (page_count * limit) - limit
    if count > 0 and offset > last_page_offset:
        offset = last_page_offset
    page = int(math.floor(math.ceil(offset / limit))) + 1 if limit > 0 else 1
    start_page = max(1, page - (_DEFAULT_PAGE_COUNT - 1))
    end_page = min(page_count, start_page + (_DEFAULT_PAGE_COUNT - 1))
    return {
        "count": count,
        "limit": limit,
        "next": page_count > page,
        "previous": page > 1,
        "page": page,
        "nextPage": page + 1,
        "previousPage": page - 1,
        "pageCount": page_count,
        "first": page > 1,
        "last": page_count > page,
        "firstRecordCount": offset + 1,
        "lastRecordCount": offset + record_count,
        "startPage": start_page,
        "endPage": end_page,
    }


def _map_staff(s) -> dict:
    return {
        "id": s.id,
        "name": s.name,
        "email": s.email,
        "role": s.role,
        "status": s.status,
        "created_at": s.created_at.strftime("%Y-%m-%d %H:%M") if s.created_at else None,
        "updated_at": s.updated_at.strftime("%Y-%m-%d %H:%M") if s.updated_at else None,
    }


@router.get("/staffs")
def index(
    keyword: Optional[str] = Query(default=None),
    roles: Optional[list[int]] = Query(default=None),
    limit: int = Query(default=20, ge=1),
    page: int = Query(default=1, ge=1),
    sort: Optional[str] = Query(default=None),
    sort_type: Optional[str] = Query(default=None),
    interactor: StaffInteractor = Depends(get_staff_interactor),
):
    offset = limit * (page - 1)
    staffs, count = interactor.find_by_condition(
        keyword=keyword, roles=roles or [], offset=offset, limit=limit, sort=sort, sort_type=sort_type
    )
    pager = _build_pager(count, limit, offset, len(staffs))
    return {"data": [_map_staff(s) for s in staffs], "pager": pager}


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
