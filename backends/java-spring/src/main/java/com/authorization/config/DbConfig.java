/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * DataSource / jOOQ DSLContext の Bean 定義です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Configuration
public class DbConfig {

    /**
     * MySQL用の {@link DataSource} を組み立てます。
     *
     * @param cfg アプリケーション設定
     * @return DataSource
     */
    @Bean(destroyMethod = "close")
    public DataSource dataSource(AppConfig cfg) {
        AppConfig.Db db = cfg.db();
        String url = "jdbc:mysql://" + db.host() + ":" + db.port() + "/" + db.database()
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Tokyo";
        HikariConfig hikari = new HikariConfig();
        hikari.setJdbcUrl(url);
        hikari.setUsername(db.username());
        hikari.setPassword(db.password());
        hikari.setMaximumPoolSize(10);
        return new HikariDataSource(hikari);
    }

    /**
     * jOOQ用の {@link DSLContext} を組み立てます。
     *
     * @param dataSource DataSource
     * @return DSLContext
     */
    @Bean
    public DSLContext dslContext(DataSource dataSource) {
        return DSL.using(dataSource, SQLDialect.MYSQL);
    }
}
