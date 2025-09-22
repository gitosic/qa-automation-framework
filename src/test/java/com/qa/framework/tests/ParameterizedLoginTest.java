package com.qa.framework.tests;

import com.qa.framework.core.TestBase;
import com.qa.framework.pages.LoginPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverRunner.url;
import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;

/**
 * Параметризованные тесты логина с использованием различных источников данных.
 * Демонстрирует продвинутые возможности JUnit 5 параметризации.
 *
 * @author Vitaliu
 * @version 1.0
 */
@DisplayName("🔐 Параметризованные тесты логина")
@Tag("Parameterized")
@Tag("Login")
@Tag("Regression")
public class ParameterizedLoginTest extends TestBase {

    private final LoginPage loginPage = new LoginPage();

    /**
     * Параметризованный тест с использованием CSV источника данных.
     * Демонстрирует различные сценарии логина.
     *
     * @param username имя пользователя для теста
     * @param password пароль для теста
     * @param expectedSuccess ожидаемый результат (true/false)
     * @param description описание тестового сценария
     * @param testInfo информация о тесте для получения метаданных
     */
    @ParameterizedTest(name = "[{index}] {3}") // Кастомное имя для отчета
    @CsvSource({
            "admin, securePass123!, true, 'Успешный логин администратора'",
            "user1, strongPass456!, true, 'Успешный логин пользователя 1'",
            "user2, safePass789!, true, 'Успешный логин пользователя 2'",
            "wrong, wrongPassword, false, 'Неверные credentials'",
            "'', strongPass456!, false, 'Пустой username'",
            "admin, '', false, 'Пустой password'"
    })
    @DisplayName("CSV Source Test")
    @Tag("Smoke")
    @Tag("UI")
    @Tag("DataFromCsvSource")
    void testLoginWithCsvSource(String username, String password,
                                boolean expectedSuccess, String description,
                                TestInfo testInfo) {

        System.out.println("🚀 Запуск теста: " + description);
        System.out.println("📊 Теги теста: " + testInfo.getTags());

        open("http://localhost:3000/login");
        loginPage.waitForPageLoad();

        loginPage.enterUsername(username)
                .enterPassword(password)
                .clickLogin();

        // Даем время для обработки
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (expectedSuccess) {
            assertTrue(url().contains("/dashboard"),
                    "Должен быть редирект на dashboard для: " + description);
            System.out.println("✅ " + description);
        } else {
            assertTrue(loginPage.isErrorMessageDisplayed() ||
                            url().contains("/login"),
                    "Должно отображаться сообщение об ошибке или остаться на login странице для: " + description);
            System.out.println("❌ " + description + " - как и ожидалось");
        }
    }

    /**
     * Параметризованный тест с использованием CSV файла как источника данных.
     * Данные читаются из внешнего файла.
     *
     * @param username имя пользователя для теста
     * @param password пароль для теста
     * @param expectedSuccess ожидаемый результат (true/false)
     * @param description описание тестового сценария
     */
    @ParameterizedTest(name = "[{index}] {3}") // Используем только description
    @CsvFileSource(resources = "/test-data/login-data.csv", numLinesToSkip = 1)
    @DisplayName("CSV File Test")
    @Tag("Regression")
    @Tag("UI")
    void testLoginWithCsvFile(String username, String password,
                              boolean expectedSuccess, String description) {

        open("http://localhost:3000/login");
        loginPage.waitForPageLoad();

        loginPage.enterUsername(username)
                .enterPassword(password)
                .clickLogin();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (expectedSuccess) {
            assertTrue(url().contains("/dashboard"),
                    "Должен быть редирект на dashboard для: " + description);
        } else {
            assertTrue(loginPage.isErrorMessageDisplayed() ||
                            url().contains("/login"),
                    "Должно отображаться сообщение об ошибке для: " + description);
        }
    }

    /**
     * Параметризованный тест с использованием ValueSource для проверки валидации username.
     * Тестирует только поле username с разными значениями.
     *
     * @param username тестовое значение для поля username
     */
    @ParameterizedTest
    @ValueSource(strings = {""}) // Можно через запятую
    @DisplayName("Username validation test: {0}")
    @Tag("Validation")
    void testUsernameValidation(String username) {
        open("http://localhost:3000/login");
        loginPage.waitForPageLoad();

        loginPage.enterUsername(username)
                .clickLogin();

        // Проверяем валидацию поля
        if (username.isEmpty() || username.length() < 3) {
            assertTrue(loginPage.isUsernameFieldInvalid(),
                    "Поле username должно быть невалидным для: " + username);
        } else {
            assertFalse(loginPage.isUsernameFieldInvalid(),
                    "Поле username должно быть валидным для: " + username);
        }
    }

    /**
     * Параметризованный тест с использованием MethodSource.
     * Данные генерируются методом provideEdgeCaseData().
     *
     * @param username имя пользователя для теста
     * @param password пароль для теста
     * @param scenario описание сценария
     */
    @ParameterizedTest
    @MethodSource("provideEdgeCaseData") //
    @DisplayName("Edge case test: {2}")
    @Tag("EdgeCase")
    @Tag("Security")
    void testLoginEdgeCases(String username, String password, String scenario) {
        open("http://localhost:3000/login");
        loginPage.waitForPageLoad();

        loginPage.enterUsername(username)
                .enterPassword(password)
                .clickLogin();

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Для edge cases ожидаем неуспешный логин
        assertTrue(loginPage.isErrorMessageDisplayed() ||
                        url().contains("/login"),
                "Edge case должен завершиться ошибкой: " + scenario);
    }

    /**
     * Метод-поставщик данных для параметризованного теста.
     * Генерирует данные для тестирования edge cases.
     *
     * @return поток аргументов для теста
     */
    private static Stream<Arguments> provideEdgeCaseData() {
        return Stream.of(
                Arguments.of("admin", "securePass123!".toUpperCase(), "Uppercase password"),
                Arguments.of("admin", "securepass123!", "Lowercase password"),
                Arguments.of("admin", "SECUREPASS123!", "Uppercase only"),
                Arguments.of("admin", "securePass123! ", "Password with trailing space"),
                Arguments.of(" admin", "securePass123!", "Username with leading space"),
                Arguments.of("<script>", "securePass123!", "XSS attempt in username"),
                Arguments.of("admin", "<script>alert()</script>", "XSS attempt in password")
        );
    }

    /**
     * Параметризованный тест с использованием Null и Empty Source.
     * Тестирует обработку пустых и null значений.
     *
     * @param username имя пользователя (может быть null или empty)
     */
    @ParameterizedTest
    @org.junit.jupiter.params.provider.NullSource
    @org.junit.jupiter.params.provider.EmptySource
    @DisplayName("Null/Empty username test: {0}")
    @Tag("Boundary")
    void testNullAndEmptyUsername(String username) {
        open("http://localhost:3000/login");
        loginPage.waitForPageLoad();

        if (username != null) {
            loginPage.enterUsername(username);
        }
        loginPage.clickLogin();

        assertTrue(loginPage.isUsernameFieldInvalid() ||
                        loginPage.isErrorMessageDisplayed(),
                "Пустой или null username должен вызывать ошибку");
    }
}