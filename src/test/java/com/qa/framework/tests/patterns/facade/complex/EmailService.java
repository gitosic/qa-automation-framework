package com.qa.framework.tests.patterns.facade.complex;

// 2. Сложный класс 2  
public class EmailService {
    public void validateEmail(String email) {
        System.out.println("📧 Проверка email: " + email);
    }

    public void sendEmail(String to, String subject) {
        System.out.println("✉️ Отправка email на " + to + ": " + subject);
    }
}
