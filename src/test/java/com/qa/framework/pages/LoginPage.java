package com.qa.framework.pages;

import com.codeborne.selenide.SelenideElement;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Condition.*;

/**
 * Page Object класс для страницы логина banking application.
 * Содержит элементы формы логина и методы для взаимодействия с ними.
 * Реализует Fluent Interface паттерн для цепочечных вызовов методов.
 * Наследует общую функциональность от BasePage.
 *
 * @author Your Name
 * @version 1.0
 */
public class LoginPage extends BasePage {

    // Используем Selenide селекторы для инициализации элементов
    private SelenideElement usernameField = $("#username");
    private SelenideElement passwordField = $("#password");
    private SelenideElement loginButton = $("button[type='submit']");
    private SelenideElement errorMessage = $("#errorMessage");
    private SelenideElement successMessage = $("#successMessage");

    /**
     * Ожидает появления сообщения об успешной операции на странице.
     * Используется после выполнения действий которые должны привести к успешному результату.
     * Выбрасывает исключение если сообщение не появляется в течение 5 секунд.
     */
    public void waitForSuccessMessage() {
        successMessage.shouldBe(visible, Duration.ofSeconds(5));
    }

    /**
     * Ожидает появления сообщения об ошибке на странице.
     * Используется после выполнения действий которые должны привести к ошибке.
     * Выбрасывает исключение если сообщение не появляется в течение 5 секунд.
     */
    public void waitForErrorMessage() {
        errorMessage.shouldBe(visible, Duration.ofSeconds(5));
    }

    /**
     * Ожидает полной загрузки страницы логина.
     * Проверяет видимость всех ключевых элементов формы логина.
     * Выбрасывает исключение если элементы не загружаются в течение 5 секунд.
     */
    public void waitForPageLoad() {
        usernameField.shouldBe(visible, Duration.ofSeconds(5));
        passwordField.shouldBe(visible, Duration.ofSeconds(5));
        loginButton.shouldBe(visible, Duration.ofSeconds(5));
    }

    /**
     * Реализация абстрактного метода для проверки загрузки страницы логина.
     * Делегирует выполнение методу waitForPageLoad() для единообразия кода.
     */
    @Override
    public void isLoaded() {
        waitForPageLoad();
    }

    /**
     * Вводит имя пользователя в соответствующее поле формы.
     * Использует Fluent Interface паттерн, возвращая this для цепочечных вызовов.
     *
     * @param username имя пользователя для ввода
     * @return this экземпляр LoginPage для цепочечных вызовов
     */
    public LoginPage enterUsername(String username) {
        setText(usernameField, username);
        return this;
    }

    /**
     * Вводит пароль в соответствующее поле формы.
     * Использует Fluent Interface паттерн, возвращая this для цепочечных вызовов.
     *
     * @param password пароль для ввода
     * @return this экземпляр LoginPage для цепочечных вызовов
     */
    public LoginPage enterPassword(String password) {
        setText(passwordField, password);
        return this;
    }

    /**
     * Выполняет клик по кнопке Login для отправки формы.
     * Не возвращает значение, так как результат действия может быть разным
     * (редирект на dashboard или отображение ошибки).
     */
    public void clickLogin() {
        clickElement(loginButton);
    }

    /**
     * Проверяет отображается ли сообщение об ошибке на странице.
     *
     * @return true если сообщение об ошибке видимо, иначе false
     */
    public boolean isErrorMessageDisplayed() {
        return errorMessage.isDisplayed();
    }

    /**
     * Получает текст сообщения об ошибке.
     *
     * @return текст сообщения об ошибке или пустую строку если сообщение не видимо
     */
    public String getErrorMessageText() {
        return errorMessage.getText();
    }

    /**
     * Проверяет отображается ли сообщение об успехе на странице.
     *
     * @return true если сообщение об успехе видимо, иначе false
     */
    public boolean isSuccessMessageDisplayed() {
        return successMessage.isDisplayed();
    }

    /**
     * Получает текст сообщения об успехе.
     *
     * @return текст сообщения об успехе или пустую строку если сообщение не видимо
     */
    public String getSuccessMessageText() {
        return successMessage.getText();
    }

    /**
     * Проверяет находится ли поле username в невалидном состоянии.
     * Анализирует HTML5 атрибуты валидации и CSS классы ошибок.
     *
     * @return true если поле имеет признаки невалидности, иначе false
     */
    public boolean isUsernameFieldInvalid() {
        return usernameField.has(attribute("invalid")) ||
                usernameField.has(cssClass("error")) ||
                Boolean.parseBoolean(usernameField.getAttribute("required"));
    }

    /**
     * Проверяет находится ли поле password в невалидном состоянии.
     * Анализирует HTML5 атрибуты валидации и CSS классы ошибок.
     *
     * @return true если поле имеет признаки невалидности, иначе false
     */
    public boolean isPasswordFieldInvalid() {
        return passwordField.has(attribute("invalid")) ||
                passwordField.has(cssClass("error")) ||
                Boolean.parseBoolean(passwordField.getAttribute("required"));
    }

    /**
     * Получает текущее значение из поля username.
     *
     * @return текущее значение поля username или null если поле пустое
     */
    public String getUsernameValue() {
        return usernameField.getValue();
    }

    /**
     * Получает текущее значение из поля password.
     *
     * @return текущее значение поля password или null если поле пустое
     */
    public String getPasswordValue() {
        return passwordField.getValue();
    }

    /**
     * Проверяет видимость поля username на странице.
     *
     * @return true если поле username видимо, иначе false
     */
    public boolean isUsernameFieldDisplayed() {
        return usernameField.isDisplayed();
    }

    /**
     * Проверяет видимость поля password на странице.
     *
     * @return true если поле password видимо, иначе false
     */
    public boolean isPasswordFieldDisplayed() {
        return passwordField.isDisplayed();
    }

    /**
     * Проверяет видимость кнопки Login на странице.
     *
     * @return true если кнопка Login видимо, иначе false
     */
    public boolean isLoginButtonDisplayed() {
        return loginButton.isDisplayed();
    }
}