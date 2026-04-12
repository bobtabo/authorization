from typing import Optional
from fastapi import APIRouter, Depends, Query
from pydantic import BaseModel
from starlette.status import HTTP_202_ACCEPTED
from app.routers.deps import get_notification_service, require_staff_id, get_staff_id_from_cookie
from app.services.notification_service import NotificationService, map_notification
from app.config.settings import get_settings, Settings

router = APIRouter()


@router.get("/notifications/counts")
def counts(
    staff_id: int = Depends(require_staff_id),
    svc: NotificationService = Depends(get_notification_service),
):
    unread, total = svc.counts(staff_id)
    return {"unread": unread, "total": total}


@router.get("/notifications")
def index(
    cursor: Optional[str] = Query(default=None),
    limit: Optional[int] = Query(default=None),
    staff_id: int = Depends(require_staff_id),
    svc: NotificationService = Depends(get_notification_service),
    settings: Settings = Depends(get_settings),
):
    lim = limit if (limit and limit > 0) else settings.notification_default_limit
    page = svc.list_page(staff_id, cursor, lim)
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
def read_all(body: ReadAllBody, svc: NotificationService = Depends(get_notification_service)):
    from app.exceptions import unauthorized, bad_request
    if body.executor_id == 0:
        raise unauthorized("unauthenticated")
    if not body.ids and not body.all:
        raise bad_request("ids_or_all_required")
    updated = svc.bulk_mark_read(body.executor_id, body.ids, body.all)
    return {"updated": updated}


class PatchBody(BaseModel):
    read: Optional[bool] = None
    title: Optional[str] = None
    body: Optional[str] = None


@router.patch("/notifications/{notification_id}")
def read(
    notification_id: int,
    body: PatchBody,
    svc: NotificationService = Depends(get_notification_service),
):
    svc.patch(notification_id, body.model_dump(exclude_none=True))
    return {"id": notification_id}
