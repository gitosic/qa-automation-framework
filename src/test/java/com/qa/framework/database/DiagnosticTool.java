package com.qa.framework.database;

import com.qa.framework.config.ConfigurationManager;

public class DiagnosticTool {

    public static void main(String[] args) {
        System.out.println("=== Database Diagnostic Tool ===");
        System.out.println("This tool helps verify database connection and data\n");

        try {
            // 1. Показываем конфигурацию
            ConfigurationManager.printConfig();

            // 2. Подключаемся
            DatabaseConnection db = DatabaseConnection.getInstance();

            // 3. Проверяем таблицы
            String schema = ConfigurationManager.getDbSchema();
            System.out.println("\n--- Checking tables in schema: " + schema + " ---");

            String tablesSQL = String.format(
                    "SELECT table_name FROM information_schema.tables " +
                            "WHERE table_schema = '%s' ORDER BY table_name",
                    schema
            );

            var tables = DatabaseUtil.query(tablesSQL);

            if (tables.isEmpty()) {
                System.out.println("❌ No tables found in schema: " + schema);
                System.out.println("\nPossible solutions:");
                System.out.println("1. Check if schema name is correct");
                System.out.println("2. Create tables manually:");
                System.out.println("   CREATE TABLE " + schema + ".users (id SERIAL PRIMARY KEY, username VARCHAR(50), email VARCHAR(100))");
            } else {
                System.out.println("✅ Found " + tables.size() + " tables:");
                for (var table : tables) {
                    System.out.println("  - " + table.get("table_name"));
                }

                // 4. Проверяем таблицу users
                System.out.println("\n--- Checking users table ---");
                String usersSQL = String.format("SELECT COUNT(*) as count FROM %s.users", schema);
                var userCount = DatabaseUtil.query(usersSQL);

                long count = (long) userCount.get(0).get("count");
                System.out.println("Users in table: " + count);

                if (count > 0) {
                    // Показываем первых 5 пользователей
                    String sampleSQL = String.format(
                            "SELECT id, username, email FROM %s.users LIMIT 5",
                            schema
                    );
                    var users = DatabaseUtil.query(sampleSQL);

                    System.out.println("\nSample users:");
                    for (var user : users) {
                        System.out.println(String.format("  ID: %-3s | Username: %-20s | Email: %s",
                                user.get("id"),
                                user.get("username"),
                                user.get("email")
                        ));
                    }
                }
            }

            System.out.println("\n=== Diagnostic Complete ===");

        } catch (Exception e) {
            System.err.println("\n❌ ERROR: " + e.getMessage());
            e.printStackTrace();
            System.out.println("\nTroubleshooting:");
            System.out.println("1. Check if PostgreSQL is running");
            System.out.println("2. Verify username/password in st.properties");
            System.out.println("3. Check database URL");
        }
    }
}