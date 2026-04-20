use sqlx::MySqlPool;

pub struct SqlxClientRepository {
    pool: MySqlPool,
}

impl SqlxClientRepository {
    pub fn new(pool: MySqlPool) -> Self {
        Self { pool }
    }
}
