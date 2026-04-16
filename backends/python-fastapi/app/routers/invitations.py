from fastapi import APIRouter, Depends
from app.routers.deps import get_invitation_interactor
from app.usecase.invitation.interactor import InvitationInteractor

router = APIRouter()


@router.get("/invitation")
def index(interactor: InvitationInteractor = Depends(get_invitation_interactor)):
    result = interactor.current()
    return {"found": True, "url": result.url, "display_url": result.display_url, "token": result.token}


@router.get("/invitation/issue")
def issue(interactor: InvitationInteractor = Depends(get_invitation_interactor)):
    result = interactor.issue()
    return {"found": True, "url": result.url, "display_url": result.display_url, "token": result.token}
