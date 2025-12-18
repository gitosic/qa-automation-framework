package com.qa.framework.tests_OLD.patterns.builder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Дополнительные примеры использования Builder Pattern
 */
@DisplayName("Builder Pattern - Advanced Examples")
public class MultipleUsersTest {

    @Test
    @DisplayName("Создание списка тестовых пользователей")
    void testMultipleUsers() {
        System.out.println("\n=== Creating Multiple Test Users ===");

        // Создаем разных пользователей для тестов
        List<User> testUsers = Arrays.asList(
                // Администратор
                User.builder()
                        .username("super_admin")
                        .password("SuperSecure!123")
                        .role("SuperAdmin")
                        .isActive(true)
                        .build(),

                // Обычный пользователь
                User.builder()
                        .username("regular_user")
                        .password("RegularPass456")
                        .role("User")
                        .isActive(true)
                        .build(),

                // Неактивный пользователь
                User.builder()
                        .username("inactive_user")
                        .password("InactivePass789")
                        .role("User")
                        .isActive(false)
                        .build(),

                // Пользователь без email
                User.builder()
                        .username("no_email_user")
                        .password("NoEmailPass000")
                        .build()
        );

        // Выводим всех пользователей
        testUsers.forEach(user ->
                System.out.println("Created: " + user.getUsername() +
                        " (Role: " + user.getRole() +
                        ", Active: " + user.isActive() + ")")
        );

        System.out.println("\n✅ Created " + testUsers.size() + " test users with Builder!");
    }
}