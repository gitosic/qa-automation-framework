package com.qa.framework.tests.patterns.template.realisation;

import com.qa.framework.tests.patterns.template.TestTemplate;

// 2. Конкретный тест - наследуем шаблон
public class LoginTest extends TestTemplate {

    @Override
    protected void setUp() {
        System.out.println("1. Open browser");
        System.out.println("2. Navigate to login page");
    }

    @Override
    protected void executeTest() {
        System.out.println("3. Enter credentials");
        System.out.println("4. Click login button");
        System.out.println("5. Verify dashboard");
    }

    @Override
    protected void tearDown() {
        System.out.println("6. Logout");
        System.out.println("7. Close browser");
    }
}