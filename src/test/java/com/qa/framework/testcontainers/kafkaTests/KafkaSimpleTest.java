package com.qa.framework.testcontainers.kafkaTests;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ПЕРВЫЙ ТЕСТ: Простая отправка и чтение сообщений из Kafka
 * <p>
 * Этот тест показывает базовую работу с Kafka в Testcontainers:
 * 1. Запуск Kafka контейнера
 * 2. Отправка сообщения
 * 3. Чтение сообщения
 * 4. Проверка, что прочитали то, что отправили
 */
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class KafkaSimpleTest {

    /**
     * Kafka контейнер.
     * НЕ static → новый контейнер на каждый тест (полная изоляция)
     */
    @Container
    KafkaContainer kafkaContainer = KafkaTestContainerConfig.createContainer();

    private KafkaTestHelper kafkaHelper;
    private String testTopic;

    @BeforeEach
    void setUp() {
        // Создаём helper для работы с Kafka
        kafkaHelper = new KafkaTestHelper(kafkaContainer);

        // Создаём уникальное имя топика для каждого теста
        testTopic = "test-topic-" + UUID.randomUUID().toString().substring(0, 8);

        System.out.println("\n🔧 Kafka Helper создан для теста");
        System.out.println("📋 Bootstrap Servers: " + kafkaContainer.getBootstrapServers());
        System.out.println("📋 Тестовый топик: " + testTopic);
    }

    @AfterEach
    void tearDown() {
        System.out.println("🔧 Тест завершён\n");
    }

    /**
     * ТЕСТ 1: Отправка и чтение простого текстового сообщения
     */
    @Test
    @Order(1)
    @DisplayName("📨 Тест 1: Отправка и чтение простого сообщения")
    void testSendAndReceiveSimpleMessage() {
        System.out.println("\n=== ТЕСТ 1: Простое сообщение ===");

        // 1. Создаём простое текстовое сообщение
        String message = "Привет, Kafka! Тестовое сообщение " + System.currentTimeMillis();
        System.out.println("📝 Отправляем сообщение: " + message);

        // 2. Отправляем сообщение
        boolean sent = kafkaHelper.sendMessage(testTopic, message);
        assertTrue(sent, "Сообщение должно отправиться успешно");

        // 3. Даём время на обработку
        System.out.println("⏳ Ждём 2 секунды для обработки...");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 4. Читаем сообщения
        String groupId = "test-consumer-" + UUID.randomUUID().toString().substring(0, 8);
        List<ConsumerRecord<String, String>> messages = kafkaHelper.readMessages(
                testTopic, groupId, 1, 5
        );

        // 5. Проверяем, что прочитали одно сообщение
        assertEquals(1, messages.size(), "Должно быть прочитано 1 сообщение");

        // 6. Проверяем, что это то же сообщение
        ConsumerRecord<String, String> received = messages.get(0);
        assertEquals(message, received.value(), "Сообщение должно совпадать с отправленным");

        System.out.println("✅ Прочитанное сообщение совпадает с отправленным!");
    }

    /**
     * ТЕСТ 2: Отправка и чтение JSON сообщения
     */
    @Test
    @Order(2)
    @DisplayName("📦 Тест 2: Отправка и чтение JSON сообщения")
    void testSendAndReceiveJsonMessage() {
        System.out.println("\n=== ТЕСТ 2: JSON сообщение ===");

        // 1. Создаём тестовые данные (как в вашем KafkaUtils.createTestMessage)
        String orderId = "ORDER-" + System.currentTimeMillis();
        Map<String, Object> testMessage = KafkaTestHelper.createTestMessage(
                orderId,
                "CUST-12345",
                1500.00,
                "NEW"
        );

        System.out.println("📦 Отправляем JSON:");
        testMessage.forEach((key, value) ->
                System.out.println("   " + key + ": " + value)
        );

        // 2. Отправляем сообщение
        boolean sent = kafkaHelper.sendMessage(testTopic, testMessage);
        assertTrue(sent, "JSON сообщение должно отправиться успешно");

        // 3. Даём время на обработку
        System.out.println("⏳ Ждём 2 секунды...");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 4. Читаем сообщения
        String groupId = "json-consumer-" + UUID.randomUUID().toString().substring(0, 8);
        List<ConsumerRecord<String, String>> messages = kafkaHelper.readMessages(
                testTopic, groupId, 1, 5
        );

        // 5. Проверяем
        assertEquals(1, messages.size(), "Должно быть прочитано 1 сообщение");

        // 6. Преобразуем JSON обратно в Map для проверки
        String receivedJson = messages.get(0).value();
        com.google.gson.Gson gson = new com.google.gson.Gson();
        Map<String, Object> receivedMap = gson.fromJson(receivedJson, Map.class);

        // 7. Проверяем поля
        assertEquals(orderId, receivedMap.get("orderId"), "orderId должен совпадать");
        assertEquals("CUST-12345", receivedMap.get("customerId"), "customerId должен совпадать");
        assertEquals(1500.00, ((Double) receivedMap.get("amount")).doubleValue(), 0.001);
        assertEquals("NEW", receivedMap.get("status"));
        assertNotNull(receivedMap.get("testRunId"), "testRunId должен присутствовать");

        System.out.println("✅ JSON сообщение успешно прочитано и верифицировано");
        System.out.println("   testRunId: " + receivedMap.get("testRunId"));
    }

    /**
     * ТЕСТ 3: Отправка с заголовками
     */
    @Test
    @Order(3)
    @DisplayName("🏷️ Тест 3: Отправка с заголовками")
    void testSendWithHeaders() {
        System.out.println("\n=== ТЕСТ 3: Сообщение с заголовками ===");

        // 1. Создаём сообщение
        Map<String, String> testData = Map.of(
                "action", "UPDATE",
                "entityId", "ENT-999"
        );

        // 2. Создаём заголовки (как в вашем ProducerAdapter)
        Map<String, String> headers = new HashMap<>();
        String transactionId = UUID.randomUUID().toString();
        headers.put("X-Transaction-Req-Id", transactionId);
        headers.put("X-Initiator-Service", "test-service");
        headers.put("X-Content-Type", "application/json");

        System.out.println("📋 Заголовки:");
        headers.forEach((key, value) ->
                System.out.println("   " + key + ": " + value)
        );

        // 3. Отправляем сообщение с заголовками
        boolean sent = kafkaHelper.sendMessage(testTopic, testData, headers);
        assertTrue(sent, "Сообщение с заголовками должно отправиться");

        // 4. Даём время
        System.out.println("⏳ Ждём 2 секунды...");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 5. Читаем сообщения
        String groupId = "headers-consumer-" + UUID.randomUUID().toString().substring(0, 8);
        List<ConsumerRecord<String, String>> messages = kafkaHelper.readMessages(
                testTopic, groupId, 1, 5
        );

        // 6. Проверяем заголовки
        assertEquals(1, messages.size());
        ConsumerRecord<String, String> record = messages.get(0);

        // Проверяем наличие наших заголовков
        var headersIterator = record.headers().headers("X-Transaction-Req-Id");
        assertTrue(headersIterator.iterator().hasNext(), "Заголовок X-Transaction-Req-Id должен присутствовать");

        byte[] headerValue = headersIterator.iterator().next().value();
        String receivedTransactionId = new String(headerValue);
        assertEquals(transactionId, receivedTransactionId, "Transaction ID должен совпадать");

        System.out.println("✅ Заголовки успешно проверены");
    }

    /**
     * ТЕСТ 4: Проверка изоляции между тестами
     */
    @Test
    @Order(4)
    @DisplayName("🧪 Тест 4: Проверка изоляции (чистый контейнер)")
    void testIsolation() {
        System.out.println("\n=== ТЕСТ 4: Проверка изоляции ===");

        // Проверяем, что предыдущие тесты не оставили сообщений
        String groupId = "isolation-consumer-" + UUID.randomUUID().toString().substring(0, 8);

        // Пытаемся прочитать сообщения - их не должно быть
        List<ConsumerRecord<String, String>> messages = kafkaHelper.readMessages(
                testTopic, groupId, 1, 3
        );

        assertTrue(messages.isEmpty(), "Новый контейнер должен быть пустым");
        System.out.println("✅ Контейнер действительно чистый - сообщений нет");
    }
}