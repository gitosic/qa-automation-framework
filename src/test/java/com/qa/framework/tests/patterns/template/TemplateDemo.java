package com.qa.framework.tests.patterns.template;

import com.qa.framework.tests.patterns.template.realisation.LoginTest;
import com.qa.framework.tests.patterns.template.realisation.RegistrationTest;

// 4. Запускаем тесты по шаблону
public class TemplateDemo {
    public static void main(String[] args) {
        System.out.println("=== Login Test ===");
        TestTemplate loginTest = new LoginTest();
        loginTest.runTest();  // Выполняется по шаблону из родителя

        System.out.println("\n=== Registration Test ===");
        TestTemplate regTest = new RegistrationTest();
        regTest.runTest();    // Тот же шаблон, другая реализация
    }
}