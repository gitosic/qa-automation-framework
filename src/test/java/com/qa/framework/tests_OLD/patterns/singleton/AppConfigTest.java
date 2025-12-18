package com.qa.framework.tests_OLD.patterns.singleton;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AppConfigTest {

    @Test
    void testAppConfigSingleton() {
        System.out.println("=== Тест AppConfig Singleton ===");

        // Получаем первый экземпляр
        AppConfig config1 = AppConfig.getInstance();
        System.out.println("Config 1: " + config1.getConfigSummary());

        // Получаем второй экземпляр
        AppConfig config2 = AppConfig.getInstance();
        System.out.println("Config 2: " + config2.getConfigSummary());

        // Проверяем что это один и тот же объект
        assertSame(config1, config2, "Должен быть один экземпляр");

        // Меняем значение в config1
        config1.setBrowser("firefox");
        config1.setHeadless(true);

        // Проверяем что изменения видны в config2
        assertEquals("firefox", config2.getBrowser());
        assertTrue(config2.isHeadless());

        System.out.println("После изменений:");
        System.out.println("Browser in config2: " + config2.getBrowser());
        System.out.println("Headless in config2: " + config2.isHeadless());

        System.out.println("✅ AppConfig Singleton работает!");
    }

    @Test
    void testAppConfigFields() {
        AppConfig config = AppConfig.getInstance();

        // Проверяем значения по умолчанию
        assertEquals("chrome", config.getBrowser());
        assertEquals("http://localhost:8080", config.getBaseUrl());
        assertEquals(10, config.getTimeout());
        assertFalse(config.isHeadless());
        assertEquals("test", config.getEnvironment());

        System.out.println("✅ Значения по умолчанию установлены правильно");
    }
}