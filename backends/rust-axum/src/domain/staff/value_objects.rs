use chrono::NaiveDateTime;

pub struct ListItem {
    pub id:         u32,
    pub name:       String,
    pub email:      String,
    pub role:       i32,
    pub status:     i32,
    pub created_at: NaiveDateTime,
    pub updated_at: NaiveDateTime,
}

pub struct Vo {
    pub id:     u32,
    pub name:   String,
    pub avatar: Option<String>,
    pub role:   i32,
}
