package com.qa.framework.tests_OLD.patterns.factory;

// Базовый класс сообщения
public abstract class Notification {
    public abstract String getType();
    public abstract void send();
}

// Конкретные сообщения
class SuccessNotification extends Notification {
    @Override public String getType() { return "SUCCESS"; }
    @Override public void send() { System.out.println("✅ Успешно!"); }
}

class ErrorNotification extends Notification {
    @Override public String getType() { return "ERROR"; }
    @Override public void send() { System.out.println("❌ Ошибка!"); }
}

class WarningNotification extends Notification {
    @Override public String getType() { return "WARNING"; }
    @Override public void send() { System.out.println("⚠️ Предупреждение!"); }
}