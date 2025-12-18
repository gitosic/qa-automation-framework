package com.qa.framework.database;

import com.qa.framework.config.ConfigurationManager;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DatabaseConnectionTest {

    @Test
    void testDatabaseConnection() {
        System.out.println("=== Testing Database Connection ===");

        // Выводим конфигурацию
        ConfigurationManager.printConfig();

        // Подключаемся
        DatabaseConnection db = DatabaseConnection.getInstance();

        // Тест: проверяем что можем выполнить простой запрос
        try {
            var result = DatabaseUtil.query("SELECT 1 as test_value");
            assertThat(result).hasSize(1);
            assertThat(result.get(0).get("test_value")).isEqualTo(1);

            System.out.println("✅ Basic query test passed");

        } catch (Exception e) {
            System.err.println("❌ Query test failed: " + e.getMessage());
            throw e;
        }

        System.out.println("=== Connection Test Complete ===\n");
    }
}