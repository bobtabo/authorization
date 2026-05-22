"""
Gate ユースケース Interactor モジュール。

Author: Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
"""
import time
from datetime import datetime
from typing import Optional

from jose import jwt, JWTError

from app.config.settings import get_settings
from app.domain.client.repository import ClientRepository
from app.domain.gate.value_objects import GateIssueVo, GateVerifyVo
from app.exceptions import unauthorized, not_found, internal
from app.infrastructure.cache.redis_gate_repository import RedisGateRepository
from app.infrastructure.persistence.sqlalchemy_jwt_history_repository import SqlAlchemyJwtHistoryRepository
from app.usecase.gate.dto import GateIssueDto, GateVerifyDto


class GateInteractor:
    """Gate のユースケース実装。

    Attributes:
        client_repo: クライアントリポジトリ
        cache_repo: JWTキャッシュリポジトリ
        history_repo: JWT履歴リポジトリ（省略可）
        settings: アプリケーション設定
    """

    def __init__(
        self,
        client_repo: ClientRepository,
        cache_repo: RedisGateRepository,
        history_repo: Optional[SqlAlchemyJwtHistoryRepository] = None,
    ):
        self.client_repo = client_repo
        self.cache_repo = cache_repo
        self.history_repo = history_repo
        self.settings = get_settings()

    def issue_token(self, dto: GateIssueDto) -> GateIssueVo:
        """クライアント会員向け JWT を発行し、発行結果の Vo を返します。

        キャッシュに有効なトークンが存在する場合はキャッシュから返します。

        Args:
            dto: JWT 発行 Dto

        Returns:
            GateIssueVo インスタンス

        Raises:
            AppException: クライアントが存在しない場合、または秘密鍵が未設定の場合
        """
        client = self.client_repo.find_client_by_token(dto.access_token)
        if client is None:
            raise unauthorized("invalid_token")
        if client.private_key is None:
            raise internal("private_key_not_found")

        cached = self.cache_repo.get_jwt(client.identifier, dto.member)
        if cached:
            return GateIssueVo(token=cached)

        token = self._issue_jwt(client.private_key, client.identifier, dto.member)
        self.cache_repo.put_jwt(client.identifier, dto.member, token)
        if self.history_repo and client.id:
            try:
                self.history_repo.save(client.id, dto.member, datetime.now(), token)
            except Exception:
                pass
        return GateIssueVo(token=token)

    def verify(self, dto: GateVerifyDto) -> GateVerifyVo:
        """JWT を検証し、ペイロードの Vo を返します。

        Args:
            dto: JWT 検証 Dto

        Returns:
            GateVerifyVo インスタンス

        Raises:
            AppException: クライアントが存在しない場合、または JWT が無効の場合
        """
        client = self.client_repo.find_client_by_identifier(dto.identifier)
        if client is None:
            raise not_found("client_not_found")
        if client.public_key is None:
            raise internal("public_key_not_found")

        try:
            payload = jwt.decode(
                dto.token,
                client.public_key,
                algorithms=[self.settings.jwt_algorithm],
                audience=dto.identifier,
            )
        except JWTError as e:
            raise unauthorized(str(e))

        return GateVerifyVo(
            identifier=dto.identifier,
            member=payload.get("sub", ""),
            fingerprint=client.fingerprint or "",
            payload=payload,
        )

    def _issue_jwt(self, private_key_pem: str, identifier: str, member: str) -> str:
        """RSA 秘密鍵で JWT を発行します。

        Args:
            private_key_pem: PEM 形式の RSA 秘密鍵
            identifier: クライアント識別子
            member: 会員ID

        Returns:
            JWT 文字列
        """
        s = self.settings
        now = int(time.time())
        claims = {
            "iss": s.jwt_issuer,
            "sub": member,
            "aud": [identifier],
            "iat": now,
            "exp": now + s.jwt_ttl,
        }
        return jwt.encode(claims, private_key_pem, algorithm=s.jwt_algorithm)
