from typing import Optional
from sqlalchemy import or_
from sqlalchemy.orm import Session
from app.models.models import Staff


class StaffRepository:
    def __init__(self, db: Session):
        self.db = db

    def find_all(self, keyword: Optional[str] = None, roles: Optional[list[int]] = None) -> list[Staff]:
        # 論理削除含む全件（status表示のため）
        q = self.db.query(Staff)
        if keyword:
            like = f"%{keyword}%"
            q = q.filter(or_(Staff.name.like(like), Staff.email.like(like)))
        if roles:
            q = q.filter(Staff.role.in_(roles))
        return q.order_by(Staff.id).all()

    def find_by_provider(self, provider: int, provider_id: str) -> Optional[Staff]:
        return self.db.query(Staff).filter(
            Staff.provider == provider,
            Staff.provider_id == provider_id,
        ).first()

    def find_by_id(self, staff_id: int) -> Optional[Staff]:
        return self.db.query(Staff).filter(Staff.id == staff_id, Staff.deleted_at.is_(None)).first()

    def find_all_active(self) -> list[Staff]:
        return self.db.query(Staff).filter(Staff.deleted_at.is_(None)).all()

    def save(self, staff: Staff) -> Staff:
        self.db.add(staff)
        self.db.commit()
        self.db.refresh(staff)
        return staff

    def update_role(self, staff: Staff, role: int) -> None:
        staff.role = role
        self.db.commit()

    def soft_delete(self, staff: Staff) -> None:
        from datetime import datetime, timezone
        staff.deleted_at = datetime.now(timezone.utc)
        self.db.commit()

    def restore(self, staff: Staff) -> None:
        staff.deleted_at = None
        self.db.commit()
