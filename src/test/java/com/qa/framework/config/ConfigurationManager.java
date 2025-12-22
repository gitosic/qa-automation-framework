package com.qa.framework.config;

import java.io.InputStream;
import java.util.Properties;

public final class ConfigurationManager {

    private static final Properties properties = new Properties();
    private static final String ENV_PROPERTY = "test.env";

    static {
        load();
    }

    private ConfigurationManager() {
    }

    private static void load() {
        String environment = System.getProperty(ENV_PROPERTY, "st");
        String fileName = environment + ".properties";

        System.out.println("🔧 Test environment: " + environment);

        try (InputStream is = ConfigurationManager.class
                .getClassLoader()
                .getResourceAsStream(fileName)) {

            if (is == null) {
                throw new RuntimeException("❌ Configuration file not found: " + fileName);
            }

            properties.load(is);
            printConfig();

        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to load config: " + fileName, e);
        }
    }

    public static String getDbUrl() {
        return getRequired("db.url");
    }

    public static String getDbUsername() {
        return getRequired("db.username");
    }

    public static String getDbPassword() {
        return getRequired("db.password");
    }

    public static String getDbSchema() {
        return getRequired("db.schema");
    }

    private static String getRequired(String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new RuntimeException("❌ Missing required property: " + key);
        }
        return value;
    }

    public static Properties getProperties() {
        return new Properties(properties); // Возвращаем копию для безопасности
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    // МЕТОДЫ ДЛЯ KAFKA
    public static String getKafkaBootstrapServers() {
        return getProperty("kafka.bootstrap.servers", "localhost:9092");
    }

    public static String getKafkaIncomingOrdersTopic() {
        return getProperty("kafka.topic.incoming_orders", "incoming_orders");
    }

    public static String getKafkaUserActivitiesTopic() {
        return getProperty("kafka.topic.user_activities", "user_activities");
    }

    public static String getKafkaSystemLogsTopic() {
        return getProperty("kafka.topic.system_logs", "system_logs");
    }

    // МЕТОДЫ ДЛЯ ПРИЛОЖЕНИЯ
    public static String getAppBaseUrl() {
        return getProperty("app.base.url");
    }

    public static String getAppApiUrl() {
        return getProperty("app.api.url");
    }

    // МЕТОДЫ ДЛЯ ТЕСТОВЫХ НАСТРОЕК
    public static int getTestTimeout() {
        return Integer.parseInt(getProperty("test.timeout", "30"));
    }

    public static String getTestBrowser() {
        return getProperty("test.browser", "chrome");
    }

    public static void printConfig() {
        System.out.println("=== Loaded DB configuration ===");
        System.out.println("URL     : " + getDbUrl());
        System.out.println("User    : " + getDbUsername());
        System.out.println("Schema  : " + getDbSchema());
        System.out.println("Password: ***");
        System.out.println("==============================");

        // Печатаем Kafka конфигурацию если она есть
        String kafkaServers = getKafkaBootstrapServers();
        if (kafkaServers != null && !kafkaServers.isEmpty()) {
            System.out.println("=== Kafka Configuration ===");
            System.out.println("Bootstrap Servers: " + kafkaServers);
            System.out.println("Topics: " + getKafkaIncomingOrdersTopic() + ", " +
                    getKafkaUserActivitiesTopic() + ", " + getKafkaSystemLogsTopic());
            System.out.println("==========================");
        }
    }
}