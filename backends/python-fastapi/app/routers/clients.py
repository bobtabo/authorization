import threading
from typing import Optional
from fastapi import APIRouter, Depends, Query
from starlette.status import HTTP_201_CREATED
from pydantic import BaseModel
from app.routers.deps import get_client_interactor, get_notification_interactor, get_staff_id_from_cookie
from app.usecase.client.interactor import ClientInteractor
from app.usecase.client.dto import ClientStoreDto, ClientUpdateDto
from app.usecase.notification.interactor import NotificationInteractor
from app.infrastructure.mail.mailer import send_access_token

router = APIRouter()


def _map_client(c) -> dict:
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
        "token": c.token,
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
    interactor: ClientInteractor = Depends(get_client_interactor),
):
    clients = interactor.find_all(keyword=keyword, status=status)
    return [_map_client(c) for c in clients]


@router.get("/clients/{client_id}")
def show(client_id: int, interactor: ClientInteractor = Depends(get_client_interactor)):
    return _map_client(interactor.find_by_id(client_id))


class StoreBody(BaseModel):
    name: str
    post_code: str = ""
    pref: str = ""
    city: str = ""
    address: str = ""
    building: str = ""
    tel: str = ""
    email: str = ""


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
    client = interactor.store(dto)

    notif_url = f"/clients/show?id={client.id}"
    notification_interactor.fan_out(
        title="新しいクライアントが登録されました",
        body=client.name,
        url=notif_url,
        executor_id=executor_id,
        message_type=1,
    )

    threading.Thread(
        target=send_access_token,
        args=(client.email, client.name, client.token),
        daemon=True,
    ).start()

    return _map_client(client)


class UpdateBody(BaseModel):
    name: Optional[str] = None
    post_code: Optional[str] = None
    pref: Optional[str] = None
    city: Optional[str] = None
    address: Optional[str] = None
    building: Optional[str] = None
    tel: Optional[str] = None
    email: Optional[str] = None
    status: Optional[int] = None


@router.put("/clients/{client_id}/update")
def update(client_id: int, body: UpdateBody, interactor: ClientInteractor = Depends(get_client_interactor)):
    dto = ClientUpdateDto(client_id=client_id, **body.model_dump(exclude_none=True))
    client = interactor.update(dto)
    return _map_client(client)


@router.delete("/clients/{client_id}/delete")
def destroy(client_id: int, interactor: ClientInteractor = Depends(get_client_interactor)):
    interactor.destroy(client_id)
    return {"id": client_id}
