package com.qa.framework.tests.patterns.singleton;

import lombok.Getter;

/**
 * Singleton для управления WebDriver.
 * Использует Double-Checked Locking для потокобезопасности.
 */
@Getter
public class WebDriverManager {

    private static volatile WebDriverManager instance;

    private String driverStatus;
    private int sessionCount;
    private String browserType;

    // Приватный конструктор
    private WebDriverManager() {
        this.driverStatus = "NOT_INITIALIZED";
        this.sessionCount = 0;
        this.browserType = "chrome";
        System.out.println("🚗 WebDriverManager создан");
    }

    // Double-Checked Locking
    public static WebDriverManager getInstance() {
        if (instance == null) {
            synchronized (WebDriverManager.class) {
                if (instance == null) {
                    instance = new WebDriverManager();
                }
            }
        }
        return instance;
    }

    // Методы для работы с драйвером
    public void initializeDriver() {
        driverStatus = "INITIALIZED";
        System.out.println("✅ WebDriver инициализирован: " + browserType);
    }

    public void startSession() {
        sessionCount++;
        driverStatus = "SESSION_ACTIVE";
        System.out.println("🎬 Сессия #" + sessionCount + " начата");
    }

    public void endSession() {
        driverStatus = "SESSION_ENDED";
        System.out.println("🏁 Сессия завершена");
    }

    public void setBrowserType(String browserType) {
        this.browserType = browserType;
    }

    // Метод для тестов
    public static void reset() {
        instance = null;
        System.out.println("🔄 WebDriverManager сброшен");
    }
}