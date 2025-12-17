package com.qa.framework.tests.patterns.factory;

// Фабрика ошибок для проверок в тестах
public class ErrorFactory {

    public static String createErrorMessage(String testCase) {
        switch (testCase) {
            case "LOGIN_EMPTY":
                return "Поля логин/пароль не могут быть пустыми";
            case "LOGIN_INVALID":
                return "Неверный логин или пароль";
            case "PASSWORD_SHORT":
                return "Пароль должен быть не менее 8 символов";
            case "EMAIL_INVALID":
                return "Неверный формат email";
            default:
                return "Неизвестная ошибка";
        }
    }

    // Тест
    public static void main(String[] args) {
        System.out.println(ErrorFactory.createErrorMessage("LOGIN_EMPTY"));
        System.out.println(ErrorFactory.createErrorMessage("LOGIN_INVALID"));
    }
}