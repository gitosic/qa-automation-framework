package com.qa.framework.tests_OLD.patterns.builder;

import com.qa.framework.core.TestBase;
import com.qa.framework.pages.LoginPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тест для демонстрации Builder Pattern через Lombok.
 * Показывает как создавать тестовые данные красиво.
 */
@DisplayName("Builder Pattern with Lombok")
public class BuilderPatternTest extends TestBase {

    /**
     * Демонстрация создания тестовых данных через Builder
     */
    @Test
    @DisplayName("Создание пользователей через Builder")
    void testCreateUsersWithBuilder() {
        System.out.println("\n=== Builder Pattern Demo ===");

        // 1. Создание администратора
        User admin = User.builder()
                .username("admin")
                .password("AdminPass123!")
                .role("Administrator")
                .email("admin@bank.com")
                .isActive(true)
                .build();

        System.out.println("1. Admin created: " + admin);
        assertEquals("Administrator", admin.getRole());
        assertTrue(admin.isActive());

        // 2. Создание обычного пользователя (используем значения по умолчанию)
        User regularUser = User.builder()
                .username("john_doe")
                .password("UserPass456")
                // role = "User" по умолчанию
                // isActive = true по умолчанию
                .build();

        System.out.println("2. Regular user: " + regularUser);
        assertEquals("User", regularUser.getRole()); // Значение по умолчанию

        // 3. Проверка обязательных полей (@NonNull)
        try {
            User.builder()
                    .password("test") // Нет username!
                    .build();
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            System.out.println("3. ✅ Validation works: username is required");
        }

        System.out.println("\n✅ Builder Pattern demonstration complete!");
    }

    /**
     * Практическое использование Builder в тесте логина
     */
    @Test
    @DisplayName("Использование Builder в тесте логина")
    void testLoginWithBuilder() {
        System.out.println("\n=== Practical Usage in Login Test ===");

        // Создаем тестового пользователя через Builder
        User testUser = User.builder()
                .username("test_user")
                .password("TestPassword789")
                .role("Tester")
                .email("tester@qa.com")
                .build();

        System.out.println("Testing login with: " + testUser.getUsername());

        // Используем в UI тесте
        openBankApp("/login");
        LoginPage loginPage = new LoginPage();
        loginPage.isLoaded();

        loginPage.enterUsername(testUser.getUsername())
                .enterPassword(testUser.getPassword())
                .clickLogin();

        System.out.println("Login performed with:");
        System.out.println("  Username: " + testUser.getUsername());
        System.out.println("  Role: " + testUser.getRole());
        System.out.println("  Email: " + testUser.getEmail());

        System.out.println("\n✅ Builder used in real test!");
    }

    /**
     * Сравнение Builder с традиционным подходом
     */
    @Test
    @DisplayName("Сравнение подходов")
    void testComparison() {
        System.out.println("\n=== Traditional vs Builder ===");

        // Традиционный подход (плохо читается)
        System.out.println("Traditional (hard to read):");
        System.out.println("new User(\"admin\", \"pass\", \"Admin\", \"email@test.com\", true)");

        // Builder подход (читается как предложение)
        System.out.println("\nBuilder (reads like English):");
        System.out.println("User.builder()");
        System.out.println("    .username(\"admin\")");
        System.out.println("    .password(\"pass\")");
        System.out.println("    .role(\"Admin\")");
        System.out.println("    .email(\"email@test.com\")");
        System.out.println("    .isActive(true)");
        System.out.println("    .build()");

        System.out.println("\n✅ Builder makes code more readable!");
    }
}