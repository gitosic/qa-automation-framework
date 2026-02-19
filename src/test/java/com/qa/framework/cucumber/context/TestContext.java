package com.qa.framework.cucumber.context;

import io.restassured.response.Response;

public class TestContext {
    private Response lastResponse;
    private String savedValue;

    public void clear() {
        lastResponse = null;
        savedValue = null;
    }

    //Используем геторы и сеторы, чтобы можно было запускать тесты через консоль командой
    //команда для запуска mvn clean test -Pcucumber-tests -DincludeTags=should1
    public Response getLastResponse() {
        return lastResponse;
    }

    public void setLastResponse(Response lastResponse) {
        this.lastResponse = lastResponse;
    }

    public String getSavedValue() {
        return savedValue;
    }

    public void setSavedValue(String savedValue) {
        this.savedValue = savedValue;
    }
}