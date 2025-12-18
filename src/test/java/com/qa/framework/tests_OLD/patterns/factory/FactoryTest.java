package com.qa.framework.tests_OLD.patterns.factory;

import org.junit.jupiter.api.Test;

public class FactoryTest {

    @Test
    void testNotificationFactory() {
        System.out.println("=== Factory Pattern ===");

        // Создаем сообщения через фабрику
        Notification success = NotificationFactory.createNotification("SUCCESS");
        Notification error = NotificationFactory.createNotification("ERROR");
        Notification warning = NotificationFactory.createNotification("WARNING");

        success.send();  // ✅ Успешно!
        error.send();    // ❌ Ошибка!
        warning.send();  // ⚠️ Предупреждение!

        System.out.println("✅ Factory работает!");
    }
}