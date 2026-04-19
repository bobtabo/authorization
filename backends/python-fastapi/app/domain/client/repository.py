from abc import ABC, abstractmethod
from typing import Optional
from app.domain.client.entity import Client
from app.domain.client.condition import ClientCondition


class ClientRepository(ABC):
    """クライアントの永続化インターフェース。"""

    @abstractmethod
    def find_all_clients(self, cond: ClientCondition) -> list[Client]:
        ...

    @abstractmethod
    def find_client_by_id(self, client_id: int) -> Optional[Client]:
        ...

    @abstractmethod
    def find_client_by_token(self, token: str) -> Optional[Client]:
        ...

    @abstractmethod
    def find_client_by_identifier(self, identifier: str) -> Optional[Client]:
        ...

    @abstractmethod
    def save_client(self, client: Client) -> Client:
        ...

    @abstractmethod
    def soft_delete_client(self, client: Client) -> None:
        ...
