package com.qa.framework.tests.uiTests;

import com.qa.framework.core.TestBase;

import com.qa.framework.pages.uigroup.LoginPageUI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Condition.*;
import static org.junit.jupiter.api.Assertions.*;

public class StartTest extends TestBase {

    @Test
    @DisplayName("Complete UI validation for Bank App login page using Selenide")
    @Tag("Some1")
    void testAccessibilityBankApp() {
        // Открываем страницу логина
        openBankApp("/login");

        // Инициализируем Page Object
        LoginPageUI loginPage = new LoginPageUI();
        loginPage.isLoaded();

        System.out.println("1. Проверка видимости элементов:");
        // Проверки через Selenide Conditions
        loginPage.getLoginForm().shouldBe(visible);
        loginPage.getUsernameField().shouldBe(visible);
        loginPage.getPasswordField().shouldBe(visible);
        loginPage.getLoginButton().shouldBe(visible);
        loginPage.getUsernameLabel().shouldBe(visible);
        loginPage.getPasswordLabel().shouldBe(visible);

        System.out.println("2. Проверка текста элементов:");
        assertEquals("🏦 Welcome to Bank App", loginPage.getPageHeaderText());
        assertEquals("Username:", loginPage.getUsernameLabelText());
        assertEquals("Password:", loginPage.getPasswordLabelText());
        assertEquals("Login", loginPage.getLoginButton().getText());

        System.out.println("3. Проверка активности элементов:");
        loginPage.getUsernameField().shouldBe(enabled);
        loginPage.getPasswordField().shouldBe(enabled);
        loginPage.getLoginButton().shouldBe(enabled);

        System.out.println("4. Проверка атрибутов:");
        loginPage.getUsernameField().shouldHave(attribute("required"));
        loginPage.getPasswordField().shouldHave(attribute("required"));
        loginPage.getUsernameField().shouldHave(attribute("type", "text"));
        loginPage.getPasswordField().shouldHave(attribute("type", "password"));

        System.out.println("5. Проверка CSS классов:");
        loginPage.getLoginForm().shouldHave(cssClass("login-form"));
        loginPage.getErrorMessage().shouldHave(cssClass("error-message"));
        loginPage.getSuccessMessage().shouldHave(cssClass("success-message"));

        System.out.println("6. Проверка начального состояния сообщений:");
        loginPage.getErrorMessage().shouldNotBe(visible);
        loginPage.getSuccessMessage().shouldNotBe(visible);

        System.out.println("7. Проверка ввода данных:");
        loginPage.enterUsername("testuser")
                .enterPassword("testpass");

        assertEquals("testuser", loginPage.getUsernameValue());
        assertEquals("testpass", loginPage.getPasswordValue());

        System.out.println("8. Проверка возможности очистки:");
        loginPage.getUsernameField().clear();
        loginPage.getPasswordField().clear();
        loginPage.getUsernameField().shouldHave(value(""));
        loginPage.getPasswordField().shouldHave(value(""));

        System.out.println("Все проверки пройдены успешно!");
    }
}