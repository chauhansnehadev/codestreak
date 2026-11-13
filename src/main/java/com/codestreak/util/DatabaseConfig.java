package com.codestreak.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConfig {

    private static HikariDataSource dataSource;

    static {
        try {
            HikariConfig config = new HikariConfig();

            // Load properties if available, fallback to environment / defaults
            Properties props = new Properties();
            try (InputStream is = DatabaseConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (is != null) {
                    props.load(is);
                }
            } catch (Exception ignored) {}

            // Support Render's full DATABASE_URL or individual credentials
            String envDbUrl = System.getenv("DATABASE_URL");
            String jdbcUrl;

            if (envDbUrl != null && !envDbUrl.isBlank()) {
                // If DATABASE_URL starts with postgres://, convert to jdbc:postgresql://
                if (envDbUrl.startsWith("postgres://")) {
                    jdbcUrl = envDbUrl.replace("postgres://", "jdbc:postgresql://");
                } else if (envDbUrl.startsWith("postgresql://")) {
                    jdbcUrl = envDbUrl.replace("postgresql://", "jdbc:postgresql://");
                } else {
                    jdbcUrl = envDbUrl;
                }
                config.setJdbcUrl(jdbcUrl);
            } else {
                String host = System.getenv().getOrDefault("DB_HOST", props.getProperty("db.host", "localhost"));
                String port = System.getenv().getOrDefault("DB_PORT", props.getProperty("db.port", "5432"));
                String dbName = System.getenv().getOrDefault("DB_NAME", props.getProperty("db.name", "codestreak"));
                String user = System.getenv().getOrDefault("DB_USER", props.getProperty("db.user", System.getProperty("user.name", "snehachauhan")));
                String password = System.getenv().getOrDefault("DB_PASS", props.getProperty("db.password", ""));

                jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + dbName;
                config.setJdbcUrl(jdbcUrl);
                config.setUsername(user);
                config.setPassword(password);
            }

            // Also check for separate DB_USER/DB_PASS overrides if provided
            if (System.getenv("DB_USER") != null) config.setUsername(System.getenv("DB_USER"));
            if (System.getenv("DB_PASS") != null) config.setPassword(System.getenv("DB_PASS"));

            config.setDriverClassName("org.postgresql.Driver");

            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(30000);
            config.setConnectionTimeout(20000);
            config.setMaxLifetime(1800000);
            config.setPoolName("CodeStreakHikariPool");

            // Recommended PostgreSQL connection properties
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            dataSource = new HikariDataSource(config);
        } catch (Exception e) {
            System.err.println("Failed to initialize HikariCP Database Connection Pool: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private DatabaseConfig() {}

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Database connection pool is not initialized.");
        }
        return dataSource.getConnection();
    }

    public static void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
