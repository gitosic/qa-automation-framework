package com.qa.framework.testcontainers.dbTests;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Класс-конфигуратор для создания и настройки контейнера PostgreSQL
 */
public class TestDatabaseConfig {

    /**
     * Фабричный метод для создания контейнера PostgreSQL
     *
     * @return настроенный контейнер PostgreSQL
     */
    public static PostgreSQLContainer<?> createContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
                .withDatabaseName("test_db")      // Имя БД внутри контейнера
                .withUsername("test_user")        // Имя пользователя
                .withPassword("test_password");   // Пароль
    }

    /**
     * Получить JDBC соединение с контейнером
     *
     * @param container запущенный контейнер PostgreSQL
     * @return Connection для работы с БД
     * @throws SQLException если подключиться не удалось
     */
    public static Connection getConnection(PostgreSQLContainer<?> container) throws SQLException {
        return DriverManager.getConnection(
                container.getJdbcUrl(),      // Автоматически сгенерированный URL (jdbc:postgresql://localhost:54321/test_db)
                container.getUsername(),     // test_user
                container.getPassword()      // test_password
        );
    }
}