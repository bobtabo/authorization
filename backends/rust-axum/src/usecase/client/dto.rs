pub struct StoreDto {
    pub name:        String,
    pub post_code:   String,
    pub pref:        String,
    pub city:        String,
    pub address:     String,
    pub building:    String,
    pub tel:         String,
    pub email:       String,
    pub executor_id: u32,
}

pub struct UpdateDto {
    pub id:          u64,
    pub name:        Option<String>,
    pub post_code:   Option<String>,
    pub pref:        Option<String>,
    pub city:        Option<String>,
    pub address:     Option<String>,
    pub building:    Option<String>,
    pub tel:         Option<String>,
    pub email:       Option<String>,
    pub status:      Option<i32>,
    pub executor_id: u32,
}

pub struct ListConditionDto {
    pub keyword:    Option<String>,
    pub start_from: Option<String>,
    pub start_to:   Option<String>,
    pub statuses:   Vec<i32>,
}
