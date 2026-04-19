from typing import Optional
from fastapi import APIRouter, Depends, Query
from app.routers.deps import get_notification_interactor, require_staff_id
from app.usecase.notification.interactor import NotificationInteractor, map_notification
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


@router.patch("/notifications")
def read_all(
    staff_id: int = Depends(require_staff_id),
    interactor: NotificationInteractor = Depends(get_notification_interactor),
):
    updated = interactor.bulk_mark_read(staff_id)
    return {"updated": updated}


@router.patch("/notifications/{notification_id}")
def read(
    notification_id: int,
    interactor: NotificationInteractor = Depends(get_notification_interactor),
):
    interactor.mark_read(notification_id)
    return {"id": notification_id}
