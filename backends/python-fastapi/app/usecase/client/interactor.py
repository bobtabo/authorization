import hashlib
import secrets
import struct
from base64 import b64encode
from cryptography.hazmat.primitives.asymmetric import rsa, padding
from cryptography.hazmat.primitives import serialization
from datetime import datetime, timezone
from typing import Optional

from app.domain.client.entity import Client
from app.domain.client.condition import ClientCondition
from app.domain.client.repository import ClientRepository
from app.exceptions import not_found, conflict
from app.usecase.client.dto import ClientStoreDto, ClientUpdateDto


def _rsa_fingerprint(private_key) -> str:
    pub_numbers = private_key.public_key().public_numbers()
    e = pub_numbers.e
    n = pub_numbers.n

    def mp_int(val: int) -> bytes:
        b = val.to_bytes((val.bit_length() + 7) // 8, "big")
        return struct.pack(">I", len(b)) + b

    def mp_str(s: str) -> bytes:
        b = s.encode()
        return struct.pack(">I", len(b)) + b

    wire = mp_str("ssh-rsa") + mp_int(e) + mp_int(n)
    digest = hashlib.sha256(wire).digest()
    b64 = b64encode(digest).decode().rstrip("=")
    return f"SHA256:{b64}"


class ClientInteractor:
    """クライアントのユースケース実装。"""

    def __init__(self, repo: ClientRepository):
        self.repo = repo

    def authenticate_by_token(self, token: str) -> Optional[Client]:
        return self.repo.find_client_by_token(token)

    def find_all(self, keyword: Optional[str] = None, status: Optional[int] = None) -> list[Client]:
        cond = ClientCondition(keyword=keyword, status=status)
        return self.repo.find_all_clients(cond)

    def find_by_id(self, client_id: int) -> Client:
        client = self.repo.find_client_by_id(client_id)
        if client is None:
            raise not_found("client_not_found")
        return client

    def store(self, dto: ClientStoreDto) -> Client:
        identifier = secrets.token_hex(8)

        private_key = rsa.generate_private_key(public_exponent=65537, key_size=4096)
        fingerprint = _rsa_fingerprint(private_key)
        pub_pem = private_key.public_key().public_bytes(
            serialization.Encoding.PEM, serialization.PublicFormat.SubjectPublicKeyInfo
        ).decode()
        priv_pem = private_key.private_bytes(
            serialization.Encoding.PEM,
            serialization.PrivateFormat.TraditionalOpenSSL,
            serialization.NoEncryption(),
        ).decode()
        token = secrets.token_hex(32)

        client = Client(
            name=dto.name,
            identifier=identifier,
            post_code=dto.post_code,
            pref=dto.pref,
            city=dto.city,
            address=dto.address,
            building=dto.building,
            tel=dto.tel,
            email=dto.email,
            token=token,
            public_key=pub_pem,
            private_key=priv_pem,
            fingerprint=fingerprint,
            executor_id=dto.executor_id,
        )
        return self.repo.save_client(client)

    def update(self, dto: ClientUpdateDto) -> Client:
        client = self.find_by_id(dto.client_id)

        if dto.name is not None:
            client.name = dto.name
        if dto.post_code is not None:
            client.post_code = dto.post_code
        if dto.pref is not None:
            client.pref = dto.pref
        if dto.city is not None:
            client.city = dto.city
        if dto.address is not None:
            client.address = dto.address
        if dto.building is not None:
            client.building = dto.building
        if dto.tel is not None:
            client.tel = dto.tel
        if dto.email is not None:
            client.email = dto.email

        if dto.status is not None and dto.status != client.status:
            now = datetime.now(timezone.utc)
            if dto.status == 2:   # Active
                client.started_at = now
            elif dto.status == 3:  # Suspended
                client.stopped_at = now
            client.status = dto.status

        return self.repo.save_client(client)

    def destroy(self, client_id: int) -> None:
        client = self.find_by_id(client_id)
        client.status = 4
        self.repo.save_client(client)
        self.repo.soft_delete_client(client)
