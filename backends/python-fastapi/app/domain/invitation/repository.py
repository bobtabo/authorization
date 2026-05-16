"""
招待ドメイン リポジトリインターフェースモジュール。

Author: Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
"""
from abc import ABC, abstractmethod
from typing import Optional

from app.domain.invitation.value_objects import InvitationVo


class InvitationRepository(ABC):
    """招待の永続化インターフェース。"""

    @abstractmethod
    def get_current_by_role(self, role: int) -> Optional[InvitationVo]:
        """ロールで絞り込んだ最新の招待情報の Vo を返します。

        Args:
            role: ロール（1=管理者, 2=メンバー）

        Returns:
            InvitationVo、または None
        """
        ...

    @abstractmethod
    def issue(self, role: int) -> InvitationVo:
        """新しい招待トークンを生成して保存し、Vo を返します。

        Args:
            role: ロール（1=管理者, 2=メンバー）

        Returns:
            InvitationVo インスタンス
        """
        ...

    @abstractmethod
    def find_by_token(self, token: str) -> Optional[InvitationVo]:
        """トークンで招待情報の Vo を返します。存在しない場合は None を返します。

        Args:
            token: 招待トークン

        Returns:
            InvitationVo、または None
        """
        ...
