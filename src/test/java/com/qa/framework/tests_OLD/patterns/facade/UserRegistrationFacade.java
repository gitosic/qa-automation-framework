package com.qa.framework.tests_OLD.patterns.facade;

import com.qa.framework.tests_OLD.patterns.facade.complex.*;

// 4. Фасад - скрывает сложность
public class UserRegistrationFacade {
    private DatabaseService db = new DatabaseService();
    private EmailService email = new EmailService();
    private LoggingService log = new LoggingService();

    // ПРОСТОЙ метод вместо сложной последовательности
    public void registerUser(String username, String userEmail) {
        log.logInfo("Начало регистрации пользователя: " + username);

        db.connect();
        db.executeQuery("INSERT INTO users VALUES ('" + username + "')");

        email.validateEmail(userEmail);
        email.sendEmail(userEmail, "Добро пожаловать!");

        log.logInfo("Пользователь " + username + " успешно зарегистрирован");
        db.disconnect();
    }

}