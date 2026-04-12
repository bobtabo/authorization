import time
from cryptography.hazmat.primitives.serialization import load_pem_private_key, load_pem_public_key
from jose import jwt, JWTError
from app.config.settings import get_settings
from app.exceptions import unauthorized, not_found, internal
from app.repositories.client_repo import ClientRepository
from app.repositories.gate_cache_repo import GateCacheRepository


class GateService:
    def __init__(self, client_repo: ClientRepository, cache_repo: GateCacheRepository):
        self.client_repo = client_repo
        self.cache_repo = cache_repo
        self.settings = get_settings()

    def issue_token(self, access_token: str, member: str) -> str:
        client = self.client_repo.find_by_access_token(access_token)
        if client is None:
            raise unauthorized("invalid_token")
        if client.private_key is None:
            raise internal("private_key_not_found")

        cached = self.cache_repo.get_jwt(client.identifier, member)
        if cached:
            return cached

        token = self._issue_jwt(client.private_key, client.identifier, member)
        self.cache_repo.put_jwt(client.identifier, member, token)
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
        private_key = load_pem_private_key(private_key_pem.encode(), password=None)
        return jwt.encode(claims, private_key, algorithm=s.jwt_algorithm)

    def verify(self, identifier: str, token: str) -> dict:
        client = self.client_repo.find_by_identifier(identifier)
        if client is None:
            raise not_found("client_not_found")
        if client.public_key is None:
            raise internal("public_key_not_found")

        try:
            public_key = load_pem_public_key(client.public_key.encode())
            payload = jwt.decode(
                token,
                public_key,
                algorithms=[self.settings.jwt_algorithm],
                audience=identifier,
            )
        except JWTError as e:
            raise unauthorized(str(e))

        return {
            "identifier": identifier,
            "member": payload.get("sub"),
            "fingerprint": client.fingerprint,
            "payload": payload,
        }
