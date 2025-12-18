package com.qa.framework.database;

import com.qa.framework.config.ConfigurationManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public final class DatabaseConnection {

    private static volatile DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        connect();
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    private void connect() {
        try {
            connection = DriverManager.getConnection(
                    ConfigurationManager.getDbUrl(),
                    ConfigurationManager.getDbUsername(),
                    ConfigurationManager.getDbPassword()
            );
            connection.setAutoCommit(true);

            try (Statement stmt = connection.createStatement()) {
                stmt.execute("SET search_path TO " + ConfigurationManager.getDbSchema());

                ResultSet rs = stmt.executeQuery(
                        "SELECT current_database(), current_schema()"
                );
                rs.next();

                System.out.println("✅ Connected to database");
                System.out.println("   Database: " + rs.getString(1));
                System.out.println("   Schema  : " + rs.getString(2));
            }

        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to connect to database", e);
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (Exception e) {
            throw new RuntimeException("❌ Database connection error", e);
        }
        return connection;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("🔌 Database connection closed");
            }
        } catch (Exception e) {
            System.err.println("❌ Error closing DB connection: " + e.getMessage());
        }
    }
}
