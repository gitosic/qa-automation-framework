package com.qa.framework.tests.patterns.singleton;

import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Singleton для логирования тестов через Enum.
 * Enum в Java гарантирует единственный экземпляр.
 */
@Slf4j
public enum TestLogger {

    INSTANCE;

    // Конструктор Enum
    TestLogger() {
        System.out.println("📝 TestLogger инициализирован");
    }

    // Методы логирования
    public void info(String message) {
        logWithTimestamp("INFO", message);
    }

    public void debug(String message) {
        logWithTimestamp("DEBUG", message);
    }

    public void error(String message) {
        logWithTimestamp("ERROR", message);
    }

    public void warn(String message) {
        logWithTimestamp("WARN", message);
    }

    // Приватный метод для логирования с timestamp
    private void logWithTimestamp(String level, String message) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        String formattedMessage = String.format("[%s] %s", timestamp, message);

        switch (level) {
            case "INFO":
                log.info(formattedMessage);
                break;
            case "DEBUG":
                log.debug(formattedMessage);
                break;
            case "ERROR":
                log.error(formattedMessage);
                break;
            case "WARN":
                log.warn(formattedMessage);
                break;
        }
    }
}