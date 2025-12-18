package com.qa.framework.tests_OLD.patterns.factory;

// Фабрика для создания сообщений
public class NotificationFactory {

    public static Notification createNotification(String type) {
        switch (type.toUpperCase()) {
            case "SUCCESS":
                return new SuccessNotification();
            case "ERROR":
                return new ErrorNotification();
            case "WARNING":
                return new WarningNotification();
            default:
                throw new IllegalArgumentException("Неизвестный тип: " + type);
        }
    }
}