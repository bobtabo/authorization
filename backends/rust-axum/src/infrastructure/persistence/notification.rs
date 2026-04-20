use sqlx::MySqlPool;

pub struct SqlxNotificationRepository {
    pool: MySqlPool,
}

impl SqlxNotificationRepository {
    pub fn new(pool: MySqlPool) -> Self {
        Self { pool }
    }
}
