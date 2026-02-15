package com.qa.framework.cucumber.api.steps;

import io.restassured.response.Response;

public class ScenarioContext {
    private static final ThreadLocal<Response> response = new ThreadLocal<>();

    public static void setResponse(Response res) {
        response.set(res);
    }

    public static Response getResponse() {
        return response.get();
    }

    public static void clear() {
        response.remove();
    }
}