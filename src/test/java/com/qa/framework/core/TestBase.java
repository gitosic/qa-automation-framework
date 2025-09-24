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
 */
public abstract class TestBase {

    private static final boolean USE_SELENOID = System.getProperty("remote.webdriver.url") != null;
    private static final String BASE_URL = System.getProperty("baseUrl", "http://localhost:8080");
    private static final String BANK_APP_URL = System.getProperty("bankAppUrl", "http://localhost:3000");

    @BeforeAll
    public static void setupAll() {
        System.out.println("🎯 Initializing test environment...");
        System.out.println("📍 Base URL: " + BASE_URL);
        System.out.println("🏦 Bank App URL: " + BANK_APP_URL);
        System.out.println("🌐 Selenoid mode: " + (USE_SELENOID ? "ENABLED" : "DISABLED"));

        configureSelenide();
        configureRestAssured();

        if (!USE_SELENOID) {
            BankAppMock.start();
        }
    }

    private static void configureSelenide() {
        // Базовые настройки
        Configuration.browserSize = "1920x1080";
        Configuration.timeout = 10000;
        Configuration.pageLoadTimeout = 30000;
        Configuration.screenshots = true;
        Configuration.savePageSource = false;
        Configuration.baseUrl = BASE_URL;

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-infobars");
        options.addArguments("--disable-notifications");
        options.addArguments("--window-size=1920,1080");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});

        if (USE_SELENOID) {
            // Настройки для Selenoid - минимальные
            Configuration.remote = System.getProperty("remote.webdriver.url");
            Configuration.browser = "chrome";
            Configuration.browserCapabilities = options;

            System.out.println("🚀 Selenoid configuration applied (minimal setup)");
            System.out.println("📡 Remote URL: " + Configuration.remote);
        } else {
            // Локальные настройки
            Configuration.browser = "chrome";
            Configuration.headless = false;
            Configuration.browserCapabilities = options;
            System.out.println("🖥️ Local browser configuration applied");
        }

        // Allure интеграция
        SelenideLogger.addListener("AllureSelenide", new AllureSelenide()
                .screenshots(true)
                .savePageSource(false));
    }

    private static void configureRestAssured() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    }

    @BeforeEach
    public void setup() {
        if (!USE_SELENOID) {
            open("/");
        }
    }

    protected void openBankApp(String path) {
        String url;
        if (USE_SELENOID) {
            // Для Selenoid используем host.docker.internal для доступа к локальной машине
            url = BANK_APP_URL.replace("localhost", "host.docker.internal") + path;
            System.out.println("🔧 Selenoid mode - Constructed URL: " + url);

            // Проверка доступности URL
            try {
                java.net.URL testUrl = new java.net.URL(url);
                System.out.println("🔍 URL protocol: " + testUrl.getProtocol());
                System.out.println("🔍 URL host: " + testUrl.getHost());
                System.out.println("🔍 URL port: " + testUrl.getPort());
                System.out.println("🔍 URL path: " + testUrl.getPath());
            } catch (Exception e) {
                System.out.println("❌ URL construction error: " + e.getMessage());
            }
        } else {
            // Для локального режима используем относительный путь
            url = path.startsWith("http") ? path : BANK_APP_URL + path;
            System.out.println("🔧 Local mode - URL: " + url);
        }
        System.out.println("🌐 Opening URL: " + url);
        open(url);
    }

    @AfterEach
    public void tearDown() {
        closeWebDriver();
    }

    @AfterAll
    public static void tearDownAll() {
        if (!USE_SELENOID) {
            BankAppMock.stop();
        }
    }

    public static String getBaseUrl() {
        return USE_SELENOID ? BANK_APP_URL : BASE_URL;
    }

    public static boolean isSelenoidMode() {
        return USE_SELENOID;
    }
}