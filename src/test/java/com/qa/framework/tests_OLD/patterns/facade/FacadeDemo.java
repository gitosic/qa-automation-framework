package com.qa.framework.tests_OLD.patterns.facade;

import com.qa.framework.tests_OLD.patterns.facade.complex.DatabaseService;
import com.qa.framework.tests_OLD.patterns.facade.complex.EmailService;
import com.qa.framework.tests_OLD.patterns.facade.complex.LoggingService;

// 5. Клиент - использует только Фасад
public class FacadeDemo {
    public static void main(String[] args) {
        // БЕЗ фасада (сложно):
        System.out.println("=== БЕЗ Facade (сложно) ===");
        DatabaseService db = new DatabaseService();
        EmailService email = new EmailService();
        LoggingService log = new LoggingService();

        log.logInfo("Начало регистрации");
        db.connect();
        db.executeQuery("INSERT...");
        email.validateEmail("test@mail.com");
        email.sendEmail("test@mail.com", "Welcome");
        db.disconnect();
        log.logInfo("Конец регистрации");

        System.out.println("\n=== С Facade (просто) ===");
        // С фасадом (просто):
        UserRegistrationFacade facade = new UserRegistrationFacade();
        facade.registerUser("JohnDoe", "john@example.com");
    }

//    @Test
//    void testUserRegistration() {
//        // Много вызовов, много деталей
//        driver.get("/register");
//        driver.findElement(By.id("username")).sendKeys("test");
//        driver.findElement(By.id("email")).sendKeys("test@mail.com");
//        driver.findElement(By.id("password")).sendKeys("123");
//        driver.findElement(By.id("confirm")).sendKeys("123");
//        driver.findElement(By.id("terms")).click();
//        driver.findElement(By.id("submit")).click();
//
//        // Проверки
//        wait.until(titleContains("Welcome"));
//        assertTrue(driver.findElement(By.id("success")).isDisplayed());
//
//        // Очистка
//        adminApi.deleteUser("test");
//        db.cleanupTestData();
//    }

}