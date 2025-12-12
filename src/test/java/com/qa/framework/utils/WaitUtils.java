package com.qa.framework.utils;

import java.time.Duration;
import java.util.function.Supplier;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class WaitUtils {

    // Стандартные таймауты лучше вынести в конфиг
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    public static final Duration POLLING_INTERVAL = Duration.ofMillis(500);

    // Функциональный метод для ожидания условия, заданного Supplier'ом
    // Supplier<T> - это функциональный интерфейс, у которого есть метод get(), возвращающий значение типа T.
    // Мы будем использовать Supplier<Boolean>, который возвращает true, когда условие выполнено.
    public static void waitForCondition(Supplier<Boolean> condition, Duration timeout, String errorMessage) {
        Duration endTime = Duration.ofMillis(System.currentTimeMillis()).plus(timeout);

        while (Duration.ofMillis(System.currentTimeMillis()).compareTo(endTime) < 0) {
            try {
                if (condition.get()) {
                    return; // Условие выполнено, выходим
                }
            } catch (Exception e) {
                // Игнорируем исключения (например, NoSuchElementException) и пытаемся снова
            }
            try {
                Thread.sleep(POLLING_INTERVAL.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Wait was interrupted", e);
            }
        }
        throw new AssertionError(errorMessage + " Timeout: " + timeout.getSeconds() + "s");
    }

    // Перегруженный метод с дефолтным таймаутом
    public static void waitForCondition(Supplier<Boolean> condition, String errorMessage) {
        waitForCondition(condition, DEFAULT_TIMEOUT, errorMessage);
    }

    // Специализированный метод для ожидания элемента (более удобный, чем общий waitForCondition)
    // Здесь мы используем встроенные ожидания Selenide, но идея та же.
    // Мы принимаем селектор и условие (например, visible).
    // Consumer<SelenideElement> - это интерфейс, который что-то делает с переданным объектом (void).
    // Но для условий Selenide у нас уже есть готовые методы.
    public static void waitForElement(String cssSelector, Duration timeout) {
        $(cssSelector).shouldBe(visible, timeout);
    }
}