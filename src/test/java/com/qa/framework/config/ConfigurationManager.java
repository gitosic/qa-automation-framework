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

    public static void printConfig() {
        System.out.println("=== Loaded DB configuration ===");
        System.out.println("URL     : " + getDbUrl());
        System.out.println("User    : " + getDbUsername());
        System.out.println("Schema  : " + getDbSchema());
        System.out.println("Password: ***");
        System.out.println("==============================");
    }
}
