package com.qa.framework.tests.patterns.singleton.example;

/**
 * Демонстрация Singleton Pattern для новичков.
 */
public class SingletonDemo {

    public static void main(String[] args) {
        System.out.println("=".repeat(50));
        System.out.println("🚀 ДЕМОНСТРАЦИЯ SINGLETON PATTERN");
        System.out.println("=".repeat(50));

        // 1. Показываем, что new SimpleSingleton() - невозможно
        // SimpleSingleton s = new SimpleSingleton(); // ОШИБКА! Конструктор приватный

        // 2. Получаем экземпляр через getInstance()
        System.out.println("\n📌 Пример 1: Простой Singleton");
        SimpleSingleton singleton1 = SimpleSingleton.getInstance();
        SimpleSingleton singleton2 = SimpleSingleton.getInstance();

        System.out.println("Первый вызов getInstance(): " + System.identityHashCode(singleton1));
        System.out.println("Второй вызов getInstance(): " + System.identityHashCode(singleton2));
        System.out.println("Это один и тот же объект? " + (singleton1 == singleton2));

        // 3. Используем методы
        singleton1.printMessage("Сообщение 1");
        singleton2.printMessage("Сообщение 2");

        // 4. Практический пример с логгером
        System.out.println("\n📌 Пример 2: Логгер (практическое применение)");

        Logger logger1 = Logger.getInstance();
        Logger logger2 = Logger.getInstance();

        logger1.info("Приложение запущено");
        logger2.debug("Инициализация завершена");

        System.out.println("\n✅ Логгер1 и логгер2 - один объект? " + (logger1 == logger2));

        System.out.println("\n" + "=".repeat(50));
        System.out.println("🎯 КЛЮЧЕВЫЕ ВЫВОДЫ:");
        System.out.println("1. Singleton гарантирует один экземпляр на всё приложение");
        System.out.println("2. Конструктор должен быть private");
        System.out.println("3. Доступ через статический метод getInstance()");
        System.out.println("4. Используется для: логгеров, конфигураций, подключений к БД");
        System.out.println("=".repeat(50));
    }
}