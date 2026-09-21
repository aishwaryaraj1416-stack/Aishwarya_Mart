package com.aishwarya.aishwarya_mart.listener;

import com.aishwarya.aishwarya_mart.util.DBUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;

@WebListener
public class DbContextListener implements ServletContextListener {

    private HikariDataSource dataSource;

    @Override
    public void contextInitialized(ServletContextEvent event) {

        try {
            HikariConfig config = new HikariConfig();

            config.setJdbcUrl(DBUtil.getDatabaseUrl());
            config.setUsername(DBUtil.getDatabaseUsername());
            config.setPassword(DBUtil.getDatabasePassword());

            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(10000);

            dataSource = new HikariDataSource(config);

            DBUtil.setDataSource(dataSource);

            try (Connection connection = dataSource.getConnection()) {

                executeSqlFile(connection, "schema.sql");
                executeSqlFile(connection, "seed.sql");
            }

            event.getServletContext().setAttribute(
                    "dataSource",
                    dataSource
            );

            System.out.println(
                    "========== AISHWARYA MART DATABASE INITIALIZED =========="
            );

        } catch (Exception e) {

            System.err.println(
                    "========== AISHWARYA MART DATABASE ERROR =========="
            );

            e.printStackTrace();

            if (dataSource != null) {
                dataSource.close();
            }

            throw new RuntimeException(
                    "Failed to initialize database connection pool",
                    e
            );
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {

        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();

            System.out.println(
                    "========== AISHWARYA MART DATABASE POOL CLOSED =========="
            );
        }
    }

    private void executeSqlFile(
            Connection connection,
            String fileName) throws Exception {

        try (InputStream input = getClass()
                .getClassLoader()
                .getResourceAsStream(fileName)) {

            if (input == null) {
                throw new RuntimeException(
                        fileName + " not found"
                );
            }

            String sql = new String(
                    input.readAllBytes(),
                    StandardCharsets.UTF_8
            );

            StringBuilder cleanedSql = new StringBuilder();

            for (String line : sql.split("\\R")) {

                String trimmed = line.trim();

                if (!trimmed.startsWith("--")) {
                    cleanedSql.append(line).append('\n');
                }
            }

            String[] commands =
                    cleanedSql.toString().split(";");

            for (String command : commands) {

                command = command.trim();

                if (!command.isEmpty()) {

                    try (Statement statement =
                                 connection.createStatement()) {

                        statement.execute(command);
                    }
                }
            }
        }
    }
}