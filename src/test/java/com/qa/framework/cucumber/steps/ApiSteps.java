package com.qa.framework.cucumber.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;


public class ApiSteps {

    @Given("I setup the base URL")
    public void setupBaseUrl() {
        RestAssured.baseURI = "http://localhost:8080";
    }

    @When("I send a GET request to {string}")
    public void sendGetRequest(String endpoint) {
        System.out.println("Hi " + endpoint);
    }

    @Then("the response status should be {int}")
    public void verifyResponseStatus(int statusCode) {
        System.out.println("Hi " + statusCode);
    }
}