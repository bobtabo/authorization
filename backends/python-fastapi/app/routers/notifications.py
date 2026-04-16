from typing import Optional
from fastapi import APIRouter, Depends, Query
from pydantic import BaseModel
from starlette.status import HTTP_202_ACCEPTED
from app.routers.deps import get_notification_interactor, require_staff_id, get_staff_id_from_cookie
from app.usecase.notification.interactor import NotificationInteractor, map_notification
from app.usecase.notification.dto import NotificationBulkReadDto, NotificationPatchDto
from app.config.settings import get_settings, Settings

router = APIRouter()


@router.get("/notifications/counts")
def counts(
    staff_id: int = Depends(require_staff_id),
    interactor: NotificationInteractor = Depends(get_notification_interactor),
):
    unread, total = interactor.counts(staff_id)
    return {"unread": unread, "total": total}


@router.get("/notifications")
def index(
    cursor: Optional[str] = Query(default=None),
    limit: Optional[int] = Query(default=None),
    staff_id: int = Depends(require_staff_id),
    interactor: NotificationInteractor = Depends(get_notification_interactor),
    settings: Settings = Depends(get_settings),
):
    lim = limit if (limit and limit > 0) else settings.notification_default_limit
    page = interactor.list_page(staff_id, cursor, lim)
    return {
        "items": [map_notification(n) for n in page.items],
        "next_cursor": page.next_cursor,
    }


class StoreBody(BaseModel):
    title: Optional[str] = None
    body: Optional[str] = None


@router.post("/notifications", status_code=HTTP_202_ACCEPTED)
def store(body: StoreBody):
    return {"message": "notification_accepted", "received": body.model_dump()}


class ReadAllBody(BaseModel):
    ids: list[int] = []
    all: bool = False
    executor_id: int = 0


@router.patch("/notifications")
def read_all(body: ReadAllBody, interactor: NotificationInteractor = Depends(get_notification_interactor)):
    from app.exceptions import unauthorized, bad_request
    if body.executor_id == 0:
        raise unauthorized("unauthenticated")
    if not body.ids and not body.all:
        raise bad_request("ids_or_all_required")
    dto = NotificationBulkReadDto(executor_id=body.executor_id, ids=body.ids, all_flag=body.all)
    updated = interactor.bulk_mark_read(dto)
    return {"updated": updated}


class PatchBody(BaseModel):
    read: Optional[bool] = None
    title: Optional[str] = None
    body: Optional[str] = None


@router.patch("/notifications/{notification_id}")
def read(
    notification_id: int,
    body: PatchBody,
    interactor: NotificationInteractor = Depends(get_notification_interactor),
):
    data = body.model_dump(exclude_none=True)
    dto = NotificationPatchDto(
        notification_id=notification_id,
        read=data.get("read"),
        title=data.get("title"),
        message=data.get("body"),
    )
    interactor.patch(dto)
    return {"id": notification_id}
