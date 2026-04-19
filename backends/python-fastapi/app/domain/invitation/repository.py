from abc import ABC, abstractmethod
from typing import Optional
from app.domain.invitation.entity import Invitation
from app.domain.invitation.value_objects import InvitationVo


class InvitationRepository(ABC):
    """招待の永続化インターフェース。"""

    @abstractmethod
    def get_current(self) -> Optional[InvitationVo]:
        ...

    @abstractmethod
    def issue(self) -> InvitationVo:
        ...

    @abstractmethod
    def find_by_token(self, token: str) -> Optional[Invitation]:
        ...
