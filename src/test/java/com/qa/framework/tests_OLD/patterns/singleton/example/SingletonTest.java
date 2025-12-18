package com.qa.framework.tests_OLD.patterns.singleton.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Простой тест для демонстрации работы Singleton.
 */
public class SingletonTest {

    @Test
    void testSingletonReturnsSameInstance() {
        // Получаем первый экземпляр
        SimpleSingleton singleton1 = SimpleSingleton.getInstance();

        // Получаем второй экземпляр
        SimpleSingleton singleton2 = SimpleSingleton.getInstance();

        // Проверяем, что это один и тот же объект
        assertSame(singleton1, singleton2, "Оба вызова должны возвращать один и тот же объект");

        // Демонстрация использования
        singleton1.printMessage("Привет из теста!");
        singleton2.printMessage("Еще одно сообщение");

        System.out.println("Singleton1 hash: " + System.identityHashCode(singleton1));
        System.out.println("Singleton2 hash: " + System.identityHashCode(singleton2));
    }

    @Test
    void testSingletonInRealScenario() {
        // Представим, что это разные части приложения

        // "Модуль А" получает доступ к Singleton
        SimpleSingleton loggerModuleA = SimpleSingleton.getInstance();
        loggerModuleA.printMessage("Лог из модуля А");

        // "Модуль Б" получает доступ к тому же Singleton
        SimpleSingleton loggerModuleB = SimpleSingleton.getInstance();
        loggerModuleB.printMessage("Лог из модуля Б");

        // Убеждаемся, что это один и тот же логгер
        assertSame(loggerModuleA, loggerModuleB);
    }
}