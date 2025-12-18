package com.qa.framework.tests.apiTests;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

public class LoginApiUniversalTest {

    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = "http://localhost:3000";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @ParameterizedTest(name = "Login test for {0} (expected: {2})")
    @CsvFileSource(
            resources = "/test-data/all_credentials.csv",
            numLinesToSkip = 1
    )
    void testLoginUniversal(
            String username,
            String password,
            String expectedStatus,
            int expectedId,
            String expectedName,
            String expectedMessage) {

        Map<String, String> requestBody = Map.of(
                "username", username,
                "password", password
        );

        if ("success".equals(expectedStatus)) {
            // Тест успешного логина
            JsonPath jsonPath = given()
                    .contentType(ContentType.JSON)
                    .body(requestBody)
                    .when()
                    .post("/api/login")
                    .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .extract()
                    .jsonPath();

            assertAll(
                    () -> assertEquals(expectedStatus, jsonPath.getString("status")),
                    () -> assertEquals(expectedMessage, jsonPath.getString("message")),
                    () -> assertEquals(expectedId, jsonPath.getInt("user.id")),
                    () -> assertEquals(expectedName, jsonPath.getString("user.name")),
                    () -> assertTrue(jsonPath.getInt("user.balance") >= 0)
            );

            System.out.printf("✅ %s logged in successfully. Balance: %d%n",
                    username, jsonPath.getInt("user.balance"));

        } else {
            // Тест неуспешного логина
            JsonPath jsonPath = given()
                    .contentType(ContentType.JSON)
                    .body(requestBody)
                    .when()
                    .post("/api/login")
                    .then()
                    .statusCode(401)
                    .contentType(ContentType.JSON)
                    .extract()
                    .jsonPath();

            assertAll(
                    () -> assertEquals(expectedStatus, jsonPath.getString("status")),
                    () -> assertEquals(expectedMessage, jsonPath.getString("message"))
            );

            System.out.printf("❌ %s login failed as expected: %s%n",
                    username, jsonPath.getString("message"));
        }
    }
}