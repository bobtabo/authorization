"""
クライアントユースケース Interactor モジュール。

Author: Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
"""
import hashlib
import secrets
import struct
from base64 import b64encode
from cryptography.hazmat.primitives.asymmetric import rsa
from cryptography.hazmat.primitives import serialization
from datetime import datetime, timezone
from typing import Optional

from app.domain.client.entity import Client
from app.domain.client.condition import ClientCondition
from app.domain.client.repository import ClientRepository
from app.domain.client.value_objects import ClientListItem, ClientDetailVo, ClientStoreResultVo, ClientQrVo, ClientInfoVo, ClientStartVo
from app.exceptions import conflict, internal, not_found
from app.usecase.client.dto import ClientStoreDto, ClientUpdateDto, ClientIdentifierDto


def _rsa_fingerprint(private_key) -> str:
    """PHP と同一方式で SSH wire format SHA256 フィンガープリントを生成します。

    Args:
        private_key: RSA 秘密鍵オブジェクト

    Returns:
        "SHA256:..." 形式のフィンガープリント文字列
    """
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


def _to_list_item(c: Client) -> ClientListItem:
    """クライアントエンティティを一覧用 Vo に変換します。

    Args:
        c: クライアントエンティティ

    Returns:
        ClientListItem インスタンス
    """
    return ClientListItem(
        id=c.id,
        name=c.name,
        identifier=c.identifier,
        status=c.status,
        started_at=c.started_at,
        stopped_at=c.stopped_at,
        created_at=c.created_at,
        updated_at=c.updated_at,
    )


def _to_detail_vo(c: Client) -> ClientDetailVo:
    """クライアントエンティティを詳細用 Vo に変換します。

    Args:
        c: クライアントエンティティ

    Returns:
        ClientDetailVo インスタンス
    """
    return ClientDetailVo(
        id=c.id,
        name=c.name,
        identifier=c.identifier,
        post_code=c.post_code,
        pref=c.pref,
        city=c.city,
        address=c.address,
        building=c.building,
        tel=c.tel,
        email=c.email,
        status=c.status,
        fingerprint=c.fingerprint,
        started_at=c.started_at,
        stopped_at=c.stopped_at,
        created_at=c.created_at,
        updated_at=c.updated_at,
    )


class ClientInteractor:
    """クライアントのユースケース実装。

    Attributes:
        repository: クライアントリポジトリ
    """

    def __init__(self, repo: ClientRepository):
        """初期化します。

        Args:
            repo: クライアントリポジトリ
        """
        self.repository = repo

    def authenticate_by_token(self, token: str) -> bool:
        """Bearerトークンでクライアントを認証します。

        Args:
            token: アクセストークン

        Returns:
            認証成功の場合 True
        """
        return self.repository.find_client_by_token(token) is not None

    def find_all(
        self,
        keyword: Optional[str] = None,
        status: Optional[int] = None,
        offset: int = 0,
        limit: int = 10,
        sort: Optional[str] = None,
        sort_type: Optional[str] = None,
    ) -> tuple[list[ClientListItem], int]:
        """検索条件に合致するクライアント一覧の Vo を返します。

        Args:
            keyword: キーワード検索文字列
            status: ステータスフィルター
            offset: 取得開始位置
            limit: 取得件数
            sort: ソート対象
            sort_type: ソート順

        Returns:
            (ClientListItem のリスト, 総件数) のタプル
        """
        cond = ClientCondition(
            keyword=keyword,
            status=status,
            offset=offset,
            limit=limit,
            sort=sort,
            sort_type=sort_type,
        )
        count = self.repository.count_clients(cond)
        clients = self.repository.find_all_clients(cond)
        return [_to_list_item(c) for c in clients], count

    def find_by_id(self, client_id: int) -> ClientDetailVo:
        """IDでクライアント詳細の Vo を返します。

        Args:
            client_id: クライアントID

        Returns:
            ClientDetailVo インスタンス

        Raises:
            AppException: クライアントが存在しない場合
        """
        client = self.repository.find_client_by_id(client_id)
        if client is None:
            raise not_found("client_not_found")
        return _to_detail_vo(client)

    def store(self, dto: ClientStoreDto) -> ClientStoreResultVo:
        """クライアントを新規登録し、登録結果の Vo を返します。RSA鍵ペア・アクセストークンを自動生成します。

        Args:
            dto: クライアント登録 Dto

        Returns:
            ClientStoreResultVo インスタンス（メール送信・通知配信に使用）
        """
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
        saved = self.repository.save_client(client)
        return ClientStoreResultVo(
            id=saved.id,
            name=saved.name,
            identifier=saved.identifier,
            email=saved.email,
            token=saved.token or "",
        )

    def update(self, dto: ClientUpdateDto) -> ClientDetailVo:
        """クライアントを更新し、更新後の詳細 Vo を返します。

        Args:
            dto: クライアント更新 Dto

        Returns:
            ClientDetailVo インスタンス

        Raises:
            AppException: クライアントが存在しない場合
        """
        client = self.repository.find_client_by_id(dto.client_id)
        if client is None:
            raise not_found("client_not_found")
        if dto.version is not None and dto.version != client.version:
            raise conflict("optimistic_lock")

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

        saved = self.repository.save_client(client)
        return _to_detail_vo(saved)

    def destroy(self, client_id: int) -> None:
        """クライアントをステータス Closed(4) に更新してから論理削除します。

        Args:
            client_id: クライアントID

        Raises:
            AppException: クライアントが存在しない場合
        """
        client = self.repository.find_client_by_id(client_id)
        if client is None:
            raise not_found("client_not_found")
        client.status = 4
        saved = self.repository.save_client(client)
        self.repository.soft_delete_client(saved)

    def get_qr(self, dto: ClientIdentifierDto) -> ClientQrVo:
        """QRコードデータを返します。

        Args:
            dto: identifier を含む Dto

        Returns:
            ClientQrVo インスタンス

        Raises:
            AppException: クライアントが存在しない場合
        """
        client = self.repository.find_client_by_identifier(dto.identifier)
        if client is None:
            raise not_found("client_not_found")
        deeplink_url = f"authgateway://clients/{client.identifier}/info"
        return ClientQrVo(identifier=client.identifier, deeplink_url=deeplink_url)

    def get_info(self, dto: ClientIdentifierDto) -> ClientInfoVo:
        """スマホアプリ向けクライアント情報を返します。

        Args:
            dto: identifier を含む Dto

        Returns:
            ClientInfoVo インスタンス

        Raises:
            AppException: クライアントが存在しない場合
        """
        client = self.repository.find_client_by_identifier(dto.identifier)
        if client is None:
            raise not_found("client_not_found")
        return ClientInfoVo(identifier=client.identifier, name=client.name, status=client.status)

    def start(self, dto: ClientIdentifierDto) -> ClientStartVo:
        """利用開始処理を行い、アクセストークンを返します。
        Active 以外の場合は Active に遷移します。既に Active の場合もトークンを返します。

        Args:
            dto: identifier を含む Dto

        Returns:
            ClientStartVo インスタンス

        Raises:
            AppException: クライアントが存在しない場合
        """
        client = self.repository.find_client_by_identifier(dto.identifier)
        if client is None:
            raise not_found("client_not_found")

        if client.status != 2:  # Active 以外
            now = datetime.now(timezone.utc)
            client.status = 2  # Active
            if client.started_at is None:
                client.started_at = now
            client.stopped_at = None
            client = self.repository.save_client(client)

        if not client.token:
            raise internal("client_token_missing")
        return ClientStartVo(access_token=client.token)

    def stop(self, dto: ClientIdentifierDto) -> None:
        """利用停止処理を行います。
        Active の場合は Suspended に遷移します。Active 以外は何もしません。

        Args:
            dto: identifier を含む Dto

        Raises:
            AppException: クライアントが存在しない場合
        """
        client = self.repository.find_client_by_identifier(dto.identifier)
        if client is None:
            raise not_found("client_not_found")

        if client.status == 2:  # Active
            now = datetime.now(timezone.utc)
            client.status = 3  # Suspended
            client.stopped_at = now
            self.repository.save_client(client)
