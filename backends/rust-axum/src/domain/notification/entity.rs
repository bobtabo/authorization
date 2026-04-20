use chrono::NaiveDateTime;

pub struct Notification {
    pub id:           u64,
    pub staff_id:     u32,
    pub message_type: i32,
    pub title:        String,
    pub message:      String,
    pub url:          Option<String>,
    pub read:         bool,
    pub created_at:   NaiveDateTime,
    pub created_by:   Option<u32>,
    pub updated_at:   NaiveDateTime,
    pub updated_by:   Option<u32>,
    pub deleted_at:   Option<NaiveDateTime>,
    pub deleted_by:   Option<u32>,
    pub version:      i32,
}
