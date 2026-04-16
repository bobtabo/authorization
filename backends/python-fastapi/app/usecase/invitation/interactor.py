from app.domain.invitation.entity import Invitation
from app.domain.invitation.value_objects import InvitationVo
from app.domain.invitation.repository import InvitationRepository
from app.exceptions import not_found


class InvitationInteractor:
    """招待のユースケース実装。"""

    def __init__(self, repo: InvitationRepository):
        self.repo = repo

    def current(self) -> InvitationVo:
        result = self.repo.get_current()
        if result is None:
            raise not_found("invitation_not_found")
        return result

    def issue(self) -> InvitationVo:
        return self.repo.issue()

    def find_by_token(self, token: str) -> Invitation:
        inv = self.repo.find_by_token(token)
        if inv is None:
            raise not_found("invitation_not_found")
        return inv
