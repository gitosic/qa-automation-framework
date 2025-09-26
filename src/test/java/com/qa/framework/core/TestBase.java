package com.qa.framework.core;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverRunner;
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
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.codeborne.selenide.Selenide.*;

/**
 * Абстрактный базовый класс для всех тестовых классов в фреймворке.
 * Поддерживает параллельное выполнение тестов через Selenoid.
 */
public abstract class TestBase {

    private static final boolean USE_SELENOID = isSelenoidEnabled();
    private static final String BASE_URL = System.getProperty("baseUrl", "http://localhost:8080");
    private static final String BANK_APP_URL = System.getProperty("bankAppUrl", "http://localhost:3000");
    private static final boolean IS_PARALLEL = Boolean.parseBoolean(System.getProperty("parallel.enabled", "false"));

    // Хранилище для thread-local данных
    private static final ThreadLocal<String> THREAD_INFO = new ThreadLocal<>();
    private static final ConcurrentHashMap<String, String> SESSION_IDS = new ConcurrentHashMap<>();

    private static boolean isSelenoidEnabled() {
        String remoteUrl = System.getProperty("remote.webdriver.url");
        return remoteUrl != null && !remoteUrl.trim().isEmpty();
    }

    @BeforeAll
    public static void setupAll() {
        System.out.println("🎯 Initializing test environment...");
        System.out.println("📍 Base URL: " + BASE_URL);
        System.out.println("🏦 Bank App URL: " + BANK_APP_URL);
        System.out.println("🌐 Selenoid mode: " + (USE_SELENOID ? "ENABLED" : "DISABLED"));
        System.out.println("🧵 Parallel execution: " + (IS_PARALLEL ? "ENABLED" : "DISABLED"));
        System.out.println("🔧 Remote WebDriver URL: " +
                (USE_SELENOID ? System.getProperty("remote.webdriver.url") : "Not set - using local browser"));

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

        // Настройки для параллельного выполнения
        if (IS_PARALLEL) {
            Configuration.holdBrowserOpen = false;
            Configuration.reopenBrowserOnFail = true;
            Configuration.browser = "chrome";
        }

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
            // Настройки для Selenoid
            String remoteUrl = System.getProperty("remote.webdriver.url");
            Configuration.remote = remoteUrl;

            // Уникальное имя сессии для каждого потока
            String sessionName = "Session-" + System.currentTimeMillis() + "-Thread-" + Thread.currentThread().getId();

            // Критически важно: устанавливаем уникальный sessionName в capabilities
            options.setCapability("selenoid:options", Map.<String, Object>of(
                    "enableVNC", true,
                    "screenResolution", "1920x1080x24",
                    "sessionTimeout", "10m",
                    "sessionName", sessionName
            ));

            Configuration.browserCapabilities = options;

            System.out.println("🚀 Selenoid configuration applied for thread: " + Thread.currentThread().getId());
            System.out.println("📡 Session Name: " + sessionName);
        } else {
            // Локальные настройки
            Configuration.browser = "chrome";
            Configuration.headless = false;
            Configuration.browserCapabilities = options;
            System.out.println("🖥️ Local browser configuration applied for thread: " + Thread.currentThread().getId());
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
        String threadInfo = "Thread-" + Thread.currentThread().getId();
        THREAD_INFO.set(threadInfo);

        System.out.println("🔧 Test setup for " + threadInfo);
        System.out.println("📊 Active threads: " + SESSION_IDS.size());
    }

    protected void openBankApp(String path) {
        String url;
        if (USE_SELENOID) {
            url = BANK_APP_URL.replace("localhost", "host.docker.internal") + path;
        } else {
            url = path.startsWith("http") ? path : BANK_APP_URL + path;
        }

        System.out.println("🌐 [" + THREAD_INFO.get() + "] Opening URL: " + url);
        open(url);
    }

    @AfterEach
    public void tearDown() {
        String threadInfo = THREAD_INFO.get();

        // Логируем ID сессии после завершения теста
        if (WebDriverRunner.hasWebDriverStarted()) {
            try {
                RemoteWebDriver driver = (RemoteWebDriver) WebDriverRunner.getWebDriver();
                String sessionId = driver.getSessionId().toString();
                SESSION_IDS.put(threadInfo, sessionId);
                System.out.println("🔍 [" + threadInfo + "] Session ID: " + sessionId);
            } catch (Exception e) {
                System.out.println("⚠️ [" + threadInfo + "] Cannot get session info: " + e.getMessage());
            }
        }

        closeWebDriver();
        System.out.println("🧹 [" + threadInfo + "] WebDriver closed");
        System.out.println("📊 Remaining active threads: " + SESSION_IDS.size());
    }

    @AfterAll
    public static void tearDownAll() {
        if (!USE_SELENOID) {
            BankAppMock.stop();
            System.out.println("🛑 BankAppMock stopped");
        }

        System.out.println("\n===== FINAL SESSION REPORT =====");
        SESSION_IDS.forEach((thread, session) ->
                System.out.println("Thread: " + thread + " | Session: " + session));

        System.out.println("🎉 All tests completed. Total threads executed: " + SESSION_IDS.size());
    }

    // Вспомогательные методы
    public static boolean isParallelExecution() {
        return IS_PARALLEL;
    }

    protected String getThreadInfo() {
        return THREAD_INFO.get();
    }

    public static String getBaseUrl() {
        return USE_SELENOID ? BANK_APP_URL : BASE_URL;
    }

    public static boolean isSelenoidMode() {
        return USE_SELENOID;
    }

    public static String getBankAppUrl() {
        return BANK_APP_URL;
    }
}