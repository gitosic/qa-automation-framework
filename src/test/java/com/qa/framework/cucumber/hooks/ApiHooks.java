package com.qa.framework.cucumber.hooks;

import io.cucumber.java.Before;
import io.cucumber.java.After;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Scenario;
import io.restassured.RestAssured;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiHooks {
    private static final Logger log = LoggerFactory.getLogger(ApiHooks.class);

    @BeforeAll
    public static void beforeAll() {
        log.info("=== INITIALIZING CUCUMBER API TESTS ===");
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        // URL можно установить позже в feature файле
    }

    @Before
    public void setup(Scenario scenario) {
        log.info("Starting scenario: {}", scenario.getName());
    }

    @After
    public void tearDown(Scenario scenario) {
        log.info("Finished scenario: {} - Status: {}",
                scenario.getName(),
                scenario.getStatus());
    }
}