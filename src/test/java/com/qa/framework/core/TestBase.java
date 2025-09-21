package com.qa.framework.core;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.logevents.SelenideLogger;
import com.qa.framework.wiremock.BankAppMock;
import io.qameta.allure.selenide.AllureSelenide;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.chrome.ChromeOptions;

import static com.codeborne.selenide.Selenide.*;

/**
 * Абстрактный базовый класс для всех тестовых классов в фреймворке.
 * Содержит общую конфигурацию и методы setup/teardown для UI и API тестирования.
 * Наследование от этого класса обеспечивает автоматическую инициализацию окружения
 * и корректное освобождение ресурсов после выполнения тестов.
 *
 * @author Vitaliu@yandex.ru
 * @version 1.0
 */
public abstract class TestBase {

    /**
     * Метод инициализации, выполняемый один раз перед всеми тестами в классе.
     * Настраивает конфигурацию Selenide, RestAssured и запускает WireMock сервер.
     * Аннотация @BeforeAll гарантирует выполнение перед всеми тестовыми методами.
     */
    @BeforeAll
    public static void setupAll() {
        // ===== КОНФИГУРАЦИЯ SELENIDE (UI ТЕСТИРОВАНИЕ) =====
        Configuration.browser = "chrome"; // Используем браузер Chrome для тестов
        Configuration.browserSize = "1920x1080"; // Размер окна браузера
        Configuration.timeout = 10000; // Таймаут ожидания элементов (10 секунд)
        Configuration.pageLoadTimeout = 20000; // Таймаут загрузки страницы (20 секунд)
        Configuration.screenshots = true; // Включение автоматических скриншотов при падении тестов
        Configuration.savePageSource = false; // Не сохранять исходный код страницы при падении
        Configuration.baseUrl = "http://localhost:8080"; // Базовый URL для всех тестов
        Configuration.headless = false; // При true браузер не будет виден, но тесты будут работать

        // Отключаем предупреждения о паролях и другие уведомления
        Configuration.browserCapabilities = new ChromeOptions()
                .addArguments("--disable-features=PasswordLeakDetection")
                .addArguments("--disable-password-manager-reauthentication")
                .addArguments("--disable-save-password-bubble")
                .addArguments("--disable-autofill-keyboard-accessory-view")
                .addArguments("--disable-infobars")
                .addArguments("--disable-notifications");

        // Подключаем Allure listener для красивого логгирования действий Selenide в отчетах
        SelenideLogger.addListener("AllureSelenide", new AllureSelenide()
                .screenshots(true) // Делать скриншоты для Allure отчетов
                .savePageSource(false) // Не сохранять исходный код страницы
        );

        // ===== КОНФИГУРАЦИЯ REST ASSURED (API ТЕСТИРОВАНИЕ) =====
        RestAssured.baseURI = "http://localhost:8080"; // Базовый URL для API запросов
        // Добавляем фильтры для логгирования всех API запросов и ответов
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());

        // ЗАПУСКАЕМ WIREMOCK СЕРВЕР ДЛЯ ЭМУЛЯЦИИ ТЕСТОВОГО ПРИЛОЖЕНИЯ
        BankAppMock.start();
    }

    /**
     * Метод выполняемый перед каждым тестом.
     * Открывает базовую страницу для инициализации WebDriver.
     * Selenide автоматически создает драйвер при первом вызове open().
     * Аннотация @BeforeEach гарантирует выполнение перед каждым тестовым методом.
     */
    @BeforeEach
    public void setup() {
        // Открываем базовую страницу для инициализации драйвера
        // Это гарантирует что WebDriver будет готов к использованию в тестах
        open("/");
    }

    /**
     * Метод очистки, выполняемый после каждого теста.
     * Закрывает браузер и очищает ресурсы, выделенные для теста.
     * Аннотация @AfterEach гарантирует выполнение после каждого тестового метода.
     */
    @AfterEach
    public void tearDown() {
        // Закрываем браузер и освобождаем ресурсы
        closeWebDriver();
    }

    /**
     * Метод очистки, выполняемый один раз после всех тестов в классе.
     * Останавливает WireMock сервер и освобождает сетевые ресурсы.
     * Аннотация @AfterAll гарантирует выполнение после всех тестовых методов.
     */
    @AfterAll
    public static void tearDownAll() {
        // ОСТАНАВЛИВАЕМ WIREMOCK СЕРВЕР ПОСЛЕ ЗАВЕРШЕНИЯ ВСЕХ ТЕСТОВ
        BankAppMock.stop();
    }
}