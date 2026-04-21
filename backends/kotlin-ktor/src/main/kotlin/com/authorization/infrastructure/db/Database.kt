package com.authorization.infrastructure.db

import com.authorization.config.Config
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database

fun initDatabase(cfg: Config): Database {
    val hikariConfig = HikariConfig().apply {
        jdbcUrl         = cfg.db.dsn
        driverClassName = "com.mysql.cj.jdbc.Driver"
        username        = cfg.db.username
        password        = cfg.db.password
        maximumPoolSize = 10
    }
    val dataSource = HikariDataSource(hikariConfig)
    return Database.connect(dataSource)
}
