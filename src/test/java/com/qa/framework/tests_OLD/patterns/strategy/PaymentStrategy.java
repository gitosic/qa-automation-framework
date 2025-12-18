package com.qa.framework.tests_OLD.patterns.strategy;

// 1. Интерфейс стратегии - определяет КАК делать
public interface PaymentStrategy {
    void pay(double amount);  // Общий метод для всех стратегий
}