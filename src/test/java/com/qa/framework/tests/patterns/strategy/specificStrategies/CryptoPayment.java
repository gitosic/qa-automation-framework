package com.qa.framework.tests.patterns.strategy.specificStrategies;

import com.qa.framework.tests.patterns.strategy.PaymentStrategy;

// 3. Конкретная стратегия B - ДРУГОЙ способ оплаты
public class CryptoPayment implements PaymentStrategy {
    @Override
    public void pay(double amount) {
        System.out.println("₿ Оплата Crypto: " + amount);
    }
}