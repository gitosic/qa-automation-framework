package com.qa.framework.tests.patterns.facade.complex;

// 3. Сложный класс 3
public class LoggingService {
    public void logInfo(String message) {
        System.out.println("📝 LOG INFO: " + message);
    }

    public void logError(String error) {
        System.out.println("❌ LOG ERROR: " + error);
    }
}
