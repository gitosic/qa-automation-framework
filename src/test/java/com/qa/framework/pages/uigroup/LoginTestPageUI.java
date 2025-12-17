package com.qa.framework.pages.uigroup;

import com.codeborne.selenide.SelenideElement;
import com.qa.framework.pages.BasePage;

import java.time.Duration;

import static com.codeborne.selenide.Condition.visible;

public class LoginTestPageUI extends BasePage {
    /**
     * Получает элемент формы логина
     *
     * @return SelenideElement формы логина
     */
    public SelenideElement getLoginForm() {
        return loginForm;
    }

    // Используем Selenide селекторы для инициализации элементов
    private SelenideElement loginForm = $(".login-form");

    /**
     * Ожидает полной загрузки страницы логина.
     * Проверяет видимость всех ключевых элементов формы логина.
     * Выбрасывает исключение если элементы не загружаются в течение 5 секунд.
     */
    public void waitForPageLoad() {
        loginForm.shouldBe(visible, Duration.ofSeconds(5));
    }

    /**
     * Реализация абстрактного метода для проверки загрузки страницы логина.
     * Делегирует выполнение методу waitForPageLoad() для единообразия кода.
     */
    @Override
    public void isLoaded() {
        waitForPageLoad();
    }
}
