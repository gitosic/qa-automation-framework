package com.qa.framework.tests_OLD.patterns.singleton;

import lombok.Builder;
import lombok.Getter;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton для управления тестовыми данными.
 * Сочетает Singleton и Builder паттерны.
 */
@Getter
public class TestDataManager {

    // Holder для ленивой инициализации
    private static class Holder {
        private static final TestDataManager INSTANCE = TestDataManager.builder().build();
    }

    // Тестовые данные
    private final Map<String, User> users;
    private final Map<String, String> configurations;

    // Приватный конструктор (используется Builder)
    @Builder
    private TestDataManager() {
        this.users = new HashMap<>();
        this.configurations = new HashMap<>();
        loadDefaultData();
        System.out.println("🗂️ TestDataManager инициализирован");
    }

    // Глобальная точка доступа
    public static TestDataManager getInstance() {
        return Holder.INSTANCE;
    }

    // Методы для работы с данными
    public void addUser(String key, User user) {
        users.put(key, user);
    }

    public User getUser(String key) {
        return users.get(key);
    }

    public void addConfig(String key, String value) {
        configurations.put(key, value);
    }

    public String getConfig(String key) {
        return configurations.get(key);
    }

    // Загрузка данных по умолчанию
    private void loadDefaultData() {
        // Пользователи
        users.put("admin", User.builder()
                .username("admin")
                .password("admin123")
                .role("Administrator")
                .email("admin@test.com")
                .build());

        users.put("user", User.builder()
                .username("testuser")
                .password("password123")
                .role("User")
                .email("user@test.com")
                .build());

        // Конфигурации
        configurations.put("api.url", "http://api.example.com");
        configurations.put("db.url", "jdbc:mysql://localhost:3306/testdb");
        configurations.put("timeout", "30");
    }

    // Вложенный класс для пользовательских данных
    @Builder
    @Getter
    public static class User {
        private final String username;
        private final String password;
        private final String role;
        private final String email;

        @Override
        public String toString() {
            return String.format("User{username='%s', role='%s', email='%s'}",
                    username, role, email);
        }
    }
}