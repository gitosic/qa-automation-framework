package com.qa.framework.cucumber.runner;


import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.*;

@Suite
@IncludeEngines("cucumber")
// Если фичи лежат в src/test/resources/features, указываем "features"
@SelectClasspathResource("features")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "com.qa.framework.cucumber.steps")
// Эту строку можно оставить для запуска из IDE,
// но Maven перезапишет её через <systemPropertyVariables>, если передать -DincludeTags
@ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME, value = "pretty, io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm")
@ConfigurationParameter(key = Constants.FILTER_TAGS_PROPERTY_NAME, value = "@should1")
public class CucumberTest {
}