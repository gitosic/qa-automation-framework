package com.qa.framework.tests;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Selenide.open;

public class SimpleSelenoidTest {

    @Tag("DataFromCsvSource2")
    @Test
    public void testSelenoidConnection() {
        Configuration.remote = "http://localhost:4444/wd/hub";
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";

        open("http://google.com");
        Selenide.sleep(2000);
    }
}