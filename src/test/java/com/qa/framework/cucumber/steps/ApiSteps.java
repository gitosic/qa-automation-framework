package com.qa.framework.cucumber.steps;

import com.qa.framework.cucumber.context.TestContext;
import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import static org.assertj.core.api.Assertions.assertThat;

public class ApiSteps {

    private final TestContext context;

    @Given("I setup the base URL")
    public void setupBaseUrl() {
        RestAssured.baseURI = "http://localhost:8085";
    }

    // Cucumber сам инжектит TestContext
    public ApiSteps(TestContext context) {
        this.context = context;
    }

    @When("I send a GET request to {string}")
    public void sendGetRequest(String endpoint) {

        Response response = RestAssured
                .given()
                .when()
                .get(endpoint);

        context.setLastResponse(response);
    }

    @Then("the response status should be {int}")
    public void verifyResponseStatus(int statusCode) {

        Response response = context.getLastResponse();

        assertThat(response.getStatusCode())
                .isEqualTo(statusCode);
    }

    @Then("I save {string} from the response")
    public void saveFieldFromResponse(String jsonPath) {

        Response response = context.getLastResponse();

        String value = response
                .jsonPath()
                .getString(jsonPath);

        context.setSavedValue(value);

        System.out.println("Saved value: " + value);
    }

    @When("I request user by saved id")
    public void requestUserBySavedId() {

        String savedId = context.getSavedValue();

        Response response = RestAssured
                .given()
                .when()
                .get("http://localhost:8085/example/api/v1/users/" + savedId);

        context.setLastResponse(response);
    }
}
