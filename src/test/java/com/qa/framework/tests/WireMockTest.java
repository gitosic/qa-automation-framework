package com.qa.framework.tests;

import com.qa.framework.core.TestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Тест для проверки, что WireMock работает корректно
 */
public class WireMockTest extends TestBase {

    @Test
    @DisplayName("[DEBUG] Проверка доступности WireMock")
    void testWireMockAccessibility() {
        given()
                .baseUri("http://localhost:8080")
                .when()
                .get("/login")
                .then()
                .statusCode(200);

        System.out.println("✅ WireMock is accessible!");
    }

    @Test
    @DisplayName("[DEBUG] Проверка содержимого ответа WireMock")
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

    @Test
    @DisplayName("[DEBUG] Проверка API логина через RestAssured")
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

    @Test
    @DisplayName("[DEBUG] Проверка CORS preflight")
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