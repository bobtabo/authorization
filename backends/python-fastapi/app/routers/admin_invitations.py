"""
管理者招待ルーターモジュール。

Author: Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
"""
from fastapi import APIRouter, Depends, Query
from app.exceptions import bad_request, unauthorized
from app.routers.deps import get_invitation_interactor, get_staff_id_from_cookie
from app.usecase.invitation.interactor import InvitationInteractor

router = APIRouter()


@router.get("/admin/invitation")
def index(
    role: int = Query(2),
    interactor: InvitationInteractor = Depends(get_invitation_interactor),
):
    if role not in (1, 2):
        raise bad_request("invalid_role")
    result = interactor.current(role)
    return {"found": True, "url": result.url, "display_url": result.display_url, "token": result.token}


@router.get("/admin/invitation/issue")
def issue(
    role: int = Query(2),
    staff_id: int = Depends(get_staff_id_from_cookie),
    interactor: InvitationInteractor = Depends(get_invitation_interactor),
):
    if staff_id == 0:
        raise unauthorized("unauthenticated")
    if role not in (1, 2):
        raise bad_request("invalid_role")
    result = interactor.issue(role)
    return {"found": True, "url": result.url, "display_url": result.display_url, "token": result.token}
