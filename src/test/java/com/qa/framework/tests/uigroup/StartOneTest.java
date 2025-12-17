package com.qa.framework.tests.uigroup;

import com.qa.framework.core.TestBase;
import com.qa.framework.pages.uigroup.LoginTestPageUI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Condition.*;

public class StartOneTest extends TestBase {

    @Test
    @DisplayName("Test UI validation for Bank App login page using Selenide")
    void testAccessibilityBankApp() {
        // Открываем страницу логина
        openBankApp("/login");

        // Инициализируем Page Object
        LoginTestPageUI loginPage = new LoginTestPageUI();
        loginPage.isLoaded();

        System.out.println("1. Проверка видимости элемента loginForm");
        // Проверка через Selenide Conditions
        loginPage.getLoginForm().shouldBe(visible);

        System.out.println("Одна проверка прошла успешно!");
    }

}