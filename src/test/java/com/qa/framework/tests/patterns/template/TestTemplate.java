package com.qa.framework.tests.patterns.template;

// 1. Абстрактный класс с шаблонным методом
public abstract class TestTemplate {

    // ШАБЛОННЫЙ МЕТОД - фиксированный алгоритм
    public final void runTest() {  // final - нельзя переопределить
        setUp();       // Шаг 1: Настройка
        executeTest(); // Шаг 2: Выполнение теста
        tearDown();    // Шаг 3: Очистка
    }

    // Абстрактные методы - должны реализовать наследники
    protected abstract void setUp();
    protected abstract void executeTest();
    protected abstract void tearDown();

    // Хук-метод (необязательный для переопределения)
    protected void logTestStart() {
        System.out.println("Test started...");
    }
}