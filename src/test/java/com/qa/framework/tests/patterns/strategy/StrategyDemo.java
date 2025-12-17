package com.qa.framework.tests.patterns.strategy;

import com.qa.framework.tests.patterns.strategy.specificStrategies.CreditCardPayment;
import com.qa.framework.tests.patterns.strategy.specificStrategies.CryptoPayment;
import com.qa.framework.tests.patterns.strategy.specificStrategies.PayPalPayment;

// 5. Использование - меняем стратегию на лету
public class StrategyDemo {
    public static void main(String[] args) {
        ShoppingCart cart = new ShoppingCart();//Создаем корзину

        // 1. Выбираем стратегию "Оплата картой"
        cart.setPaymentStrategy(new CreditCardPayment());//Говорим: "Плачу картой!"
        // 2. Оплачиваем 100$
        // ShoppingCart сам не знает КАК платить, он делегирует это CreditCardPayment
        cart.checkout(100.0);  // "Paying $100.0 with Credit Card" - Говорим: "Оплати 100$!"

        // Меняем стратегию на лету
        cart.setPaymentStrategy(new PayPalPayment());
        cart.checkout(50.0);   // "Paying $50.0 with PayPal" - Говорим: "Оплати 50$!"

        // Меняем стратегию на лету
        cart.setPaymentStrategy(new CryptoPayment());
        cart.checkout(0.2);   // "Paying 0.2 with Crypto" - Говорим: "Оплати 0.2₿!"
    }
}