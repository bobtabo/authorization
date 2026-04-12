import hashlib
import secrets
import struct
from base64 import b64encode
from cryptography.hazmat.primitives.asymmetric import rsa, padding
from cryptography.hazmat.primitives import serialization
from datetime import datetime, timezone
from typing import Optional
from app.exceptions import not_found, conflict
from app.models.models import Client
from app.repositories.client_repo import ClientRepository


def _rsa_fingerprint(public_key) -> str:
    pub_numbers = public_key.public_key().public_numbers()
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


class ClientService:
    def __init__(self, client_repo: ClientRepository):
        self.repo = client_repo

    def authenticate_by_token(self, token: str) -> Optional[Client]:
        return self.repo.find_by_access_token(token)

    def find_all(self, keyword: Optional[str] = None, status: Optional[int] = None) -> list[Client]:
        return self.repo.find_all(keyword=keyword, status=status)

    def find_by_id(self, client_id: int) -> Client:
        client = self.repo.find_by_id(client_id)
        if client is None:
            raise not_found("client_not_found")
        return client

    def store(self, name: str, identifier: str, **kwargs) -> Client:
        existing = self.repo.find_by_identifier(identifier)
        if existing:
            raise conflict("identifier_already_exists")

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
            name=name,
            identifier=identifier,
            token=token,
            public_key=pub_pem,
            private_key=priv_pem,
            fingerprint=fingerprint,
            **kwargs,
        )
        return self.repo.save(client)

    def update(self, client_id: int, **kwargs) -> Client:
        client = self.find_by_id(client_id)
        new_status = kwargs.pop("status", None)

        for k, v in kwargs.items():
            if v is not None:
                setattr(client, k, v)

        if new_status is not None and new_status != client.status:
            now = datetime.now(timezone.utc)
            if new_status == 1:   # Active
                client.started_at = now
            elif new_status == 2:  # Suspended
                client.stopped_at = now
            client.status = new_status

        return self.repo.save(client)

    def destroy(self, client_id: int) -> None:
        client = self.find_by_id(client_id)
        client.status = 4
        self.repo.save(client)
        self.repo.soft_delete(client)
