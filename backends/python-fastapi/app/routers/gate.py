from fastapi import APIRouter, Depends, Query
from app.routers.deps import get_gate_service, get_bearer_token
from app.services.gate_service import GateService

router = APIRouter()


@router.get("/gate/issue")
def issue(
    member: str = Query(...),
    token: str = Depends(get_bearer_token),
    svc: GateService = Depends(get_gate_service),
):
    jwt_token = svc.issue_token(token, member)
    return {"token": jwt_token}


@router.get("/gate/client/{identifier}/verify")
def verify(
    identifier: str,
    token: str = Query(...),
    svc: GateService = Depends(get_gate_service),
):
    return svc.verify(identifier, token)
