package com.qa.framework.tests_OLD.patterns.factory;

import com.qa.framework.core.TestBase;
import com.qa.framework.pages.LoginPage;
import org.junit.jupiter.api.Test;

public class TestFactoryExample extends TestBase {

    // Фабрика тестовых данных
    static class TestUserFactory {
        static TestUser createUser(String role) {
            switch (role) {
                case "ADMIN":
                    return new TestUser("admin", "admin123", "Администратор");
                case "USER":
                    return new TestUser("user", "password", "Пользователь");
                case "GUEST":
                    return new TestUser("guest", "guest", "Гость");
                default:
                    return new TestUser("test", "test", "Тестовый");
            }
        }
    }

    // Класс тестового пользователя
    static class TestUser {
        String username;
        String password;
        String role;

        TestUser(String u, String p, String r) {
            this.username = u;
            this.password = p;
            this.role = r;
        }
    }

    @Test
    void testLoginWithFactory() {
        System.out.println("=== Factory в автотестах ===");

        // Создаем тестовых пользователей через фабрику
        TestUser admin = TestUserFactory.createUser("ADMIN");
        TestUser user = TestUserFactory.createUser("USER");
        TestUser guest = TestUserFactory.createUser("GUEST");

        System.out.println("Админ: " + admin.username);
        System.out.println("Пользователь: " + user.username);
        System.out.println("Гость: " + guest.username);

        // Используем в тесте
        openBankApp("/login");
        LoginPage loginPage = new LoginPage();

        // Тестируем с админом
        loginPage.enterUsername(admin.username)
                .enterPassword(admin.password)
                .clickLogin();

        System.out.println("✅ Логин с " + admin.role);

        System.out.println("\n✅ Factory создает тестовые данные!");
    }
}