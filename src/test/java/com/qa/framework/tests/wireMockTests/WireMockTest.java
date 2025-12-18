package com.qa.framework.tests.wireMockTests;

import com.qa.framework.core.TestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Тестовый класс для проверки работы WireMock сервера - com.qa.framework.wiremock.BankAppMock
 * Содержит тесты для проверки доступности и корректности мок-сервера
 * Наследует базовый класс TestBase для общей конфигурации тестов
 */
public class WireMockTest extends TestBase {

    /**
     * Тест проверяет базовую доступность WireMock сервера
     * Отправляет GET запрос на эндпоинт /login и проверяет статус 200
     */
    @Test
    @DisplayName("[DEBUG] Проверка доступности WireMock")
    @Tag("WireMockTest")
    void testWireMockAccessibility() {
        given()
                .baseUri("http://localhost:8080")
                .when()
                .get("/login")
                .then()
                .statusCode(200);

        System.out.println("✅ WireMock is accessible!");
    }

    /**
     * Тест проверяет содержимое ответа от WireMock сервера
     * Проверяет наличие ожидаемых элементов в HTML ответе
     */
    @Test
    @DisplayName("[DEBUG] Проверка содержимого ответа WireMock")
    @Tag("WireMockTest")
    void testWireMockResponseContent() {
        String response = given()
                .baseUri("http://localhost:8080")
                .when()
                .get("/login")
                .then()
                .statusCode(200)
                .extract().asString();

        System.out.println("=== RESPONSE CONTENT ===");
        System.out.println(response);
        System.out.println("=== END RESPONSE ===");

        // Проверим что в ответе есть наши элементы
        assertTrue(response.contains("username"), "Response should contain username field");
        assertTrue(response.contains("password"), "Response should contain password field");
        assertTrue(response.contains("Welcome to Bank App"), "Response should contain welcome message");

        System.out.println("✅ WireMock response contains expected elements!");
    }

    /**
     * Тест проверяет API логина через прямые HTTP запросы
     * Проверяет успешный и неуспешный сценарии авторизации
     * Проверяет статус коды и CORS заголовки
     */
    @Test
    @DisplayName("[DEBUG] Проверка API логина через RestAssured")
    @Tag("WireMockTest")
    void testLoginApiDirectly() {
        // Проверяем успешный логин
        given()
                .baseUri("http://localhost:8080")
                .contentType("application/json")
                .body("{\"username\": \"admin\", \"password\": \"password\"}")
                .when()
                .post("/api/login")
                .then()
                .statusCode(200)
                .header("Access-Control-Allow-Origin", "*");

        // Проверяем неуспешный логин
        given()
                .baseUri("http://localhost:8080")
                .contentType("application/json")
                .body("{\"username\": \"wrong\", \"password\": \"wrong\"}")
                .when()
                .post("/api/login")
                .then()
                .statusCode(401)
                .header("Access-Control-Allow-Origin", "*");

        System.out.println("✅ API login tests passed!");
    }

    /**
     * Тест проверяет корректность настройки CORS (Cross-Origin Resource Sharing)
     * Проверяет preflight OPTIONS запрос и соответствующие заголовки
     */
    @Test
    @DisplayName("[DEBUG] Проверка CORS preflight")
    @Tag("WireMockTest")
    void testCorsPreflight() {
        given()
                .baseUri("http://localhost:8080")
                .when()
                .options("/api/login")
                .then()
                .statusCode(200)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");

        System.out.println("✅ CORS preflight test passed!");
    }
}