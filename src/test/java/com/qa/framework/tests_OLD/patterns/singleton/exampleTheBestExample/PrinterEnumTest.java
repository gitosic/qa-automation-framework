package com.qa.framework.tests_OLD.patterns.singleton.exampleTheBestExample;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PrinterEnumTest {

    @Test
    public void testSingletonReturnsSameInstance() {
        // Получаем первый и второй экземпляр
        Printer printer1 = Printer.getInstance();
        Printer printer2 = Printer.getInstance();

        // Проверяем, что это один и тот же объект
        assertSame(printer1, printer2, "Оба вызова getInstance() должны возвращать один объект");

        // Используем принтер
        printer1.printInfo("Первое сообщение");
        printer2.printInfo("Второе сообщение");

        System.out.println("HashCode printer1: " + System.identityHashCode(printer1));
        System.out.println("HashCode printer2: " + System.identityHashCode(printer2));
    }

    @Test
    public void testPrinterCannotBeCreatedWithNew() {
        // Этот код не скомпилируется - конструктор приватный!
        // PrinterEnum printer = new PrinterEnum(); // ОШИБКА КОМПИЛЯЦИИ!

        // Единственный способ - через getInstance()
        Printer printer = Printer.getInstance();
        assertNotNull(printer);
    }
}