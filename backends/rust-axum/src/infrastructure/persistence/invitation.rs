use sqlx::MySqlPool;

pub struct SqlxInvitationRepository {
    pool: MySqlPool,
}

impl SqlxInvitationRepository {
    pub fn new(pool: MySqlPool) -> Self {
        Self { pool }
    }
}
