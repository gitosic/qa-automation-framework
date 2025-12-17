package com.qa.framework.tests.patterns.singleton.exampleTheBestExample;

public class Printer {
    // volatile важно для многопоточности
    private static volatile Printer instance;

    //Приватный конструктор
    private Printer() {
        System.out.println("PrinterEnum инициализирован");
    }

    //Такая базовая конструкция для singleton
    public static Printer getInstance() {
        if (instance == null) {  // Первая проверка (без блокировки)
            synchronized (Printer.class) {  // Блокировка
                if (instance == null) {  // Вторая проверка (под блокировкой)
                    instance = new Printer();
                }
            }
        }
        return instance;
    }

    //Метод эмуляции работы сиглтона
    public void printInfo(String text) {
        System.out.println("Принтер напечатал: " + text);
    }
}