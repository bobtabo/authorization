"""
クライアントルーターモジュール。

Author: Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
"""
import math
import threading
from typing import Optional
from fastapi import APIRouter, Depends, Query
from starlette.status import HTTP_201_CREATED
from pydantic import BaseModel, Field
from app.routers.deps import get_client_interactor, get_jwt_history_repo, get_notification_interactor, get_staff_id_from_cookie
from app.infrastructure.persistence.sqlalchemy_jwt_history_repository import SqlAlchemyJwtHistoryRepository
from app.usecase.client.interactor import ClientInteractor
from app.usecase.client.dto import ClientStoreDto, ClientUpdateDto, ClientIdentifierDto
from app.usecase.notification.interactor import NotificationInteractor
from app.infrastructure.mail.mailer import send_activation
from app.config.settings import get_settings

router = APIRouter()

DEFAULT_PAGE_COUNT = 5


def _build_pager(count: int, limit: int, offset: int, record_count: int) -> dict:
    """ページング情報を構築します。"""
    if limit <= 0:
        limit = 20
    page_count = max(1, math.ceil(count / limit))
    last_page_offset = (page_count * limit) - limit
    if count > 0 and offset > last_page_offset:
        offset = last_page_offset
    page = int(math.floor(math.ceil(offset / limit))) + 1 if limit > 0 else 1
    start_page = max(1, page - (DEFAULT_PAGE_COUNT - 1))
    end_page = min(page_count, start_page + (DEFAULT_PAGE_COUNT - 1))
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


def _map_list_item(c) -> dict:
    return {
        "id": c.id,
        "name": c.name,
        "identifier": c.identifier,
        "status": c.status,
        "start_at": c.started_at.strftime("%Y-%m-%d %H:%M") if c.started_at else None,
        "stop_at": c.stopped_at.strftime("%Y-%m-%d %H:%M") if c.stopped_at else None,
        "created_at": c.created_at.strftime("%Y-%m-%d %H:%M") if c.created_at else None,
        "updated_at": c.updated_at.strftime("%Y-%m-%d %H:%M") if c.updated_at else None,
    }


def _map_detail(c) -> dict:
    return {
        "id": c.id,
        "name": c.name,
        "identifier": c.identifier,
        "post_code": c.post_code,
        "pref": c.pref,
        "city": c.city,
        "address": c.address,
        "building": c.building,
        "tel": c.tel,
        "email": c.email,
        "status": c.status,
        "fingerprint": c.fingerprint,
        "start_at": c.started_at.strftime("%Y-%m-%d %H:%M") if c.started_at else None,
        "stop_at": c.stopped_at.strftime("%Y-%m-%d %H:%M") if c.stopped_at else None,
        "created_at": c.created_at.strftime("%Y-%m-%d %H:%M") if c.created_at else None,
        "updated_at": c.updated_at.strftime("%Y-%m-%d %H:%M") if c.updated_at else None,
    }


@router.get("/clients")
def index(
    keyword: Optional[str] = Query(default=None),
    status: Optional[int] = Query(default=None),
    limit: int = Query(default=20, ge=1),
    page: int = Query(default=1, ge=1),
    sort: Optional[str] = Query(default=None),
    sort_type: Optional[str] = Query(default=None),
    interactor: ClientInteractor = Depends(get_client_interactor),
):
    actual_offset = limit * (page - 1)
    clients, count = interactor.find_all(
        keyword=keyword,
        status=status,
        offset=actual_offset,
        limit=limit,
        sort=sort,
        sort_type=sort_type,
    )
    pager = _build_pager(count, limit, actual_offset, len(clients))
    return {"data": [_map_list_item(c) for c in clients], "pager": pager}


@router.get("/clients/{client_id}")
def show(client_id: int, interactor: ClientInteractor = Depends(get_client_interactor)):
    return _map_detail(interactor.find_by_id(client_id))


@router.get("/clients/{client_id}/jwt-histories")
def jwt_histories(
    client_id: int,
    page: int = Query(default=1, ge=1),
    limit: int = Query(default=20, ge=1),
    sort: str = Query(default="issue_at"),
    sort_type: str = Query(default="desc"),
    repo: SqlAlchemyJwtHistoryRepository = Depends(get_jwt_history_repo),
):
    offset = limit * (page - 1)
    count = repo.count_by_client_id(client_id)
    histories = repo.find_by_condition(client_id, offset=offset, limit=limit, sort=sort, sort_type=sort_type)
    data = [
        {
            "id": h.id,
            "member_id": h.member_id,
            "issue_at": h.issue_at.strftime("%Y-%m-%d %H:%M:%S") if h.issue_at else None,
            "jwt": h.jwt,
        }
        for h in histories
    ]
    pager = _build_pager(count, limit, offset, len(data))
    return {"data": data, "pager": pager}


class StoreBody(BaseModel):
    name: str = Field(max_length=255)
    post_code: str = Field(max_length=8)
    pref: str = Field(max_length=50)
    city: str = Field(max_length=100)
    address: str = Field(max_length=255)
    building: str = Field(default="", max_length=255)
    tel: str = Field(pattern=r"^\d{10,11}$")
    email: str = Field(max_length=255, pattern=r"^[^\s@]+@[^\s@]+\.[^\s@]+$")


@router.post("/clients/store", status_code=HTTP_201_CREATED)
def store(
    body: StoreBody,
    interactor: ClientInteractor = Depends(get_client_interactor),
    notification_interactor: NotificationInteractor = Depends(get_notification_interactor),
    executor_id: int = Depends(get_staff_id_from_cookie),
):
    dto = ClientStoreDto(name=body.name, post_code=body.post_code, pref=body.pref,
                         city=body.city, address=body.address, building=body.building,
                         tel=body.tel, email=body.email, executor_id=executor_id)
    result = interactor.store(dto)

    notif_url = f"/clients/show?id={result.id}"
    notification_interactor.fan_out(
        title="新しいクライアントが登録されました",
        body=result.name,
        url=notif_url,
        executor_id=executor_id,
        message_type=1,
    )

    settings = get_settings()
    activate_url = f"{settings.frontend_url}/clients/{result.identifier}/qr"
    threading.Thread(
        target=send_activation,
        args=(result.email, result.name, activate_url),
        daemon=True,
    ).start()

    return {"id": result.id, "name": result.name, "identifier": result.identifier, "email": result.email, "token": result.token}


class UpdateBody(BaseModel):
    name: Optional[str] = Field(default=None, max_length=255)
    post_code: Optional[str] = Field(default=None, max_length=8)
    pref: Optional[str] = Field(default=None, max_length=50)
    city: Optional[str] = Field(default=None, max_length=100)
    address: Optional[str] = Field(default=None, max_length=255)
    building: Optional[str] = Field(default=None, max_length=255)
    tel: Optional[str] = Field(default=None, pattern=r"^\d{10,11}$")
    email: Optional[str] = Field(default=None, max_length=255, pattern=r"^[^\s@]+@[^\s@]+\.[^\s@]+$")
    status: Optional[int] = None


@router.put("/clients/{client_id}/update")
def update(client_id: int, body: UpdateBody, interactor: ClientInteractor = Depends(get_client_interactor)):
    dto = ClientUpdateDto(client_id=client_id, **body.model_dump(exclude_none=True))
    return _map_detail(interactor.update(dto))


@router.delete("/clients/{client_id}/delete")
def destroy(client_id: int, interactor: ClientInteractor = Depends(get_client_interactor)):
    interactor.destroy(client_id)
    return {"id": client_id}


@router.get("/clients/{identifier}/qr")
def qr(identifier: str, interactor: ClientInteractor = Depends(get_client_interactor)):
    dto = ClientIdentifierDto(identifier=identifier)
    vo = interactor.get_qr(dto)
    return {"identifier": vo.identifier, "deeplink_url": vo.deeplink_url}


@router.get("/clients/{identifier}/info")
def info(identifier: str, interactor: ClientInteractor = Depends(get_client_interactor)):
    dto = ClientIdentifierDto(identifier=identifier)
    vo = interactor.get_info(dto)
    return {"identifier": vo.identifier, "name": vo.name, "status": vo.status}


@router.patch("/clients/{identifier}/start")
def start(identifier: str, interactor: ClientInteractor = Depends(get_client_interactor)):
    dto = ClientIdentifierDto(identifier=identifier)
    vo = interactor.start(dto)
    return {"access_token": vo.access_token}


@router.patch("/clients/{identifier}/stop")
def stop(identifier: str, interactor: ClientInteractor = Depends(get_client_interactor)):
    dto = ClientIdentifierDto(identifier=identifier)
    interactor.stop(dto)
    return {}
