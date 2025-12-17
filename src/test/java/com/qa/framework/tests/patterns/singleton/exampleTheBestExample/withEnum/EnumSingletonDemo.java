package com.qa.framework.tests.patterns.singleton.exampleTheBestExample.withEnum;

import java.lang.reflect.Constructor;

public class EnumSingletonDemo {

    public static void main(String[] args) {
        System.out.println("=== ДЕМОНСТРАЦИЯ ENUM SINGLETON ===");

        // 1. Получаем экземпляры разными способами
        PrinterEnum p1 = PrinterEnum.INSTANCE;
        PrinterEnum p2 = PrinterEnum.INSTANCE;

        // 2. Проверяем, что это один объект
        System.out.println("p1 == p2? " + (p1 == p2));  // true
        System.out.println("HashCode p1: " + System.identityHashCode(p1));
        System.out.println("HashCode p2: " + System.identityHashCode(p2));

        // 3. Используем
        p1.printInfo("Первая страница");
        p2.printInfo("Вторая страница");
        PrinterEnum.INSTANCE.printInfo("Третья страница");

        // 4. Пытаемся создать новый (не получится!)
        System.out.println("\nПопытка создать через рефлексию:");
        try {
            // Этот код вызовет исключение - нельзя создать enum через рефлексию!
             Constructor<PrinterEnum> constructor = PrinterEnum.class.getDeclaredConstructor();
             constructor.setAccessible(true);
             PrinterEnum p3 = constructor.newInstance(); // ОШИБКА!
        } catch (Exception e) {
            System.out.println("✅ Защита от рефлексии работает!");
        }
    }
}