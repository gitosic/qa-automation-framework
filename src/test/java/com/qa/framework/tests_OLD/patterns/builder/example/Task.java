package com.qa.framework.tests_OLD.patterns.builder.example;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;

@AllArgsConstructor
@ToString
@Getter
@Builder
public class Task {
    private String name;
    @Builder.Default  // Устанавливает через lombok значение даты по умолчанию
    private LocalDate date = LocalDate.now();
    private int complexity;

    // Метод для создания builder с текущей датой по умолчанию. Этот вариант по старинке
    // УДАЛИТЬ ЭТОТ МЕТОД! Lombok уже его создает
    // public static TaskBuilder builder() {
    //     return new TaskBuilder().date(LocalDate.now());
    // }

}