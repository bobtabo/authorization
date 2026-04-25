"""
招待ユースケース Interactor モジュール。

Author: Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
"""
from app.domain.invitation.value_objects import InvitationVo
from app.domain.invitation.repository import InvitationRepository
from app.exceptions import not_found


class InvitationInteractor:
    """招待のユースケース実装。

    Attributes:
        repository: 招待リポジトリ
    """

    def __init__(self, repo: InvitationRepository):
        """初期化します。

        Args:
            repo: 招待リポジトリ
        """
        self.repository = repo

    def current(self) -> InvitationVo:
        """最新の招待情報の Vo を返します。

        Returns:
            InvitationVo インスタンス

        Raises:
            AppException: 招待が存在しない場合
        """
        result = self.repository.get_current()
        if result is None:
            raise not_found("invitation_not_found")
        return result

    def issue(self) -> InvitationVo:
        """新しい招待トークンを発行し、招待情報の Vo を返します。

        Returns:
            InvitationVo インスタンス
        """
        return self.repository.issue()

    def find_by_token(self, token: str) -> InvitationVo:
        """トークンで招待情報の Vo を返します。

        Args:
            token: 招待トークン

        Returns:
            InvitationVo インスタンス

        Raises:
            AppException: 招待が存在しない場合
        """
        result = self.repository.find_by_token(token)
        if result is None:
            raise not_found("invitation_not_found")
        return result
