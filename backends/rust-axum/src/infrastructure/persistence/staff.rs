use sqlx::MySqlPool;

pub struct SqlxStaffRepository {
    pool: MySqlPool,
}

impl SqlxStaffRepository {
    pub fn new(pool: MySqlPool) -> Self {
        Self { pool }
    }
}
