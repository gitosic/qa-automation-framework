package com.qa.framework.tests.patterns.factory.example;

import org.junit.jupiter.api.Test;

public class FactoryExampleTest {

    @Test
    void testFactoryPattern() {
        // Правильное использование существующего класса
        TestUserFactory.FactoryPatternDemo demo = new TestUserFactory.FactoryPatternDemo();
        demo.runFullDemo();
    }
}