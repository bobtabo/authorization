use crate::config::Config;
use sqlx::mysql::MySqlPool;

pub async fn new(cfg: &Config) -> Result<MySqlPool, sqlx::Error> {
    MySqlPool::connect(&cfg.db.dsn).await
}
