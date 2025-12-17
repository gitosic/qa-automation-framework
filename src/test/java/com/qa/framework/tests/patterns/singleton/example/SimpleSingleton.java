package com.qa.framework.tests.patterns.singleton.example;

/**
 * Singleton - гарантирует, что у класса есть только один экземпляр
 * и предоставляет глобальную точку доступа к нему.
 *
 * Простая аналогия: В офисе есть только один принтер.
 * Все сотрудники используют один и тот же принтер.
 */
public class SimpleSingleton {

    // 1. Единственный экземпляр (статическая переменная)
    private static SimpleSingleton instance;

    // 2. Приватный конструктор - нельзя создать через new
    private SimpleSingleton() {
        System.out.println("✅ Создан единственный экземпляр SimpleSingleton!");
    }

    // 3. Публичный статический метод для получения экземпляра
    public static SimpleSingleton getInstance() {
        if (instance == null) {
            instance = new SimpleSingleton();
        }
        return instance;
    }

    // 4. Метод для демонстрации работы
    public void printMessage(String message) {
        System.out.println("Singleton печатает: " + message);
    }
}