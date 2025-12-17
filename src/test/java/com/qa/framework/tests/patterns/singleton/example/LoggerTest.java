package com.qa.framework.tests.patterns.singleton.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LoggerTest {

    @Test
    void testLoggerSingleton() {
        // Симуляция разных частей приложения

        // Веб-компонент логирует
        Logger webLogger = Logger.getInstance();
        webLogger.info("Пользователь залогинился");

        // Сервисный слой логирует
        Logger serviceLogger = Logger.getInstance();
        serviceLogger.debug("Выполняется бизнес-логика");

        // DAO слой логирует ошибку
        Logger daoLogger = Logger.getInstance();
        daoLogger.error("Ошибка подключения к БД");

        // Проверяем, что везде один и тот же логгер
        assertSame(webLogger, serviceLogger);
        assertSame(serviceLogger, daoLogger);

        System.out.println("\n✅ Все компоненты используют один экземпляр логгера!");
    }
}