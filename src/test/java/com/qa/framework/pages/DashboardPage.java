package com.qa.framework.pages;

import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.support.FindBy;

import static com.codeborne.selenide.Condition.visible;

/**
 * Page Object класс для страницы Dashboard (главной страницы) после успешного логина.
 * Содержит элементы и методы для взаимодействия с dashboard интерфейсом banking application.
 * Наследует общую функциональность от BasePage.
 *
 * @author Your Name
 * @version 1.0
 */
public class DashboardPage extends BasePage {

    /**
     * Элемент приветственного сообщения на dashboard странице.
     * Аннотация @FindBy используется для lazy инициализации элемента при первом обращении.
     */
    @FindBy(className = "welcome-message")
    private SelenideElement welcomeMessage;

    /**
     * Элемент ссылки для выхода из системы (Logout).
     * Находится по тексту ссылки используя linkText локатор.
     */
    @FindBy(linkText = "Logout")
    private SelenideElement logoutLink;

    /**
     * Реализация абстрактного метода для проверки загрузки dashboard страницы.
     * Проверяет видимость ключевых элементов, подтверждая что страница полностью загружена.
     * Выбрасывает исключение если элементы не становятся видимыми в течение таймаута.
     */
    @Override
    public void isLoaded() {
        welcomeMessage.shouldBe(visible);
        logoutLink.shouldBe(visible);
    }

    /**
     * Получает текст приветственного сообщения с dashboard страницы.
     * Используется для verification в тестах после успешного логина.
     *
     * @return текст приветственного сообщения
     */
    public String getWelcomeMessage() {
        return welcomeMessage.getText();
    }

    /**
     * Выполняет клик по ссылке Logout и возвращает экземпляр LoginPage.
     * Реализует навигацию между страницами используя Fluent Interface паттерн.
     *
     * @return экземпляр LoginPage для последующих действий после logout
     */
    public LoginPage clickLogout() {
        clickElement(logoutLink);
        return openPage(LoginPage.class);
    }
}