package com.qa.framework.tests_OLD.patterns.builder;

/**
 * Класс User с ручной реализацией Builder Pattern (без Lombok).
 * Для сравнения с Lombok версией.
 */
public class UserWithoutLombok {
    private final String username;
    private final String password;
    private final String role;
    private final boolean isActive;
    private final String email;

    // Приватный конструктор - используется только Builder'ом
    private UserWithoutLombok(String username, String password,
                              String role, boolean isActive, String email) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.isActive = isActive;
        this.email = email;
    }

    // === Геттеры ===
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public boolean isActive() {
        return isActive;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "UserWithoutLombok{" +
                "username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", isActive=" + isActive +
                ", email='" + email + '\'' +
                '}';
    }

    // === Builder класс ===
    public static class Builder {
        private String username;
        private String password;
        private String role = "User";      // Значение по умолчанию
        private boolean isActive = true;   // Значение по умолчанию
        private String email;

        // Обязательные параметры через конструктор Builder'а
        public Builder(String username, String password) {
            this.username = username;
            this.password = password;
        }

        // Необязательные параметры через fluent методы
        public Builder withRole(String role) {
            this.role = role;
            return this;
        }

        public Builder withActiveStatus(boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public Builder withEmail(String email) {
            this.email = email;
            return this;
        }

        // Финальный метод build
        public UserWithoutLombok build() {
            // Валидация
            if (username == null || username.trim().isEmpty()) {
                throw new IllegalArgumentException("Username is required");
            }
            if (password == null || password.trim().isEmpty()) {
                throw new IllegalArgumentException("Password is required");
            }

            return new UserWithoutLombok(username, password, role, isActive, email);
        }
    }

    // Альтернативный подход: статический factory метод
    public static Builder builder(String username, String password) {
        return new Builder(username, password);
    }
}