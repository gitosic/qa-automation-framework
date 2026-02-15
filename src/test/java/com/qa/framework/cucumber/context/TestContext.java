package com.qa.framework.cucumber.context;

import io.restassured.response.Response;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestContext {
    private Response lastResponse;
    private String savedValue;

    public void clear() {
        lastResponse = null;
        savedValue = null;
    }
}