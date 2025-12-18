package com.qa.framework.tests_OLD.patterns.template.realisation;

import com.qa.framework.tests_OLD.patterns.template.TestTemplate;

// 3. Другой тест - тот же шаблон
public class RegistrationTest extends TestTemplate {

    @Override
    protected void setUp() {
        System.out.println("1. Open registration page");
    }

    @Override
    protected void executeTest() {
        System.out.println("2. Fill registration form");
        System.out.println("3. Submit form");
        System.out.println("4. Verify confirmation");
    }

    @Override
    protected void tearDown() {
        System.out.println("5. Delete test user");
    }
}