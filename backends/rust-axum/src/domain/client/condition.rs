use chrono::NaiveDateTime;

pub struct Condition {
    pub keyword:    Option<String>,
    pub start_from: Option<NaiveDateTime>,
    pub start_to:   Option<NaiveDateTime>,
    pub statuses:   Vec<i32>,
}
