package com.qa.framework.tests.patterns.strategy.specificStrategies;

import com.qa.framework.tests.patterns.strategy.PaymentStrategy;

// 2. Конкретная стратегия A - ОДИН способ оплаты
public class CreditCardPayment implements PaymentStrategy {
    @Override
    public void pay(double amount) {
        System.out.println("💳 Оплата картой: $" + amount);
    }
}