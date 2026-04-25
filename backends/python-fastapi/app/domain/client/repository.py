"""
クライアントドメイン リポジトリインターフェースモジュール。

Author: Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
"""
from abc import ABC, abstractmethod
from typing import Optional
from app.domain.client.entity import Client
from app.domain.client.condition import ClientCondition


class ClientRepository(ABC):
    """クライアントの永続化インターフェース。"""

    @abstractmethod
    def find_all_clients(self, cond: ClientCondition) -> list[Client]:
        """検索条件に合致するクライアントの一覧を返します。

        Args:
            cond: 検索条件

        Returns:
            Client エンティティのリスト
        """
        ...

    @abstractmethod
    def find_client_by_id(self, client_id: int) -> Optional[Client]:
        """IDでクライアントを返します。存在しない場合は None を返します。

        Args:
            client_id: クライアントID

        Returns:
            Client エンティティ、または None
        """
        ...

    @abstractmethod
    def find_client_by_token(self, token: str) -> Optional[Client]:
        """アクセストークンでクライアントを返します。存在しない場合は None を返します。

        Args:
            token: アクセストークン

        Returns:
            Client エンティティ、または None
        """
        ...

    @abstractmethod
    def find_client_by_identifier(self, identifier: str) -> Optional[Client]:
        """識別子でクライアントを返します。存在しない場合は None を返します。

        Args:
            identifier: クライアント識別子

        Returns:
            Client エンティティ、または None
        """
        ...

    @abstractmethod
    def save_client(self, client: Client) -> Client:
        """クライアントを保存し、保存後のエンティティを返します。

        Args:
            client: 保存するクライアントエンティティ

        Returns:
            保存後の Client エンティティ
        """
        ...

    @abstractmethod
    def soft_delete_client(self, client: Client) -> None:
        """クライアントを論理削除します。

        Args:
            client: 論理削除するクライアントエンティティ
        """
        ...
