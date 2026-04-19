from typing import Optional
from datetime import datetime, timezone
from sqlalchemy import or_
from sqlalchemy.orm import Session

from app.domain.staff.entity import Staff
from app.domain.staff.condition import StaffCondition
from app.domain.staff.repository import StaffRepository
from app.infrastructure.model.model import StaffModel


def _to_entity(m: StaffModel) -> Staff:
    return Staff(
        id=m.id,
        name=m.name,
        email=m.email,
        provider=m.provider,
        provider_id=m.provider_id,
        avatar=m.avatar,
        role=m.role,
        created_at=m.created_at,
        updated_at=m.updated_at,
        deleted_at=m.deleted_at,
    )


class SqlAlchemyStaffRepository(StaffRepository):
    """StaffRepository の SQLAlchemy 実装。"""

    def __init__(self, db: Session):
        self.db = db

    def find_all_staffs(self, cond: StaffCondition) -> list[Staff]:
        q = self.db.query(StaffModel)
        if cond.keyword:
            like = f"%{cond.keyword}%"
            q = q.filter(or_(StaffModel.name.like(like), StaffModel.email.like(like)))
        if cond.roles:
            q = q.filter(StaffModel.role.in_(cond.roles))
        return [_to_entity(m) for m in q.order_by(StaffModel.id).all()]

    def find_staff_by_id(self, staff_id: int) -> Optional[Staff]:
        m = self.db.query(StaffModel).filter(
            StaffModel.id == staff_id,
            StaffModel.deleted_at.is_(None),
        ).first()
        return _to_entity(m) if m else None

    def find_staff_by_id_include_deleted(self, staff_id: int) -> Optional[Staff]:
        m = self.db.query(StaffModel).filter(StaffModel.id == staff_id).first()
        return _to_entity(m) if m else None

    def find_staff_by_provider(self, provider: int, provider_id: str) -> Optional[Staff]:
        m = self.db.query(StaffModel).filter(
            StaffModel.provider == provider,
            StaffModel.provider_id == provider_id,
        ).first()
        return _to_entity(m) if m else None

    def find_all_active_staffs(self) -> list[Staff]:
        return [
            _to_entity(m)
            for m in self.db.query(StaffModel).filter(StaffModel.deleted_at.is_(None)).all()
        ]

    def save_staff(self, staff: Staff) -> Staff:
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
        m.deleted_at = staff.deleted_at

        self.db.add(m)
        self.db.commit()
        self.db.refresh(m)
        return _to_entity(m)

    def update_staff_role(self, staff: Staff, role: int) -> None:
        m = self.db.query(StaffModel).filter(StaffModel.id == staff.id).first()
        if m:
            m.role = role
            self.db.commit()

    def soft_delete_staff(self, staff: Staff) -> None:
        m = self.db.query(StaffModel).filter(StaffModel.id == staff.id).first()
        if m:
            m.deleted_at = datetime.now(timezone.utc)
            self.db.commit()

    def restore_staff(self, staff: Staff) -> None:
        m = self.db.query(StaffModel).filter(StaffModel.id == staff.id).first()
        if m:
            m.deleted_at = None
            self.db.commit()
