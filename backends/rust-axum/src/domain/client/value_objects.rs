use chrono::NaiveDateTime;

pub struct ListItem {
    pub id:         u64,
    pub name:       String,
    pub status:     i32,
    pub start_at:   Option<NaiveDateTime>,
    pub stop_at:    Option<NaiveDateTime>,
    pub created_at: NaiveDateTime,
    pub updated_at: NaiveDateTime,
}

pub struct DetailVo {
    pub id:          u64,
    pub name:        String,
    pub identifier:  String,
    pub post_code:   String,
    pub pref:        String,
    pub city:        String,
    pub address:     String,
    pub building:    String,
    pub tel:         String,
    pub email:       String,
    pub status:      i32,
    pub start_at:    Option<NaiveDateTime>,
    pub stop_at:     Option<NaiveDateTime>,
    pub created_at:  NaiveDateTime,
    pub updated_at:  NaiveDateTime,
}
