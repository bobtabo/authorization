from dataclasses import dataclass


@dataclass
class InvitationResultDto:
    token: str
    url: str
    display_url: str
