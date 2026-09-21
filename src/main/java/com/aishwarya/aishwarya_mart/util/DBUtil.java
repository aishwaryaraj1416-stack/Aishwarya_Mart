package com.aishwarya.aishwarya_mart.util;

import com.zaxxer.hikari.HikariDataSource;

import java.io.InputStream;
import java.sql.Connection;
import java.util.Properties;

public class DBUtil {

    private static String URL;
    private static String USERNAME;
    private static String PASSWORD;

    private static HikariDataSource dataSource;

    static {
        try {
            Properties properties = new Properties();

            try (InputStream input = DBUtil.class
                    .getClassLoader()
                    .getResourceAsStream("db.properties")) {

                if (input == null) {
                    throw new RuntimeException(
                            "db.properties not found"
                    );
                }

                properties.load(input);
            }

            URL = getConfig(
                    "DB_URL",
                    properties.getProperty("db.url")
            );

            USERNAME = getConfig(
                    "DB_USERNAME",
                    properties.getProperty("db.username")
            );

            PASSWORD = getConfig(
                    "DB_PASSWORD",
                    properties.getProperty("db.password")
            );

            Class.forName(
                    properties.getProperty("db.driver")
            );

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to load database configuration",
                    e
            );
        }
    }

    private static String getConfig(
            String environmentVariable,
            String propertyValue) {

        String environmentValue =
                System.getenv(environmentVariable);

        if (environmentValue != null
                && !environmentValue.isBlank()) {

            return environmentValue;
        }

        return propertyValue;
    }

    public static String getDatabaseUrl() {
        return URL;
    }

    public static String getDatabaseUsername() {
        return USERNAME;
    }

    public static String getDatabasePassword() {
        return PASSWORD;
    }

    public static void setDataSource(
            HikariDataSource source) {

        dataSource = source;
    }

    public static Connection getConnection()
            throws Exception {

        if (dataSource == null) {
            throw new IllegalStateException(
                    "Database connection pool is not initialized"
            );
        }

        return dataSource.getConnection();
    }
}