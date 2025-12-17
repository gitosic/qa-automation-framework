package com.qa.framework.tests.patterns.singleton.exampleTheBestExample;

/**
 * Демонстрация, почему Singleton полезен
 */
public class Office {

    public static void main(String[] args) {
        System.out.println("🏢 Офис открывается...");

        // Сотрудник 1 хочет распечатать
        System.out.println("\n👨‍💼 Сотрудник 1:");
        Printer employee1Printer = Printer.getInstance();
        employee1Printer.printInfo("Отчет за январь");

        // Сотрудник 2 хочет распечатать
        System.out.println("\n👩‍💼 Сотрудник 2:");
        Printer employee2Printer = Printer.getInstance();
        employee2Printer.printInfo("Презентация для клиента");

        // Сотрудник 3 хочет распечатать
        System.out.println("\n👨‍💼 Сотрудник 3:");
        Printer employee3Printer = Printer.getInstance();
        employee3Printer.printInfo("Счет на оплату");

        // Проверяем, что все используют один принтер
        System.out.println("\n🔍 Проверка:");
        System.out.println("employee1Printer == employee2Printer? " +
                (employee1Printer == employee2Printer)); // true
        System.out.println("employee2Printer == employee3Printer? " +
                (employee2Printer == employee3Printer)); // true

        System.out.println("\n✅ Все сотрудники используют один и тот же принтер!");
    }
}