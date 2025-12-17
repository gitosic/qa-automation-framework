package com.qa.framework.tests.patterns.facade.complex;

// 1. Сложный класс 1
public class DatabaseService {
    public void connect() {
        System.out.println("🔗 Подключение к базе данных...");
    }

    public void executeQuery(String query) {
        System.out.println("📊 Выполнение запроса: " + query);
    }

    public void disconnect() {
        System.out.println("🔌 Отключение от базы данных...");
    }
}

