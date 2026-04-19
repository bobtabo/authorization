import time
from jose import jwt, JWTError

from app.config.settings import get_settings
from app.domain.client.repository import ClientRepository
from app.exceptions import unauthorized, not_found, internal
from app.infrastructure.cache.gate_cache_repository import GateCacheRepository
from app.usecase.gate.dto import GateIssueDto, GateVerifyDto


class GateInteractor:
    """Gate のユースケース実装。"""

    def __init__(self, client_repo: ClientRepository, cache_repo: GateCacheRepository):
        self.client_repo = client_repo
        self.cache_repo = cache_repo
        self.settings = get_settings()

    def issue_token(self, dto: GateIssueDto) -> str:
        client = self.client_repo.find_client_by_token(dto.access_token)
        if client is None:
            raise unauthorized("invalid_token")
        if client.private_key is None:
            raise internal("private_key_not_found")

        cached = self.cache_repo.get_jwt(client.identifier, dto.member)
        if cached:
            return cached

        token = self._issue_jwt(client.private_key, client.identifier, dto.member)
        self.cache_repo.put_jwt(client.identifier, dto.member, token)
        return token

    def _issue_jwt(self, private_key_pem: str, identifier: str, member: str) -> str:
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

    def verify(self, dto: GateVerifyDto) -> dict:
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

        return {
            "identifier": dto.identifier,
            "member": payload.get("sub"),
            "fingerprint": client.fingerprint,
            "payload": payload,
        }
