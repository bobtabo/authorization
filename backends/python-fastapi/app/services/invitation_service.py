from app.exceptions import not_found
from app.repositories.invitation_repo import InvitationRepository, InvitationResult


class InvitationService:
    def __init__(self, repo: InvitationRepository):
        self.repo = repo

    def current(self) -> InvitationResult:
        result = self.repo.get_current()
        if result is None:
            raise not_found("invitation_not_found")
        return result

    def issue(self) -> InvitationResult:
        return self.repo.issue()

    def find_by_token(self, token: str):
        inv = self.repo.find_by_token(token)
        if inv is None:
            raise not_found("invitation_not_found")
        return inv
