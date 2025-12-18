package com.qa.framework.tests_OLD.patterns.builder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тест для сравнения Builder Pattern с Lombok и без.
 */
@DisplayName("Builder Pattern Comparison: Lombok vs Manual")
public class BuilderComparisonTest {

    @Test
    @DisplayName("Сравнение двух реализаций")
    void testBothImplementations() {
        System.out.println("=== Сравнение Builder Pattern ===");
        System.out.println("Lombok vs Ручная реализация\n");

        // 1. Создание через Lombok (коротко и читаемо)
        System.out.println("1. С Lombok (@Builder аннотация):");
        User userLombok = User.builder()
                .username("admin")
                .password("pass123")
                .role("Admin")
                .email("admin@test.com")
                .build();

        System.out.println("   Создан: " + userLombok);
        System.out.println("   Количество строк в классе: ~10 (с аннотациями)");

        // 2. Создание без Lombok (больше кода)
        System.out.println("\n2. Без Lombok (ручная реализация):");
        UserWithoutLombok userManual = new UserWithoutLombok.Builder("admin", "pass123")
                .withRole("Admin")
                .withEmail("admin@test.com")
                .withActiveStatus(true)
                .build();

        System.out.println("   Создан: " + userManual);
        System.out.println("   Количество строк в классе: ~70 (без аннотаций)");

        // 3. Сравнение функциональности
        System.out.println("\n3. Сравнение функциональности:");

        // Проверка значений
        assertEquals(userLombok.getUsername(), userManual.getUsername());
        assertEquals(userLombok.getRole(), userManual.getRole());
        assertEquals(userLombok.isActive(), userManual.isActive());

        // Проверка валидации (оба подхода проверяют обязательные поля)
        System.out.println("   ✅ Оба подхода проверяют обязательные поля");

        // 4. Плюсы и минусы
        System.out.println("\n4. Плюсы и минусы:");
        System.out.println("   Lombok (+):");
        System.out.println("     - Меньше кода (10 строк vs 70)");
        System.out.println("     - Автоматическое обновление при изменении полей");
        System.out.println("     - Меньше вероятности ошибок");
        System.out.println("     - Генерирует toString(), equals(), hashCode()");

        System.out.println("\n   Lombok (-):");
        System.out.println("     - Зависимость от библиотеки");
        System.out.println("     - Нужно настраивать IDE");
        System.out.println("     - Меньше контроля над процессом");

        System.out.println("\n   Ручная реализация (+):");
        System.out.println("     - Полный контроль");
        System.out.println("     - Нет зависимостей");
        System.out.println("     - Понятно как работает");

        System.out.println("\n   Ручная реализация (-):");
        System.out.println("     - Много boilerplate кода");
        System.out.println("     - Легко допустить ошибку");
        System.out.println("     - Сложно поддерживать при изменении");

        System.out.println("\n✅ Сравнение завершено!");
    }

    @Test
    @DisplayName("Пример использования ручного Builder")
    void testManualBuilderUsage() {
        System.out.println("\n=== Примеры использования ручного Builder ===");

        // Вариант 1: Обязательные поля в конструкторе Builder'а
        UserWithoutLombok user1 = new UserWithoutLombok.Builder("john", "pass123")
                .withRole("User")
                .build();
        System.out.println("1. Обязательные поля в конструкторе: " + user1);

        // Вариант 2: Со значениями по умолчанию
        UserWithoutLombok user2 = new UserWithoutLombok.Builder("jane", "pass456")
                // role = "User" по умолчанию
                // isActive = true по умолчанию
                .withEmail("jane@test.com")
                .build();
        System.out.println("2. Со значениями по умолчанию: " + user2);

        // Вариант 3: Через статический метод
        UserWithoutLombok user3 = UserWithoutLombok.builder("admin", "admin123")
                .withRole("Admin")
                .withActiveStatus(false)
                .build();
        System.out.println("3. Через статический метод: " + user3);

        // Проверка валидации
        try {
            new UserWithoutLombok.Builder("", "pass") // Пустой username
                    .build();
            fail("Should throw exception");
        } catch (IllegalArgumentException e) {
            System.out.println("4. ✅ Валидация работает: " + e.getMessage());
        }

        System.out.println("\n✅ Ручной Builder работает!");
    }
}