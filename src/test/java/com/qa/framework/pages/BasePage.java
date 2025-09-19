package com.qa.framework.pages;

import com.codeborne.selenide.SelenideElement;
import com.qa.framework.core.TestBase;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.page;

/**
 * Абстрактный базовый класс для всех Page Object в фреймворке.
 * Предоставляет общие методы и функциональность для работы с веб-страницами.
 * Наследует от TestBase, обеспечивая доступ к конфигурации и методам тестового окружения.
 * Реализует паттерн Page Object Model (POM) для улучшения поддерживаемости тестов.
 *
 * @author Your Name
 * @version 1.0
 */
public abstract class BasePage extends TestBase {

    /**
     * Общий элемент заголовка страницы, присутствующий на большинстве страниц.
     * Используется для проверки загрузки страницы через метод isLoaded().
     */
    protected SelenideElement pageHeader = $("h1");

    /**
     * Абстрактный метод для проверки что страница полностью загрузилась.
     * Должен быть реализован в каждом конкретном Page Object.
     * Обычно проверяет видимость ключевых элементов страницы.
     */
    public abstract void isLoaded();

    /**
     * Универсальный метод для безопасного клика по элементу с предварительной проверкой видимости.
     * Гарантирует что элемент видим перед выполнением клика, предотвращая NoSuchElementException.
     *
     * @param element SelenideElement по которому требуется выполнить клик
     * @throws com.codeborne.selenide.ex.ElementNotFound если элемент не видим в течение таймаута
     */
    protected void clickElement(SelenideElement element) {
        element.shouldBe(visible).click();
    }

    /**
     * Универсальный метод для ввода текста в поле с предварительной очисткой и проверкой видимости.
     * Очищает поле перед вводом нового значения, обеспечивая корректность тестовых данных.
     *
     * @param element SelenideElement поле ввода для текста
     * @param text    текст для ввода в поле
     * @throws com.codeborne.selenide.ex.ElementNotFound если элемент не видим в течение таймаута
     */
    protected void setText(SelenideElement element, String text) {
        element.shouldBe(visible).clear();
        element.setValue(text);
    }

    /**
     * Вспомогательный метод для инициализации и возврата экземпляра другой Page Object.
     * Реализует паттерн Fluent Interface для цепочки вызовов при навигации между страницами.
     * Использует Selenide Page Factory для автоматической инициализации элементов.
     *
     * @param <T>       тип возвращаемой Page Object, должен наследоваться от BasePage
     * @param pageClass класс Page Object для инициализации
     * @return экземпляр запрошенной Page Object
     */
    protected <T extends BasePage> T openPage(Class<T> pageClass) {
        return page(pageClass);
    }
}