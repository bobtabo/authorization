use super::entity::Notification;

pub struct Page {
    pub items:       Vec<Notification>,
    pub next_cursor: Option<String>,
}
