package com.example.demo.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Primary
    @Bean(name = "neonDataSource")
    public DataSource neonDataSource() {
        return DataSourceBuilder.create()
                .url(System.getenv("NEON_DB_URL"))
                .username(System.getenv("NEON_DB_USER"))
                .password(System.getenv("NEON_DB_PASSWORD"))
                .driverClassName("org.postgresql.Driver")
                .build();
    }

    @Bean(name = "supabaseDataSource")
    public DataSource supabaseDataSource() {
        return DataSourceBuilder.create()
                .url(System.getenv("SUPABASE_DB_URL"))
                .username(System.getenv("SUPABASE_DB_USER"))
                .password(System.getenv("SUPABASE_DB_PASSWORD"))
                .driverClassName("org.postgresql.Driver")
                .build();
    }

    @Bean(name = "neonJdbcTemplate")
    public JdbcTemplate neonJdbcTemplate(@Qualifier("neonDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "supabaseJdbcTemplate")
    public JdbcTemplate supabaseJdbcTemplate(@Qualifier("supabaseDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}