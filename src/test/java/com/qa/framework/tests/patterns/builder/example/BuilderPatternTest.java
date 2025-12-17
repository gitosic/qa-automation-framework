package com.qa.framework.tests.patterns.builder.example;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BuilderPatternTest {

    @DisplayName("Создание задачи через Builder")
    @Test
    void checkInfo() {
        // Создаем задачу - дата установится автоматически через @Builder.Default
        Task task = Task.builder()
                .name("Проверить баг")
                .complexity(5)
                // .date(LocalDate.now())  // НЕ НУЖНО - установится автоматически!
                .build();

        System.out.println("Созданная задача: " + task);

        // Проверяем через JUnit 5 assertAll (soft assertions)
        assertAll(
                () -> assertEquals("Проверить баг", task.getName()),
                () -> assertEquals(5, task.getComplexity()),
                () -> assertEquals(LocalDate.now(), task.getDate())
        );

        // Используем Soft Assertions вместо обычных assertEquals
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(task.getName()).isEqualTo("Проверить баг");
        softly.assertThat(task.getComplexity()).isEqualTo(5);
        softly.assertThat(task.getDate()).isEqualTo(LocalDate.now());  // Проверка текущей даты

        // Все проверки выполнятся, даже если некоторые упадут
        softly.assertAll();
    }
}