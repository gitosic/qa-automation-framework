package com.qa.framework.tests.patterns.strategy;

import lombok.Setter;

// 4. Контекст - выбирает КАКУЮ стратегию использовать
public class ShoppingCart {
    private PaymentStrategy paymentStrategy;  // Храним выбранную стратегию, paymentStrategy - это способ оплаты (карта/PayPal)

    // Устанавливаем стратегию - можно использовать аннотацию @Setter от lombok
    public void setPaymentStrategy(PaymentStrategy strategy) {
        this.paymentStrategy = strategy;
    }

    // Используем стратегию
    //checkout - это "произвести оплату"
    //paymentStrategy - это "способ оплаты" (карта, PayPal и т.д.)
    //paymentStrategy.pay(amount) - это "оплатить выбранным способом"
    // ВОТ ЗДЕСЬ ПРОИСХОДИТ "МАГИЯ" STRATEGY PATTERN! Мы не знаем КАК оплатят, мы знаем ЧТО нужно оплатить
    // А КАК оплатить - решит paymentStrategy
    public void checkout(double amount) {
        paymentStrategy.pay(amount);
    }
    /*
    Представьте кассу в магазине:

    public class Касса {
    private СпособОплаты способОплаты;// Может быть: наличные/карта

    public void оплатить(double сумма){
        способОплаты.произвестиОплату(сумма);// Делегируем оплату
       }
    }
     */

}