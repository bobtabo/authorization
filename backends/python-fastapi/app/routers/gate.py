from fastapi import APIRouter, Depends, Query
from app.routers.deps import get_gate_interactor, get_bearer_token
from app.usecase.gate.interactor import GateInteractor
from app.usecase.gate.dto import GateIssueDto, GateVerifyDto

router = APIRouter()


@router.get("/gate/issue")
def issue(
    member: str = Query(...),
    token: str = Depends(get_bearer_token),
    interactor: GateInteractor = Depends(get_gate_interactor),
):
    dto = GateIssueDto(access_token=token, member=member)
    jwt_token = interactor.issue_token(dto)
    return {"token": jwt_token}


@router.get("/gate/client/{identifier}/verify")
def verify(
    identifier: str,
    token: str = Query(...),
    interactor: GateInteractor = Depends(get_gate_interactor),
):
    dto = GateVerifyDto(identifier=identifier, token=token)
    return interactor.verify(dto)
