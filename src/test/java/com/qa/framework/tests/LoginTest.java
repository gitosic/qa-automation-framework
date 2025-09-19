package com.qa.framework.tests;

import com.qa.framework.core.TestBase;
import com.qa.framework.pages.LoginPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.WebDriverRunner.url;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Тестовый класс для проверки функциональности страницы логина
 * Наследует базовый класс TestBase для общей конфигурации тестов
 *
 * @author QA Team
 * @version 1.0
 */
public class LoginTest extends TestBase {

    private final LoginPage loginPage = new LoginPage();

    /**
     * Тест проверяет корректность ввода данных в форму логина
     * Не выполняет полный процесс логина, только проверяет ввод значений
     */
    @Test
    @DisplayName("[DEBUG] Упрощенный UI тест - проверяем только ввод данных")
    void testSimpleLoginForm() {
        open("/login");

        // Ждем загрузки страницы
        loginPage.waitForPageLoad();

        loginPage.enterUsername("admin")
                .enterPassword("password");

        // Просто проверяем что данные введены
        assertEquals("admin", loginPage.getUsernameValue());
        assertEquals("password", loginPage.getPasswordValue());

        System.out.println("✅ Form input test passed!");
    }

    /**
     * Тест проверяет наличие и отображение всех необходимых элементов
     * на странице логина (поля ввода, кнопка)
     */
    @Test
    @DisplayName("[DEBUG] Проверка отображения элементов на странице")
    void testPageElements() {
        open("/login");

        // Ждем загрузки страницы
        loginPage.waitForPageLoad();

        // Проверяем что все элементы присутствуют
        assertTrue(loginPage.isUsernameFieldDisplayed(), "Username field should be displayed");
        assertTrue(loginPage.isPasswordFieldDisplayed(), "Password field should be displayed");
        assertTrue(loginPage.isLoginButtonDisplayed(), "Login button should be displayed");

        System.out.println("✅ All page elements are present!");
    }

    /**
     * Тест проверяет успешную авторизацию с валидными учетными данными
     * Проверяет редирект на dashboard страницу и наличие контента
     */
    @Test
    @DisplayName("Успешный логин с валидными креденшелами")
    void testSuccessfulLogin() {
        open("/login");

        // Ждем загрузки страницы
        loginPage.waitForPageLoad();

        loginPage.enterUsername("admin")
                .enterPassword("password")
                .clickLogin();

        // Ждем редиректа на dashboard
        // Даем больше времени, так есть setTimeout на 1 секунду
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Проверяем что URL изменился на dashboard
        assertTrue(url().contains("/dashboard"), "Should be redirected to dashboard. Current URL: " + url());

        // Проверяем контент dashboard страницы
        String pageContent = com.codeborne.selenide.Selenide.webdriver().driver().source();
        assertTrue(pageContent.contains("Dashboard"), "Should be on dashboard page");
        assertTrue(pageContent.contains("Welcome"), "Should contain welcome message");

        System.out.println("✅ Successful login test passed! Redirected to dashboard.");
    }

    /**
     * Тест проверяет обработку невалидных учетных данных
     * Проверяет отображение сообщения об ошибке и отсутствие редиректа
     */
    @Test
    @DisplayName("Неуспешный логин с невалидными креденшелами")
    void testFailedLogin() {
        open("/login");

        // Ждем загрузки страницы
        loginPage.waitForPageLoad();

        loginPage.enterUsername("wrong")
                .enterPassword("wrong")
                .clickLogin();

        // Ждем сообщения об ошибке (остаемся на той же странице)
        loginPage.waitForErrorMessage();

        // Проверяем что URL не изменился (остались на login странице)
        assertTrue(url().contains("/login"), "Should stay on login page. Current URL: " + url());

        // Проверяем сообщение об ошибке
        assertTrue(loginPage.isErrorMessageDisplayed(), "Error message should be displayed");
        assertEquals("Invalid credentials", loginPage.getErrorMessageText());

        System.out.println("✅ Failed login test passed! Stayed on login page.");
    }

    /**
     * Тест проверяет валидацию формы при попытке логина с пустыми полями
     * Проверяет HTML5 валидацию полей ввода
     */
    @Test
    @DisplayName("Логин с пустыми полями")
    void testLoginWithEmptyFields() {
        open("/login");

        // Ждем загрузки страницы
        loginPage.waitForPageLoad();

        loginPage.clickLogin(); // Кликаем без ввода данных

        // Должна быть HTML5 валидация
        assertTrue(loginPage.isUsernameFieldInvalid(), "Username field should be invalid");
        assertTrue(loginPage.isPasswordFieldInvalid(), "Password field should be invalid");

        System.out.println("✅ Empty fields validation test passed!");
    }
}