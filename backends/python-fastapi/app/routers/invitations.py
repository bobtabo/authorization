from fastapi import APIRouter, Depends
from app.routers.deps import get_invitation_service
from app.services.invitation_service import InvitationService

router = APIRouter()


@router.get("/invitation")
def index(svc: InvitationService = Depends(get_invitation_service)):
    result = svc.current()
    return {"found": True, "url": result.url, "display_url": result.display_url, "token": result.token}


@router.get("/invitation/issue")
def issue(svc: InvitationService = Depends(get_invitation_service)):
    result = svc.issue()
    return {"found": True, "url": result.url, "display_url": result.display_url, "token": result.token}
