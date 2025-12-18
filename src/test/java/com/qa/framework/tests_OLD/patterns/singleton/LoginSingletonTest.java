package com.qa.framework.tests_OLD.patterns.singleton;

import com.qa.framework.core.TestBase;
import com.qa.framework.pages.LoginPage;
import org.junit.jupiter.api.Test;

/**
 * Реальный пример использования Singleton в тесте.
 */
public class LoginSingletonTest extends TestBase {

    @Test
    void testLoginUsingSingletons() {
        // Получаем конфигурацию
        TestConfig config = TestConfig.getInstance();

        // Начинаем логирование
        TestLogger logger = TestLogger.INSTANCE;
        logger.info("🚀 Запуск теста логина");

        // Получаем тестовые данные
        TestDataManager dataManager = TestDataManager.getInstance();
        TestDataManager.User adminUser = dataManager.getUser("admin");

        logger.info("Пользователь для теста: " + adminUser.getUsername());
        logger.debug("Роль: " + adminUser.getRole());

        // Инициализируем драйвер
        WebDriverManager driverManager = WebDriverManager.getInstance();
        driverManager.setBrowserType(config.getBrowser());
        driverManager.initializeDriver();
        driverManager.startSession();

        // Выполняем UI тест
        logger.info("Открываем страницу логина");
        openBankApp("/login");

        LoginPage loginPage = new LoginPage();
        loginPage.isLoaded();

        logger.info("Вводим учетные данные");
        loginPage.enterUsername(adminUser.getUsername())
                .enterPassword(adminUser.getPassword())
                .clickLogin();

        logger.info("Логин выполнен");

        // Завершаем сессию
        driverManager.endSession();
        logger.info("✅ Тест завершен успешно");
    }
}