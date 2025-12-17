package com.qa.framework.tests.patterns.builder;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/**
 * Класс User с автоматическим Builder от Lombok.
 * Демонстрация Builder Pattern в 1 классе.
 */
@Builder
@Getter
@ToString
public class User {
    @NonNull private String username;
    @NonNull private String password;
    @Builder.Default private String role = "User";
    @Builder.Default private boolean isActive = true;
    private String email;

    // Можно добавить кастомную логику в build()
//    public static UserBuilder builder() {
//        return new UserBuilder();
//    }
}