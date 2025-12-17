package com.qa.framework.tests.patterns.singleton;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Тест для демонстрации Singleton Pattern.
 */
@DisplayName("Singleton Pattern Test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SingletonPatternTest {

    @BeforeEach
    void setUp() {
        System.out.println("\n" + "=".repeat(50));
    }

    @Test
    @Order(1)
    @DisplayName("1. Простой Singleton")
    void testSimpleSingleton() {
        System.out.println("Тест: Простой Singleton");

        AppConfig config1 = AppConfig.getInstance();
        AppConfig config2 = AppConfig.getInstance();

        assertSame(config1, config2);

        config1.setBrowser("firefox");
        assertEquals("firefox", config2.getBrowser());

        System.out.println("✅ Browser: " + config1.getBrowser());
        System.out.println("✅ Timeout: " + config1.getTimeout());
    }

    @Test
    @Order(2)
    @DisplayName("2. Thread-safe Singleton")
    void testThreadSafeSingleton() {
        System.out.println("Тест: Thread-safe Singleton");

        TestConfig config1 = TestConfig.getInstance();
        TestConfig config2 = TestConfig.getInstance();

        assertSame(config1, config2);

        System.out.println("✅ Browser: " + config1.getBrowser());
        System.out.println("✅ Environment: " + config1.getEnvironment());
        System.out.println("✅ Headless: " + config1.isHeadless());
    }

    @Test
    @Order(3)
    @DisplayName("3. Enum Singleton")
    void testEnumSingleton() {
        System.out.println("Тест: Enum Singleton");

        TestLogger logger1 = TestLogger.INSTANCE;
        TestLogger logger2 = TestLogger.INSTANCE;

        assertSame(logger1, logger2);

        logger1.info("Информационное сообщение");
        logger1.debug("Отладочное сообщение");
        logger1.warn("Предупреждение");
        logger1.error("Ошибка");

        System.out.println("✅ Логирование работает");
    }

    @Test
    @Order(4)
    @DisplayName("4. Data Manager Singleton")
    void testDataManagerSingleton() {
        System.out.println("Тест: Data Manager Singleton");

        TestDataManager manager = TestDataManager.getInstance();

        TestDataManager.User admin = manager.getUser("admin");
        TestDataManager.User user = manager.getUser("user");

        assertNotNull(admin);
        assertNotNull(user);

        System.out.println("✅ Admin: " + admin);
        System.out.println("✅ User: " + user);
        System.out.println("✅ API URL: " + manager.getConfig("api.url"));

        // Создаем нового пользователя
        TestDataManager.User newUser = TestDataManager.User.builder()
                .username("qa_tester")
                .password("qa_password")
                .role("QA")
                .email("qa@test.com")
                .build();

        manager.addUser("qa", newUser);
        assertEquals("QA", manager.getUser("qa").getRole());

        System.out.println("✅ Новый пользователь: " + newUser);
    }

    @Test
    @Order(5)
    @DisplayName("5. WebDriver Manager Singleton")
    void testWebDriverManager() {
        System.out.println("Тест: WebDriver Manager Singleton");

        WebDriverManager.reset(); // Сброс для теста

        WebDriverManager manager1 = WebDriverManager.getInstance();
        WebDriverManager manager2 = WebDriverManager.getInstance();

        assertSame(manager1, manager2);

        manager1.setBrowserType("chrome");
        manager1.initializeDriver();
        manager1.startSession();

        assertEquals("SESSION_ACTIVE", manager2.getDriverStatus());
        assertEquals(1, manager2.getSessionCount());

        System.out.println("✅ Статус: " + manager1.getDriverStatus());
        System.out.println("✅ Сессий: " + manager1.getSessionCount());
    }

    @Test
    @Order(6)
    @DisplayName("6. Комплексный пример")
    void testComplexExample() {
        System.out.println("Тест: Комплексный пример");

        // Получаем все Singleton'ы
        TestConfig config = TestConfig.getInstance();
        TestLogger logger = TestLogger.INSTANCE;
        TestDataManager dataManager = TestDataManager.getInstance();
        WebDriverManager driverManager = WebDriverManager.getInstance();

        logger.info("=== Начало комплексного теста ===");
        logger.info("Среда: " + config.getEnvironment());
        logger.info("Браузер: " + config.getBrowser());

        // Получаем тестовые данные
        TestDataManager.User testUser = dataManager.getUser("admin");
        logger.info("Тестовый пользователь: " + testUser.getUsername());

        // Инициализируем драйвер
        driverManager.setBrowserType(config.getBrowser());
        driverManager.initializeDriver();
        driverManager.startSession();

        logger.info("Статус драйвера: " + driverManager.getDriverStatus());
        logger.info("=== Тест завершен ===");

        System.out.println("✅ Все Singleton'ы работают вместе!");
    }
}