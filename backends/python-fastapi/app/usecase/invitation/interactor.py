"""
招待ユースケース Interactor モジュール。

Author: Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
"""
from app.domain.invitation.value_objects import InvitationVo
from app.domain.invitation.repository import InvitationRepository
from app.domain.invitation.auth_repository import InvitationAuthRepository
from app.exceptions import not_found


class InvitationInteractor:
    """招待のユースケース実装。

    Attributes:
        invitation_repository: 招待リポジトリ
        invitation_auth_repository: 招待認証キャッシュリポジトリ
    """

    def __init__(self, repo: InvitationRepository, auth_repo: InvitationAuthRepository):
        """初期化します。

        Args:
            repo: 招待リポジトリ
            auth_repo: 招待認証キャッシュリポジトリ
        """
        self.invitation_repository = repo
        self.invitation_auth_repository = auth_repo

    def current(self, role: int) -> InvitationVo:
        """ロールで絞り込んだ最新の招待情報の Vo を返します。

        Args:
            role: ロール（1=管理者, 2=メンバー）

        Returns:
            InvitationVo インスタンス

        Raises:
            AppException: 招待が存在しない場合
        """
        result = self.invitation_repository.get_current_by_role(role)
        if result is None:
            raise not_found("invitation_not_found")
        return result

    def issue(self, role: int) -> InvitationVo:
        """新しい招待トークンを発行し、招待情報の Vo を返します。

        Args:
            role: ロール（1=管理者, 2=メンバー）

        Returns:
            InvitationVo インスタンス
        """
        return self.invitation_repository.issue(role)

    def find_by_token(self, token: str) -> InvitationVo:
        """トークンで招待情報の Vo を返し、認証トークンとロールをキャッシュします。

        Args:
            token: 招待トークン

        Returns:
            InvitationVo インスタンス

        Raises:
            AppException: 招待が存在しない場合
        """
        result = self.invitation_repository.find_by_token(token)
        if result is None:
            raise not_found("invitation_not_found")
        self.invitation_auth_repository.store(result.token, result.role, 600)
        return result
