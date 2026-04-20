use crate::config::Config;
use redis::Client;

pub fn new(cfg: &Config) -> redis::RedisResult<Client> {
    let url = if cfg.redis.password.is_empty() {
        format!("redis://{}/{}", cfg.redis.addr, cfg.redis.db)
    } else {
        format!("redis://:{}@{}/{}", cfg.redis.password, cfg.redis.addr, cfg.redis.db)
    };
    Client::open(url)
}
