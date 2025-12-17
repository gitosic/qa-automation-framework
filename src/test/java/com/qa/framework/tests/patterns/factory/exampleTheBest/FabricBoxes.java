package com.qa.framework.tests.patterns.factory.exampleTheBest;

public class FabricBoxes {
    public static Box checkBox(String type) {
        switch (type.toLowerCase()) {
            case "small":
                return new Box(5, 5);
            case "big":
                return new Box(10, 10);
            default:
                throw new IllegalArgumentException("Неизвестная коробка: " + type);
        }
    }

}
