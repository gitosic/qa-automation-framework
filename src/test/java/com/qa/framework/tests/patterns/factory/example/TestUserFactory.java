package com.qa.framework.tests.patterns.factory.example;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.junit.jupiter.api.Nested;

/**
 * Factory Pattern пример для создания тестовых пользователей.
 * Демонстрирует разницу между подходами с использованием Lombok.
 */
public class TestUserFactory {

    // ==================== ШАГ 1: Класс User с Lombok ====================

    /**
     * Класс для представления тестового пользователя.
     * Lombok аннотации генерируют весь boilerplate код.
     */
    @AllArgsConstructor
    @Getter
    @ToString
    public static class User {
        private final String username;
        private final String password;
        private final String role;
        // Lombok автоматически создаст:
        // 1. Конструктор со всеми параметрами
        // 2. Геттеры для всех полей
        // 3. toString() метод
    }

    // ==================== ШАГ 2: Фабрика для создания пользователей ====================

    /**
     * Фабрика для создания тестовых пользователей.
     * Централизует логику создания объектов.
     */
    public static class UserFactory {

        /**
         * Основной фабричный метод.
         * Создает пользователя по указанному типу.
         */
        public static User create(String type) {
            switch (type.toLowerCase()) {
                case "admin":
                    return new User("admin", "AdminPass123!", "Администратор");
                case "user":
                    return new User("user", "UserPass456!", "Пользователь");
                case "guest":
                    return new User("guest", "GuestPass789!", "Гость");
                case "moderator":
                    return new User("moderator", "ModPass000!", "Модератор");
                default:
                    throw new IllegalArgumentException("Неизвестный тип пользователя: " + type);
            }
        }

        /**
         * Альтернативный метод с логированием.
         */
        public static User createWithLog(String type) {
            User user = create(type);
            System.out.println("✅ Создан: " + user);
            return user;
        }
    }

    // ==================== ШАГ 3: Примеры использования ====================

    /**
     * Пример БЕЗ Factory Pattern (старый подход).
     * Проблема: дублирование кода в разных тестах.
     */
    public static class WithoutFactoryExample {

        public void testLogin_OldWay(String userType) {
            System.out.println("\n=== БЕЗ Factory (СТАРЫЙ СПОСОБ) ===");

            // Этот код будет повторяться в каждом тесте!
            String username, password;
            if (userType.equalsIgnoreCase("admin")) {
                username = "admin";
                password = "AdminPass123!";
            } else if (userType.equalsIgnoreCase("user")) {
                username = "user";
                password = "UserPass456!";
            } else if (userType.equalsIgnoreCase("guest")) {
                username = "guest";
                password = "GuestPass789!";
            } else {
                throw new IllegalArgumentException("Неизвестный тип: " + userType);
            }

            // Симуляция теста логина
            System.out.println("Тестируем логин...");
            System.out.println("Логин: " + username);
            System.out.println("Пароль: " + password);
            System.out.println("⚠️ Проблема: if/else дублируется во многих тестах!");
        }
    }

    /**
     * Пример С Factory Pattern (новый подход).
     * Решение: логика создания в одном месте.
     */
    public static class WithFactoryExample {

        public void testLogin_NewWay(String userType) {
            System.out.println("\n=== С Factory (НОВЫЙ СПОСОБ) ===");

            // Всего одна строка! Логика создания в фабрике
            User testUser = UserFactory.create(userType);

            // Симуляция теста логина
            System.out.println("Тестируем логин...");
            System.out.println("Логин: " + testUser.getUsername());
            System.out.println("Пароль: " + testUser.getPassword());
            System.out.println("Роль: " + testUser.getRole());
            System.out.println("✅ Преимущество: создание централизовано в одном месте!");
        }
    }

    // ==================== ШАГ 4: Реальные примеры тестов ====================

    /**
     * Пример реального теста с использованием Factory.
     */
    public static class RealTestExamples {

        public void runAdminLoginTest() {
            System.out.println("\n🧪 Тест 1: Логин администратора");
            User admin = UserFactory.create("admin");
            // Реальный вызов: loginPage.login(admin.getUsername(), admin.getPassword());
            System.out.println("Используем: " + admin);
            System.out.println("Ожидаем: успешный логин с правами администратора");
        }

        public void runUserLoginTest() {
            System.out.println("\n🧪 Тест 2: Логин обычного пользователя");
            User user = UserFactory.create("user");
            // loginPage.login(user.getUsername(), user.getPassword());
            System.out.println("Используем: " + user);
            System.out.println("Ожидаем: успешный логин с обычными правами");
        }

        public void runInvalidLoginTest() {
            System.out.println("\n🧪 Тест 3: Неправильный логин");
            try {
                UserFactory.create("unknown"); // Несуществующий тип
            } catch (IllegalArgumentException e) {
                System.out.println("✅ Корректно получена ошибка: " + e.getMessage());
            }
        }
    }

    // ==================== ШАГ 5: Демонстрационный тест ====================

    /**
     * Основной демонстрационный тест, показывающий преимущества Factory Pattern.
     */
    public static class FactoryPatternDemo {

        public void runFullDemo() {
            System.out.println("=".repeat(60));
            System.out.println("🚀 ПОЛНАЯ ДЕМОНСТРАЦИЯ FACTORY PATTERN");
            System.out.println("=".repeat(60));

            // Создаем экземпляры для демонстрации
            WithoutFactoryExample oldWay = new WithoutFactoryExample();
            WithFactoryExample newWay = new WithFactoryExample();
            RealTestExamples realTests = new RealTestExamples();

            // 1. Показываем проблему старого подхода
            System.out.println("\n📋 ЧАСТЬ 1: ПРОБЛЕМА БЕЗ FACTORY PATTERN");
            oldWay.testLogin_OldWay("admin");
            oldWay.testLogin_OldWay("user");

            // 2. Показываем решение с Factory Pattern
            System.out.println("\n📋 ЧАСТЬ 2: РЕШЕНИЕ С FACTORY PATTERN");
            newWay.testLogin_NewWay("admin");
            newWay.testLogin_NewWay("user");
            newWay.testLogin_NewWay("guest");

            // 3. Показываем использование в реальных тестах
            System.out.println("\n📋 ЧАСТЬ 3: ПРАКТИЧЕСКОЕ ИСПОЛЬЗОВАНИЕ");
            realTests.runAdminLoginTest();
            realTests.runUserLoginTest();
            realTests.runInvalidLoginTest();

            // 4. Демонстрируем преимущества Lombok
            System.out.println("\n📋 ЧАСТЬ 4: ПРЕИМУЩЕСТВА LOMBOK");
            User sampleUser = UserFactory.create("moderator");
            System.out.println("Lombok сгенерировал:");
            System.out.println("  • Конструктор: new User(...)");
            System.out.println("  • Геттеры: " + sampleUser.getUsername() + ", " + sampleUser.getRole());
            System.out.println("  • toString(): " + sampleUser);

            // 5. Итоговые выводы
            System.out.println("\n📋 ЧАСТЬ 5: КЛЮЧЕВЫЕ ВЫВОДЫ");
            System.out.println("✅ Factory Pattern централизует логику создания объектов");
            System.out.println("✅ Упрощает поддержку кода (меняем в одном месте)");
            System.out.println("✅ Уменьшает дублирование кода в тестах");
            System.out.println("✅ Lombok сокращает boilerplate код на 80%");
            System.out.println("✅ Код становится чище и читаемее");

            System.out.println("\n" + "=".repeat(60));
            System.out.println("🎉 ДЕМОНСТРАЦИЯ ЗАВЕРШЕНА");
            System.out.println("=".repeat(60));
        }
    }

    // ==================== ШАГ 6: Точка входа ====================

    /**
     * Главный метод для запуска демонстрации.
     * Можно запускать как обычную Java программу.
     */
    public static void main(String[] args) {
        FactoryPatternDemo demo = new FactoryPatternDemo();
        demo.runFullDemo();
    }

    /**
     * JUnit тест для запуска демонстрации.
     * Можно запускать как тест в среде разработки.
     */
    @Nested
    class FactoryPatternJUnitTest {
        @org.junit.jupiter.api.Test
        void runFactoryPatternDemo() {
            FactoryPatternDemo demo = new FactoryPatternDemo();
            demo.runFullDemo();
        }
    }
}