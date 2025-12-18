package com.qa.framework.tests_OLD.patterns.strategy.specificStrategies;

import com.qa.framework.tests_OLD.patterns.strategy.PaymentStrategy;

// 3. Конкретная стратегия B - ДРУГОЙ способ оплаты
public class PayPalPayment implements PaymentStrategy {
    @Override
    public void pay(double amount) {
        System.out.println("💰 Оплата PayPal: $" + amount);
    }
}