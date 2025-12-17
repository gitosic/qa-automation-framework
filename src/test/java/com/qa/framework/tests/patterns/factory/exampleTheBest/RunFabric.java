package com.qa.framework.tests.patterns.factory.exampleTheBest;

public class RunFabric {

    enum UserType {SMALL, BIG}

    public static void main(String[] args) {
        // 1. Демонстрация создания разных коробок через фабрику
        Box box = FabricBoxes.checkBox(UserType.BIG.toString()); // Подставляем в кавычки нужны названия коробок из enum
        System.out.println("Box created: " + box);

        // 2. Показываем обработку ошибки
        try {
            Box box2 = FabricBoxes.checkBox("unexpectedBox"); // Подставляем в кавычки нужны названия коробок
            System.out.println("Box created: " + box2);
        } catch (IllegalArgumentException e) {
            System.out.println("Expected error: " + e.getMessage());
        }


    }
}
