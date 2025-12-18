package com.qa.framework.tests_OLD.patterns.singleton.exampleTheBestExample.withEnum;

/**
 * Самый безопасный способ реализовать Singleton в Java — через enum,
 * так как он:
 * потокобезопасен
 * защищён от Reflection
 * корректно работает с Serialization
 * Однако в реальных проектах, особенно со Spring, чаще используют Singleton как Spring Bean, а не через enum.
 */
public enum PrinterEnum {
    INSTANCE;  // <- Это ВСЁ! Больше ничего не нужно!

    private PrinterEnum() {
        System.out.println("PrinterEnum инициализирован");
    }

    public void printInfo(String text) {
        System.out.println("Принтер напечатал: " + text);
    }
}