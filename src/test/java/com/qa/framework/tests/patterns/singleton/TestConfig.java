package com.qa.framework.tests.patterns.singleton;

import lombok.Getter;

/**
 * Потокобезопасный Singleton для конфигурации тестов.
 * Использует Holder идиому (Bill Pugh Singleton).
 */
@Getter
public class TestConfig {

    // Поля конфигурации
    private final String browser;
    private final String baseUrl;
    private final boolean isHeadless;
    private final String environment;
    private final int defaultTimeout;

    // Приватный конструктор
    private TestConfig() {
        this.browser = System.getProperty("browser", "chrome");
        this.baseUrl = System.getProperty("baseUrl", "http://localhost:8080");
        this.isHeadless = Boolean.parseBoolean(System.getProperty("headless", "false"));
        this.environment = System.getProperty("env", "test");
        this.defaultTimeout = Integer.parseInt(System.getProperty("timeout", "30"));

        System.out.println("✅ TestConfig создан для среды: " + environment);
    }

    // Holder для ленивой инициализации
    private static class Holder {
        private static final TestConfig INSTANCE = new TestConfig();
    }

    // Глобальная точка доступа
    public static TestConfig getInstance() {
        return Holder.INSTANCE;
    }
}