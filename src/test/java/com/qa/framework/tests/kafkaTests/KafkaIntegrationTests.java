package com.qa.framework.tests.kafkaTests;

import com.qa.framework.config.ConfigurationManager;
import com.qa.framework.kafka.ConsumerAdapter;
import com.qa.framework.kafka.KafkaMessage;
import com.qa.framework.kafka.ProducerAdapter;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.header.Header;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Isolated;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.qa.framework.kafka.KafkaUtils.createTestMessage;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Isolated
@Tag("kafka-tests")
public class KafkaIntegrationTests {

    // КОНФИГУРАЦИЯ ТЕСТА
    private String bootstrapServers;
    private String testTopic;
    private String testGroupId;
    private Map<String, String> testMessages = new ConcurrentHashMap<>();

    @BeforeAll
    void setup() {
        // Используем конфигурацию из вашего ConfigurationManager
        bootstrapServers = ConfigurationManager.getKafkaBootstrapServers();
        testTopic = ConfigurationManager.getProperty("test.kafka.topic", "test_topic");
        // Динамический Group ID, чтобы не мешать другим тестам
        testGroupId = "test-group-" + System.currentTimeMillis();

        System.out.println("🚀 Настройка тестов адаптации Kafka");
        System.out.println("Bootstrap Servers: " + bootstrapServers);
        System.out.println("Test Topic: " + testTopic);
        System.out.println("Test Group ID: " + testGroupId);
    }

    @Test
    @Order(1)
    @Tag("kafka-smoke")
    @DisplayName("Smoke тест Kafka: подключение, отправка, чтение")
    void kafkaSmokeTest() {
        System.out.println("\n🚬 Kafka Smoke Test");

        // 1. Проверка подключения (отправка простого сообщения)
        Map<String, Object> message = Map.of(
                "smokeTest", true,
                "timestamp", System.currentTimeMillis()
        );

        boolean sent = ProducerAdapter.sendMessage(bootstrapServers, testTopic, message);
        assertTrue(sent, "Не удалось отправить сообщение в Kafka");
        System.out.println("✅ Отправка сообщения - OK");

        // 2. Проверка чтения (ждем немного и читаем)
        try { Thread.sleep(1000); } catch (InterruptedException e) {}

        ConsumerRecords<String, String> records = ConsumerAdapter.readMessage(
                bootstrapServers, testTopic, "smoke-test-group-" + System.currentTimeMillis());

        assertFalse(records.isEmpty(), "Не удалось прочитать сообщения из Kafka");
        System.out.println("✅ Чтение сообщений - OK");

        // 3. Проверка структуры сообщения
        List<KafkaMessage> messages = ConsumerAdapter.convertRecordsToMessageObject(records);
        assertFalse(messages.isEmpty(), "Не удалось конвертировать сообщения");
        System.out.println("✅ Конвертация сообщений - OK");

        System.out.println("🎉 Kafka smoke test пройден!");
    }

    @Test
    @Order(2)
    @Tag("kafka-producer-basic")
    @DisplayName("Тест отправки 5 сообщений в Kafka без хедоров")
    void testSendMessageToKafka() {
        System.out.println("\n📬 Тест отправки сообщения без X-Transaction-Req-Id");
        // Отправляем 5 тестовых сообщений
        AtomicInteger count = new AtomicInteger(1);
        for (int i = 0; i < 5; i++) {
            String orderId = "ORD-" + System.currentTimeMillis() + "-" + count.getAndIncrement();
            Map<String, Object> message = createTestMessage(orderId, "CUST-001", 100.0 * i, "CREATED");
            boolean sent = ProducerAdapter.sendMessage(bootstrapServers, testTopic, message);
            assertTrue(sent, "Сообщение должно быть отправлено успешно");
            testMessages.put(orderId, (String) message.get("testRunId"));
        }
    }

    @Test
    @Order(3)
    @Tag("kafka-producer-transaction")
    @DisplayName("Тест отправки 1 сообщения с header-ом X-Transaction-Req-Id")
    void testSendMessageWithTransactionId() {
        System.out.println("\n💳 Тест отправки сообщения c X-Transaction-Req-Id");
        String orderId = "ORD-TX-" + System.currentTimeMillis();
        Map<String, Object> message = createTestMessage(orderId, "CUST-002", 500.0, "PENDING");

        String transactionId = ProducerAdapter.sendMessageWithTransactionId(
                bootstrapServers,
                testTopic,
                message
        );

        assertNotNull(transactionId, "Сообщение header-ом X-Transaction-Req-Id должно быть отправлено успешно");
        testMessages.put(orderId, (String) message.get("testRunId"));
        System.out.println("   Сгенерированный X-Transaction-Req-Id: " + transactionId);
    }

    @Test
    @Order(4)
    @Tag("kafka-producer-headers")
    @DisplayName("Тест отправки сообщения с заголовком X-Transaction-Req-Id")
    void testPrepareTransactionHeader() {
        System.out.println("\n🧾 Тест заголовка X-Transaction-Req-Id");

        Map<String, Object> message = new HashMap<>();
        message.put("requestId", "REQ-" + System.currentTimeMillis());
        message.put("applicationId", "APP-001");
        message.put("type", "PREPARE");

        Map<String, String> headers = new HashMap<>();
        String transactionId = "TX-" + System.currentTimeMillis();
        headers.put("X-Transaction-Req-Id", transactionId);
        headers.put("X-Initiator-Service", "test-adaptation-service");

        boolean sent = ProducerAdapter.sendMessageWithHeaders(
                bootstrapServers,
                testTopic,
                message,
                headers
        );

        assertTrue(sent, "Сообщение с transaction header должно быть отправлено");
        System.out.println("✅ Отправлено с X-Transaction-Req-Id: " + transactionId);
    }

    @Test
    @Order(5)
    @Tag("kafka-consumer-validation")
    @DisplayName("Тест чтения сообщений из Kafka в котором может присутствовать определенный хедор")
    void testReadMessagesFromKafka() {
        System.out.println("\n🔎 Тест чтения сообщений");
        // Читаем последние 5 сообщений - см. NUMBER_LAST_MESSAGE в ConsumerAdapter
        ConsumerRecords<String, String> consumerRecords = ConsumerAdapter.readMessage(
                bootstrapServers,
                testTopic,
                testGroupId
        );

        assertFalse(consumerRecords.isEmpty(), "Из топика должны быть прочитаны сообщения");

        // Проверяем, что прочитаны сообщения, которые мы отправили
        List<KafkaMessage> messages = ConsumerAdapter.convertRecordsToMessageObject(consumerRecords);
        assertFalse(messages.isEmpty(), "Список сообщений не должен быть пустым");

        // Проверка наличия любого заголовка
        long batchMessages = messages.stream()
                .filter(msg -> msg.getHeaders() != null && !msg.getHeaders().isEmpty())
                .count();

        assertTrue(batchMessages > 0, "Должно быть найдено хотя бы одно сообщение с любым заголовком");

        // Проверка на наличие конкретного заголовка X-Transaction-Req-Id
        long messagesWithTransactionId = messages.stream()
                .filter(msg -> msg.getHeaders() != null)
                .filter(msg -> msg.getHeaders().stream()
                        .anyMatch(header -> "X-Transaction-Req-Id".equals(header.key())))
                .count();

        assertTrue(messagesWithTransactionId > 0,
                "Должно быть найдено хотя бы одно сообщение с заголовком X-Transaction-Req-Id");


        System.out.println("   Прочитано сообщений с заголовками: " + batchMessages);

        System.out.println("\n📰 Детали прочитанных сообщений:");
        messages.forEach(msg -> {
            System.out.println("   Message Body: " + msg.getBody());
            if (msg.getHeaders() != null && !msg.getHeaders().isEmpty()) {
                System.out.println("   Headers Found: " + msg.getHeaders().size());
                for (Header header : msg.getHeaders()) {
                    System.out.println("   Header: " + header.key() + " = " +
                            new String(header.value()));
                }
            }
        });

    }

}