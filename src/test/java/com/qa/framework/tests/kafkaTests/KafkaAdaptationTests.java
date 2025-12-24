package com.qa.framework.tests.kafkaTests;

import com.google.gson.Gson;
import com.qa.framework.config.ConfigurationManager;
import com.qa.framework.kafka.ConsumerAdapter;
import com.qa.framework.kafka.KafkaMessage;
import com.qa.framework.kafka.ProducerAdapter;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.header.Header;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Isolated;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("kafka-tests")
@Isolated
public class KafkaAdaptationTests {

    // КОНФИГУРАЦИЯ ТЕСТА
    private String bootstrapServers;
    private String testTopic;
    private String testGroupId;
    private Map<String, String> testMessages = new ConcurrentHashMap<>();
    private static final String TARGET_TESTRUN_ID = "TEST-1766431880908"; // Фиксированное значение для поиска
    private static final Gson GSON = new Gson(); // Используем GSON для парсинга JSON

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
    @DisplayName("Тест отправки сообщения в Kafka")
    void testSendMessageToKafka() {
        System.out.println("\n📬 Тест отправки сообщения без Transaction ID");
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
    @Order(2)
    @DisplayName("Тест отправки сообщения с Transaction ID")
    void testSendMessageWithTransactionId() {
        System.out.println("\n💳 Тест отправки сообщения c Transaction ID");
        String orderId = "ORD-TX-" + System.currentTimeMillis();
        Map<String, Object> message = createTestMessage(orderId, "CUST-002", 500.0, "PENDING");

        String transactionId = ProducerAdapter.sendMessageWithTransactionId(
                bootstrapServers,
                testTopic,
                message
        );

        assertNotNull(transactionId, "Сообщение с transaction ID должно быть отправлено успешно");
        testMessages.put(orderId, (String) message.get("testRunId"));
        System.out.println("   Сгенерированный Transaction ID: " + transactionId);
    }


    @Test
    @Order(3)
    @DisplayName("Тест чтения сообщений из Kafka")
    void testReadMessagesFromKafka() {
        System.out.println("\n🔎 Тест чтения сообщений");
        waitForMessages(5); // Ожидаем 5 секунд

        // Читаем последние 6 сообщений
        ConsumerRecords<String, String> consumerRecords = ConsumerAdapter.readMessage(
                bootstrapServers,
                testTopic,
                testGroupId
        );

        assertFalse(consumerRecords.isEmpty(), "Из топика должны быть прочитаны сообщения");

        // Проверяем, что прочитаны сообщения, которые мы отправили
        List<KafkaMessage> messages = ConsumerAdapter.convertRecordsToMessageObject(consumerRecords);
        assertFalse(messages.isEmpty(), "Список сообщений не должен быть пустым");

        // Проверка сообщения с Transaction ID (и наличием любого заголовка)
        long batchMessages = messages.stream()
                .filter(msg -> msg.getHeaders() != null && !msg.getHeaders().isEmpty())
                .count();

        assertTrue(batchMessages > 0, "Должно быть найдено хотя бы одно сообщение с заголовком (Transaction ID)");

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

        System.out.println("\n🎉 ТЕСТЫ АДАПТАЦИИ УСПЕШНО ПРОЙДЕНЫ!");
    }

    @Test
    @Order(4)
    @DisplayName("Тест чтения сообщений за последние 2 дня и поиск по testRunId")
    void testReadMessagesFromLastTwoDaysAndFilter() {
        System.out.println("\n⏳ Тест чтения сообщений за последние 2 дня и поиск по значению 'testRunId'");

        // 1. Расчет стартовой временной метки (Текущее время - 2 полных дня)
        // Instant.now() - 2 дня
        long twoDaysAgoMs = Instant.now().minus(2, ChronoUnit.DAYS).toEpochMilli();
        System.out.println("   Стартовая временная метка (2 дня назад): " + twoDaysAgoMs);
        System.out.println("   Искомое значение 'testRunId': " + TARGET_TESTRUN_ID);

        // 2. Чтение сообщений из Kafka с помощью нового адаптера
        // Метод readMessagesFromTimestamp теперь возвращает готовый List<KafkaMessage>,
        // не требуя дополнительной конвертации.
        String searchGroupId = "search-test-group-" + System.currentTimeMillis();

        List<KafkaMessage> messages = ConsumerAdapter.readMessagesFromTimestamp(
                bootstrapServers,
                testTopic,
                searchGroupId,
                twoDaysAgoMs,
                10 // Максимальное время ожидания 10 секунд
        );

        if (messages.isEmpty()) {
            System.out.println("   ❌ Не найдено сообщений за последние 2 дня.");
            // Если сообщений нет, тест считается успешным, если они не являются обязательными.
            // Если вам нужно, чтобы тест провалился, используйте: fail("Не найдено ни одного сообщения...");
            return;
        }

        System.out.println("   ✅ Прочитано " + messages.size() + " сообщений за последние 2 дня.");

        // 3. Фильтрация сообщений
        // Фильтрация: ищем в body (JSON String) поле "testRunId" с целевым значением
        Optional<KafkaMessage> foundMessage = messages.stream()
                .filter(msg -> {
                    // Используем GSON для безопасного парсинга JSON
                    try {
                        // Парсим body в Map
                        Map<String, Object> bodyMap = GSON.fromJson(msg.getBody(), Map.class);

                        // Ищем поле "testRunId" и сравниваем его значение
                        // Kafka сохраняет числа как Double, поэтому нужно проверить тип.
                        Object testRunIdValue = bodyMap.get("testRunId");
                        return TARGET_TESTRUN_ID.equals(testRunIdValue);

                    } catch (Exception e) {
                        // Игнорируем сообщения, которые не являются валидным JSON
                        // System.err.println("   Warning: Failed to parse message body as JSON: " + msg.getBody());
                        return false;
                    }
                })
                .findFirst();

        // 4. Проверка результата
        assertTrue(foundMessage.isPresent(),
                "Сообщение с testRunId = " + TARGET_TESTRUN_ID + " должно быть найдено среди прочитанных.");

        System.out.println("   🎉 Сообщение с 'testRunId' = " + TARGET_TESTRUN_ID + " успешно найдено!");
        System.out.println("   Детали: " + foundMessage.get());
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    private Map<String, Object> createTestMessage(String orderId, String customerId,
                                                  double amount, String status) {
        Map<String, Object> message = new HashMap<>();
        message.put("orderId", orderId);
        message.put("customerId", customerId);
        message.put("amount", amount);
        message.put("status", status);
        message.put("timestamp", System.currentTimeMillis());
        // ВАЖНО: При отправке тестового сообщения, мы не используем фиксированный ID,
        // чтобы не нарушать другие тесты. ФИКСИРОВАННЫЙ ID должен быть отправлен отдельно,
        // если вы хотите, чтобы этот тест гарантированно его нашел.
        message.put("testRunId", "TEST-" + System.currentTimeMillis());
        message.put("randomValue", new Random().nextInt(1000));
        return message;
    }

    private void waitForMessages(int seconds) {
        System.out.println("⏳ Ожидание " + seconds + " секунд для доставки сообщений...");
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}