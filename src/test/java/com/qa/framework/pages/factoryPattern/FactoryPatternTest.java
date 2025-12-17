package com.qa.framework.pages.factoryPattern;

import com.qa.framework.core.TestBase;
import com.qa.framework.pages.LoginPage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FactoryPatternTest extends TestBase {

    /**
     * 1. Factory Method Pattern Test
     */
    @Test
    void testPageFactory() {
        openBankApp("/login");

        // Используем фабрику вместо прямого создания
        LoginPage loginPage = PageFactory.createLoginPage();
        loginPage.isLoaded();

        assertTrue(loginPage.isUsernameFieldDisplayed());
        assertTrue(loginPage.isPasswordFieldDisplayed());
        assertTrue(loginPage.isLoginButtonDisplayed());

        System.out.println("✅ Factory Pattern тест пройден");
    }
}