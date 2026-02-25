package com.qa.framework.testcontainers.dbTests;

import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.*;
import java.util.*;

/**
 * Вспомогательный класс для удобной работы с базой данных в тестах.
 * Инкапсулирует всю низкоуровневую работу с JDBC.
 */
public class DatabaseHelper {

    private final PostgreSQLContainer<?> container;  // Ссылка на контейнер
    private Connection connection;                    // Соединение с БД

    /**
     * Конструктор принимает контейнер, с которым будем работать
     */
    public DatabaseHelper(PostgreSQLContainer<?> container) {
        this.container = container;
    }

    /**
     * Приватный метод для инициализации соединения.
     * Вызывается автоматически перед первой операцией с БД.
     *
     * Pattern: Lazy Initialization (ленивая инициализация)
     */
    private void initConnection() {
        try {
            // Проверяем, нужно ли создавать новое соединение
            if (connection == null || connection.isClosed()) {
                this.connection = TestDatabaseConfig.getConnection(container);
            }
        } catch (SQLException e) {
            throw new RuntimeException("❌ Не удалось создать соединение", e);
        }
    }

    /**
     * Выполнить SQL команду, которая изменяет данные (CREATE, INSERT, UPDATE, DELETE)
     *
     * @param sql SQL запрос
     * @return количество затронутых строк
     */
    public int executeUpdate(String sql) {
        initConnection();  // Убеждаемся, что соединение есть

        try (Statement stmt = connection.createStatement()) {
            return stmt.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException("❌ Ошибка executeUpdate: " + sql, e);
        }
    }

    /**
     * Выполнить SQL запрос и вернуть результат в удобном формате
     *
     * @param sql SELECT запрос
     * @return список записей, где каждая запись - Map<название колонки, значение>
     */
    public List<Map<String, Object>> executeQuery(String sql) {
        initConnection();
        List<Map<String, Object>> results = new ArrayList<>();

        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);           // Выполняем запрос
            var metaData = rs.getMetaData();                 // Получаем метаданные (информацию о колонках)
            int columnCount = metaData.getColumnCount();     // Сколько колонок в результате

            // Проходим по всем строкам результата
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                // Для каждой колонки добавляем значение в Map
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnLabel(i), rs.getObject(i));
                }
                results.add(row);
            }
        } catch (SQLException e) {
            throw new RuntimeException("❌ Ошибка executeQuery: " + sql, e);
        }

        return results;
    }

    /**
     * Проверить, существует ли таблица в базе данных
     *
     * @param tableName имя таблицы
     * @return true если таблица существует
     */
    public boolean tableExists(String tableName) {
        // Запрос к системной таблице information_schema (стандарт SQL)
        String sql = """
            SELECT EXISTS (
                SELECT FROM information_schema.tables 
                WHERE table_name = '%s'
            )
        """.formatted(tableName);

        List<Map<String, Object>> result = executeQuery(sql);
        return (Boolean) result.get(0).get("exists");
    }

    /**
     * Получить количество записей в таблице
     *
     * @param tableName имя таблицы
     * @return количество строк
     */
    public int getCount(String tableName) {
        List<Map<String, Object>> result =
                executeQuery("SELECT COUNT(*) as count FROM " + tableName);

        // COUNT(*) возвращает Long, преобразуем в int
        return ((Long) result.get(0).get("count")).intValue();
    }

    /**
     * Закрыть соединение с БД
     * Важно вызывать после каждого теста!
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ignored) {
            // При закрытии ошибки игнорируем
        }
    }
}