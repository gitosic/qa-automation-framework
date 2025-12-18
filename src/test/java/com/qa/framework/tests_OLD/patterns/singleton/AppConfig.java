package com.qa.framework.tests_OLD.patterns.singleton;

import lombok.Getter;
import lombok.Setter;

/**
 * Singleton для хранения конфигурации приложения.
 */
@Getter
@Setter
public class AppConfig {

    private static AppConfig instance;

    // Конфигурационные поля
    private String browser = "chrome";
    private String baseUrl = "http://localhost:8080";
    private int timeout = 10;
    private boolean headless = false;
    private String environment = "test";

    /**
     * Приватный конструктор.
     * Предотвращает создание экземпляров извне класса.
     */
    private AppConfig() {
        System.out.println("⚙️ AppConfig создан");
        System.out.println("   Браузер по умолчанию: " + browser);
        System.out.println("   URL по умолчанию: " + baseUrl);
    }

    /**
     * Глобальная точка доступа к единственному экземпляру.
     *
     * @return единственный экземпляр AppConfig
     */
    public static AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    /**
     * Вспомогательный метод для получения полной конфигурации.
     */
    public String getConfigSummary() {
        return String.format(
                "Config{browser='%s', baseUrl='%s', timeout=%d, headless=%s, env='%s'}",
                browser, baseUrl, timeout, headless, environment
        );
    }
}