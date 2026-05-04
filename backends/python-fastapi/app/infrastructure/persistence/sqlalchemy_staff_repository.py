"""
スタッフリポジトリ SQLAlchemy 実装モジュール。

Author: Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
"""
from typing import Optional

from datetime import datetime, timezone
from sqlalchemy import or_
from sqlalchemy.orm import Session

from app.domain.staff.entity import Staff
from app.domain.staff.condition import StaffCondition
from app.domain.staff.repository import StaffRepository
from app.infrastructure.model.model import StaffModel
from app.support.assign import assign


def _to_entity(m: StaffModel) -> Staff:
    return assign(Staff(), m)


class SqlAlchemyStaffRepository(StaffRepository):
    """StaffRepository の SQLAlchemy 実装。

    Attributes:
        db: SQLAlchemy セッション
    """

    def __init__(self, db: Session):
        """初期化します。

        Args:
            db: SQLAlchemy セッション
        """
        self.db = db

    def find_all_staffs(self, cond: StaffCondition) -> list[Staff]:
        """検索条件に合致するスタッフエンティティを返します。

        Args:
            cond: 検索条件

        Returns:
            スタッフエンティティのリスト
        """
        q = self.db.query(StaffModel)
        if cond.keyword:
            like = f"%{cond.keyword}%"
            q = q.filter(or_(StaffModel.name.like(like), StaffModel.email.like(like)))
        if cond.roles:
            q = q.filter(StaffModel.role.in_(cond.roles))
        return [_to_entity(m) for m in q.order_by(StaffModel.id).all()]

    def find_staff_by_id(self, staff_id: int) -> Optional[Staff]:
        """IDで有効なスタッフエンティティを返します。存在しない場合は None を返します。

        Args:
            staff_id: スタッフID

        Returns:
            スタッフエンティティ、または None
        """
        m = self.db.query(StaffModel).filter(
            StaffModel.id == staff_id,
            StaffModel.deleted_at.is_(None),
        ).first()
        return _to_entity(m) if m else None

    def find_staff_by_id_include_deleted(self, staff_id: int) -> Optional[Staff]:
        """IDで削除済みを含むスタッフエンティティを返します。

        Args:
            staff_id: スタッフID

        Returns:
            スタッフエンティティ、または None
        """
        m = self.db.query(StaffModel).filter(StaffModel.id == staff_id).first()
        return _to_entity(m) if m else None

    def find_staff_by_provider(self, provider: int, provider_id: str) -> Optional[Staff]:
        """プロバイダーとプロバイダーIDでスタッフエンティティを返します。

        Args:
            provider: プロバイダー種別
            provider_id: プロバイダーのユーザーID

        Returns:
            スタッフエンティティ、または None
        """
        m = self.db.query(StaffModel).filter(
            StaffModel.provider == provider,
            StaffModel.provider_id == provider_id,
        ).first()
        return _to_entity(m) if m else None

    def find_all_active_staffs(self) -> list[Staff]:
        """論理削除されていないスタッフエンティティを全件返します。

        Returns:
            有効なスタッフエンティティのリスト
        """
        return [
            _to_entity(m)
            for m in self.db.query(StaffModel).filter(StaffModel.deleted_at.is_(None)).all()
        ]

    def save_staff(self, staff: Staff) -> Staff:
        """スタッフエンティティを保存（新規作成または更新）して返します。

        Args:
            staff: 保存するスタッフエンティティ

        Returns:
            保存済みスタッフエンティティ

        Raises:
            ValueError: 更新対象のスタッフが存在しない場合
        """
        if staff.id:
            m = self.db.query(StaffModel).filter(StaffModel.id == staff.id).first()
            if m is None:
                raise ValueError(f"Staff {staff.id} not found")
        else:
            m = StaffModel()

        m.name = staff.name
        m.email = staff.email
        m.provider = staff.provider
        m.provider_id = staff.provider_id
        m.avatar = staff.avatar
        m.role = staff.role
        m.last_login_at = staff.last_login_at
        m.deleted_at = staff.deleted_at

        self.db.add(m)
        self.db.flush()
        self.db.refresh(m)
        return _to_entity(m)

    def update_staff_role(self, staff: Staff, role: int) -> None:
        """スタッフのロールを更新します。

        Args:
            staff: 更新対象のスタッフエンティティ
            role: 新しいロール値
        """
        m = self.db.query(StaffModel).filter(StaffModel.id == staff.id).first()
        if m:
            m.role = role
            self.db.flush()

    def soft_delete_staff(self, staff: Staff) -> None:
        """スタッフを論理削除します。

        Args:
            staff: 削除対象のスタッフエンティティ
        """
        m = self.db.query(StaffModel).filter(StaffModel.id == staff.id).first()
        if m:
            m.deleted_at = datetime.now(timezone.utc)
            self.db.flush()

    def restore_staff(self, staff: Staff) -> None:
        """スタッフの論理削除を復元します。

        Args:
            staff: 復元対象のスタッフエンティティ
        """
        m = self.db.query(StaffModel).filter(StaffModel.id == staff.id).first()
        if m:
            m.deleted_at = None
            self.db.flush()
