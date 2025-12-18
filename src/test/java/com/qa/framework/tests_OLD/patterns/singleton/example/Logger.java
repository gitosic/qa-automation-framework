package com.qa.framework.tests_OLD.patterns.singleton.example;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Реалистичный пример Singleton - логгер приложения.
 * В реальном приложении логгер должен быть один на всё приложение.
 */
public class Logger {

    private static Logger instance;
    private final DateTimeFormatter formatter;

    // Приватный конструктор
    private Logger() {
        this.formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        System.out.println("📝 Логгер инициализирован");
    }

    // Потокобезопасная версия (для многопоточных приложений)
    public static synchronized Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }

    // Методы логирования
    public void info(String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        System.out.println("[INFO][" + timestamp + "] " + message);
    }

    public void error(String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        System.out.println("[ERROR][" + timestamp + "] " + message);
    }

    public void debug(String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        System.out.println("[DEBUG][" + timestamp + "] " + message);
    }
}