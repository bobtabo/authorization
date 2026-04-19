from dataclasses import dataclass


@dataclass
class GateIssueDto:
    access_token: str
    member: str


@dataclass
class GateVerifyDto:
    identifier: str
    token: str
