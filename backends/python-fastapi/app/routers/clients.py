from typing import Optional
from fastapi import APIRouter, Depends, Query
from starlette.status import HTTP_201_CREATED
from pydantic import BaseModel
from app.routers.deps import get_client_service
from app.services.client_service import ClientService

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
        "started_at": c.started_at.strftime("%Y-%m-%d %H:%M") if c.started_at else None,
        "stopped_at": c.stopped_at.strftime("%Y-%m-%d %H:%M") if c.stopped_at else None,
        "created_at": c.created_at.strftime("%Y-%m-%d %H:%M") if c.created_at else None,
        "updated_at": c.updated_at.strftime("%Y-%m-%d %H:%M") if c.updated_at else None,
    }


@router.get("/clients")
def index(
    keyword: Optional[str] = Query(default=None),
    status: Optional[int] = Query(default=None),
    svc: ClientService = Depends(get_client_service),
):
    clients = svc.find_all(keyword=keyword, status=status)
    return [_map_client(c) for c in clients]


@router.get("/clients/{client_id}")
def show(client_id: int, svc: ClientService = Depends(get_client_service)):
    return _map_client(svc.find_by_id(client_id))


class StoreBody(BaseModel):
    name: str
    identifier: str
    post_code: str = ""
    pref: str = ""
    city: str = ""
    address: str = ""
    building: str = ""
    tel: str = ""
    email: str = ""


@router.post("/clients/store", status_code=HTTP_201_CREATED)
def store(body: StoreBody, svc: ClientService = Depends(get_client_service)):
    client = svc.store(**body.model_dump())
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
def update(client_id: int, body: UpdateBody, svc: ClientService = Depends(get_client_service)):
    client = svc.update(client_id, **body.model_dump(exclude_none=True))
    return _map_client(client)


@router.delete("/clients/{client_id}/delete")
def destroy(client_id: int, svc: ClientService = Depends(get_client_service)):
    svc.destroy(client_id)
    return {"id": client_id}
