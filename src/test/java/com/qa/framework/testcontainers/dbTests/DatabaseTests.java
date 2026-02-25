package com.qa.framework.testcontainers.dbTests;

import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты, демонстрирующие работу с Testcontainers
 *
 * @Testcontainers - волшебная аннотация, которая включает поддержку контейнеров
 */
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)  // Тесты выполняются по порядку
public class DatabaseTests {

    /**
     * @Container - говорит JUnit, что это поле нужно запустить как контейнер
     *
     * НЕ static -> каждый тест получает НОВЫЙ контейнер!
     * Это значит:
     * - Тест 1: контейнер запущен, тест работает, контейнер остановлен
     * - Тест 2: новый контейнер запущен, тест работает, контейнер остановлен
     */
    @Container
    PostgreSQLContainer<?> container = TestDatabaseConfig.createContainer();

    private DatabaseHelper db;

    /**
     * Выполняется ПЕРЕД каждым тестом
     * Контейнер к этому моменту УЖЕ ЗАПУЩЕН!
     */
    @BeforeEach
    void setUp() {
        db = new DatabaseHelper(container);
        System.out.println("\n🔧 Helper создан для теста");
    }

    /**
     * Выполняется ПОСЛЕ каждого теста
     */
    @AfterEach
    void tearDown() {
        if (db != null) {
            db.close();  // Закрываем соединение
            System.out.println("🔧 Helper закрыт\n");
        }
        // Контейнер остановится автоматически после теста
    }

    /**
     * ТЕСТ 1: Создание таблицы и работа с данными
     */
    @Test
    @Order(1)
    void testCreateAndReadData() {
        // 1. Создаём таблицу
        db.executeUpdate("""
            CREATE TABLE employees (
                id SERIAL PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                position VARCHAR(100),
                salary DECIMAL(10, 2)
            )
        """);

        // Проверяем, что таблица создалась
        assertTrue(db.tableExists("employees"));

        // 2. Вставляем данные
        int inserted = db.executeUpdate("""
            INSERT INTO employees (name, position, salary) VALUES
            ('Иван Петров', 'Разработчик', 150000.00),
            ('Мария Сидорова', 'Тестировщик', 120000.00),
            ('Алексей Иванов', 'Аналитик', 130000.00)
        """);

        // Проверяем, что вставилось 3 строки
        assertEquals(3, inserted);
        assertEquals(3, db.getCount("employees"));

        // 3. Читаем данные с фильтром
        List<Map<String, Object>> employees =
                db.executeQuery("SELECT name FROM employees WHERE salary > 125000");

        // Должно быть 2 сотрудника с зарплатой > 125000
        assertEquals(2, employees.size());
    }

    /**
     * ТЕСТ 2: Проверка изоляции
     *
     * Этот тест показывает, что каждый тест начинает с ЧИСТОЙ базы данных
     */
    @Test
    @Order(2)
    void testFreshDatabase() {
        // Таблицы employees из первого теста НЕТ!
        // Потому что контейнер новый
        assertFalse(db.tableExists("employees"));

        // Создаём свою таблицу
        db.executeUpdate("""
            CREATE TABLE departments (
                id SERIAL PRIMARY KEY,
                name VARCHAR(100) NOT NULL
            )
        """);

        assertTrue(db.tableExists("departments"));
    }
}