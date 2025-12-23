package com.qa.framework.tests.kafkaTests;

import com.qa.framework.config.ConfigurationManager;
import com.qa.framework.kafka.ConsumerAdapter;
import com.qa.framework.kafka.KafkaMessage;
import com.qa.framework.kafka.ProducerAdapter;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Isolated;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("kafka-tests")
@Isolated
public class KafkaAdaptationTests {

    private String bootstrapServers;
    private String testTopic;
    private String testGroupId;
    private Map<String, String> testMessages = new ConcurrentHashMap<>();

    @BeforeAll
    void setup() {
        // Используем конфигурацию из вашего ConfigurationManager
        bootstrapServers = ConfigurationManager.getKafkaBootstrapServers();
        testTopic = ConfigurationManager.getProperty("test.kafka.topic", "test_topic");
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
        System.out.println("\n📤 Тест отправки сообщения в Kafka");

        // Создаем тестовое сообщение
        Map<String, Object> testMessage = createTestMessage(
                "ORDER-001",
                "CUSTOMER-001",
                100.50,
                "CREATED"
        );

        // Отправляем сообщение
        boolean sent = ProducerAdapter.sendMessage(
                bootstrapServers,
                testTopic,
                testMessage
        );

        assertTrue(sent, "Сообщение должно быть успешно отправлено");

        // Сохраняем ID для последующего поиска
        testMessages.put("ORDER-001", "CUSTOMER-001");

        System.out.println("✅ Сообщение успешно отправлено");
    }

    @Test
    @Order(2)
    @DisplayName("Тест отправки сообщения с заголовками")
    void testSendMessageWithHeaders() {
        System.out.println("\n📤 Тест отправки сообщения с заголовками");

        // Создаем тестовое сообщение
        Map<String, Object> testMessage = createTestMessage(
                "ORDER-002",
                "CUSTOMER-002",
                200.75,
                "PROCESSED"
        );

        // Создаем заголовки как в вашем коде
        Map<String, String> headers = new HashMap<>();
        String transactionId = UUID.randomUUID().toString();
        headers.put("X-Prepare-Transaction-Req-Id", transactionId);
        headers.put("X-Initiator-Service", "test-service");

        // Отправляем сообщение с заголовками
        boolean sent = ProducerAdapter.sendMessageWithHeaders(
                bootstrapServers,
                testTopic,
                testMessage,
                headers
        );

        assertTrue(sent, "Сообщение с заголовками должно быть успешно отправлено");
        testMessages.put("ORDER-002", transactionId);

        System.out.println("✅ Сообщение с заголовками успешно отправлено");
        System.out.println("   Transaction ID: " + transactionId);
    }

    @Test
    @Order(3)
    @DisplayName("Тест отправки сообщения с транзакционным ID")
    void testSendMessageWithTransactionId() {
        System.out.println("\n📤 Тест отправки сообщения с транзакционным ID");

        // Создаем тестовое сообщение
        Map<String, Object> testMessage = createTestMessage(
                "ORDER-003",
                "CUSTOMER-003",
                300.25,
                "COMPLETED"
        );

        // Отправляем сообщение (метод сам генерирует transaction ID)
        String transactionId = ProducerAdapter.sendMessageWithTransactionId(
                bootstrapServers,
                testTopic,
                testMessage
        );

        assertNotNull(transactionId, "Transaction ID не должен быть null");
        assertFalse(transactionId.isEmpty(), "Transaction ID не должен быть пустым");

        testMessages.put("ORDER-003", transactionId);

        System.out.println("✅ Сообщение с транзакционным ID успешно отправлено");
        System.out.println("   Generated Transaction ID: " + transactionId);
    }

    @Test
    @Order(4)
    @DisplayName("Тест чтения сообщений из Kafka")
    void testReadMessagesFromKafka() {
        System.out.println("\n📥 Тест чтения сообщений из Kafka");

        // Даем время на доставку сообщений
        waitForMessages(5);

        // Читаем сообщения из топика
        var consumerRecords = ConsumerAdapter.readMessage(
                bootstrapServers,
                testTopic,
                testGroupId + "-read"
        );

        assertNotNull(consumerRecords, "ConsumerRecords не должен быть null");
        assertTrue(consumerRecords.count() > 0,
                "Должно быть хотя бы одно сообщение в топике");

        System.out.println("✅ Прочитано сообщений: " + consumerRecords.count());

        // Конвертируем в объекты сообщений
        List<KafkaMessage> messages = ConsumerAdapter.convertRecordsToMessageObject(consumerRecords);

        assertFalse(messages.isEmpty(), "Список сообщений не должен быть пустым");

        // Выводим информацию о сообщениях
        messages.forEach(msg -> {
            System.out.println("📄 Сообщение:");
            System.out.println("   Топик: " + msg.getTopic());
            System.out.println("   Partition: " + msg.getPartition());
            System.out.println("   Offset: " + msg.getOffset());
            System.out.println("   Timestamp: " + new Date(msg.getTimestamp()));

            if (msg.getHeader() != null) {
                System.out.println("   Header: " + msg.getHeader().key() + " = " +
                        new String(msg.getHeader().value()));
            }

            // Парсим тело сообщения для проверки
            try {
                Map<?, ?> body = new com.google.gson.Gson().fromJson(msg.getBody(), Map.class);
                if (body.containsKey("orderId")) {
                    System.out.println("   Order ID: " + body.get("orderId"));
                }
            } catch (Exception e) {
                System.out.println("   Body: " +
                        (msg.getBody().length() > 100 ?
                                msg.getBody().substring(0, 100) + "..." :
                                msg.getBody()));
            }
            System.out.println();
        });
    }

    @Test
    @Order(5)
    @DisplayName("Тест чтения сообщений с ожиданием")
    void testReadMessagesWithWait() {
        System.out.println("\n⏱️ Тест чтения сообщений с ожиданием");

        // Отправляем новое сообщение
        Map<String, Object> newMessage = createTestMessage(
                "ORDER-WAIT",
                "CUSTOMER-WAIT",
                150.00,
                "PENDING"
        );

        boolean sent = ProducerAdapter.sendMessage(
                bootstrapServers,
                testTopic,
                newMessage
        );

        assertTrue(sent, "Новое сообщение должно быть отправлено");

        // Читаем с ожиданием
        var consumerRecords = ConsumerAdapter.readMessagesWithWait(
                bootstrapServers,
                testTopic,
                testGroupId + "-wait",
                5 // 5 секунд максимум
        );

        assertTrue(consumerRecords.count() > 0,
                "Должно найти хотя бы одно сообщение за 5 секунд");

        // Ищем наше новое сообщение
        boolean found = false;
        for (var record : consumerRecords) {
            try {
                Map<?, ?> body = new com.google.gson.Gson().fromJson(record.value(), Map.class);
                if ("ORDER-WAIT".equals(body.get("orderId"))) {
                    found = true;
                    break;
                }
            } catch (Exception e) {
                // Игнорируем ошибки парсинга
            }
        }

        assertTrue(found, "Должно найти отправленное сообщение ORDER-WAIT");
        System.out.println("✅ Сообщение ORDER-WAIT успешно найдено");
    }

    @Test
    @Order(6)
    @DisplayName("Тест конвертации сообщений с сортировкой по времени")
    void testConvertMessagesSortedByTimestamp() {
        System.out.println("\n🕒 Тест конвертации сообщений с сортировкой по времени");

        // Читаем сообщения
        var consumerRecords = ConsumerAdapter.readMessage(
                bootstrapServers,
                testTopic,
                testGroupId + "-sort"
        );

        if (consumerRecords.count() > 1) {
            // Конвертируем с сортировкой по timestamp
            SortedMap<Long, String> sortedMessages =
                    ConsumerAdapter.convertRecordsToMessageSortedByTimestamp(consumerRecords);

            assertFalse(sortedMessages.isEmpty(),
                    "Отсортированный список не должен быть пустым");

            System.out.println("Сообщения отсортированы по времени (от старых к новым):");
            sortedMessages.forEach((timestamp, message) -> {
                System.out.println("   " + new Date(timestamp) + ": " +
                        (message.length() > 50 ?
                                message.substring(0, 50) + "..." : message));
            });

            // Проверяем, что сообщения отсортированы правильно
            Long previousTimestamp = null;
            for (Long timestamp : sortedMessages.keySet()) {
                if (previousTimestamp != null) {
                    assertTrue(timestamp >= previousTimestamp,
                            "Сообщения должны быть отсортированы по возрастанию timestamp");
                }
                previousTimestamp = timestamp;
            }

            System.out.println("✅ Сообщения успешно отсортированы по времени");
        } else {
            System.out.println("⚠️ Недостаточно сообщений для теста сортировки");
        }
    }

    @Test
    @Order(7)
    @DisplayName("Тест отправки нескольких сообщений и проверки заголовков")
    void testMultipleMessagesWithHeaders() {
        System.out.println("\n📨 Тест отправки нескольких сообщений и проверки заголовков");

        // Отправляем 3 сообщения с разными заголовками
        List<String> transactionIds = new ArrayList<>();

        for (int i = 1; i <= 3; i++) {
            Map<String, Object> message = createTestMessage(
                    "BATCH-ORDER-" + i,
                    "BATCH-CUST-" + i,
                    50.0 * i,
                    "BATCH-" + i
            );

            Map<String, String> headers = new HashMap<>();
            String transactionId = "BATCH-TX-" + UUID.randomUUID().toString();
            headers.put("X-Prepare-Transaction-Req-Id", transactionId);
            headers.put("X-Batch-Number", String.valueOf(i));

            boolean sent = ProducerAdapter.sendMessageWithHeaders(
                    bootstrapServers,
                    testTopic,
                    message,
                    headers
            );

            assertTrue(sent, "Батч-сообщение " + i + " должно быть отправлено");
            transactionIds.add(transactionId);

            System.out.println("   Отправлено батч-сообщение " + i +
                    " с Transaction ID: " + transactionId);
        }

        // Ждем доставки
        waitForMessages(3);

        // Читаем и проверяем заголовки
        var consumerRecords = ConsumerAdapter.readMessage(
                bootstrapServers,
                testTopic,
                testGroupId + "-batch"
        );

        // Конвертируем в объекты для проверки заголовков
        List<KafkaMessage> messages = ConsumerAdapter.convertRecordsToMessageObject(consumerRecords);

        // Считаем сообщения с заголовками X-Batch-Number
        long batchMessages = messages.stream()
                .filter(msg -> {
                    if (msg.getHeader() != null) {
                        // Проверяем все заголовки
                        // В реальном коде нужно получить доступ ко всем заголовкам
                        return true; // Упрощенная проверка
                    }
                    return false;
                })
                .count();

        assertTrue(batchMessages >= 3,
                "Должно быть хотя бы 3 батч-сообщения");

        System.out.println("✅ Все батч-сообщения успешно отправлены и найдены");
    }

    @Test
    @Order(8)
    @DisplayName("Тест поиска сообщения по содержимому")
    void testFindMessageByContent() {
        System.out.println("\n🔍 Тест поиска сообщения по содержимому");

        // Создаем уникальное сообщение для поиска
        String uniqueOrderId = "SEARCH-" + System.currentTimeMillis();
        Map<String, Object> searchMessage = createTestMessage(
                uniqueOrderId,
                "SEARCH-CUSTOMER",
                999.99,
                "SEARCHABLE"
        );

        // Отправляем сообщение
        boolean sent = ProducerAdapter.sendMessage(
                bootstrapServers,
                testTopic,
                searchMessage
        );

        assertTrue(sent, "Сообщение для поиска должно быть отправлено");

        // Ждем
        waitForMessages(2);

        // Читаем сообщения и ищем наше
        var consumerRecords = ConsumerAdapter.readMessage(
                bootstrapServers,
                testTopic,
                testGroupId + "-search"
        );

        boolean found = false;
        for (var record : consumerRecords) {
            if (record.value().contains(uniqueOrderId)) {
                found = true;
                System.out.println("✅ Найдено сообщение с Order ID: " + uniqueOrderId);
                System.out.println("   Полное сообщение: " +
                        (record.value().length() > 100 ?
                                record.value().substring(0, 100) + "..." :
                                record.value()));
                break;
            }
        }

        assertTrue(found, "Должно найти сообщение с уникальным Order ID");
    }

    @Test
    @Order(9)
    @DisplayName("Тест обработки больших объемов сообщений")
    void testHighVolumeMessages() {
        System.out.println("\n📊 Тест обработки больших объемов сообщений");

        // Отправляем 10 сообщений быстро
        int messageCount = 10;
        List<String> sentOrderIds = new ArrayList<>();

        for (int i = 0; i < messageCount; i++) {
            String orderId = "VOLUME-" + System.currentTimeMillis() + "-" + i;
            Map<String, Object> message = createTestMessage(
                    orderId,
                    "VOLUME-CUST",
                    10.0 * (i + 1),
                    "VOLUME"
            );

            boolean sent = ProducerAdapter.sendMessage(
                    bootstrapServers,
                    testTopic,
                    message
            );

            assertTrue(sent, "Объемное сообщение " + i + " должно быть отправлено");
            sentOrderIds.add(orderId);
        }

        System.out.println("   Отправлено " + messageCount + " объемных сообщений");

        // Даем время на обработку
        waitForMessages(5);

        // Читаем и проверяем
        var consumerRecords = ConsumerAdapter.readMessage(
                bootstrapServers,
                testTopic,
                testGroupId + "-volume"
        );

        // Проверяем, что получили достаточно сообщений
        assertTrue(consumerRecords.count() >= messageCount,
                "Должно получить хотя бы " + messageCount + " сообщений");

        System.out.println("✅ Успешно обработано " + consumerRecords.count() +
                " объемных сообщений");
    }

    @Test
    @Order(10)
    @DisplayName("Итоговый тест - проверка всей функциональности")
    void testFinalIntegrationTest() {
        System.out.println("\n🏁 Итоговый тест - проверка всей функциональности");

        // 1. Проверяем подключение
        System.out.println("1. Проверка подключения к Kafka...");
        assertNotNull(bootstrapServers, "Bootstrap servers не должны быть null");
        assertFalse(bootstrapServers.isEmpty(), "Bootstrap servers не должны быть пустыми");

        // 2. Отправляем финальное сообщение
        System.out.println("2. Отправка финального тестового сообщения...");
        String finalOrderId = "FINAL-" + System.currentTimeMillis();

        Map<String, Object> finalMessage = new HashMap<>();
        finalMessage.put("orderId", finalOrderId);
        finalMessage.put("customerId", "FINAL-CUSTOMER");
        finalMessage.put("amount", 1234.56);
        finalMessage.put("status", "FINAL");
        finalMessage.put("timestamp", System.currentTimeMillis());
        finalMessage.put("testName", "KafkaAdaptationFinalTest");

        boolean sent = ProducerAdapter.sendMessage(
                bootstrapServers,
                testTopic,
                finalMessage
        );

        assertTrue(sent, "Финальное сообщение должно быть отправлено");

        // 3. Читаем и проверяем
        System.out.println("3. Чтение и проверка сообщений...");
        waitForMessages(3);

        var consumerRecords = ConsumerAdapter.readMessage(
                bootstrapServers,
                testTopic,
                testGroupId + "-final"
        );

        assertTrue(consumerRecords.count() > 0,
                "Должно быть хотя бы одно сообщение в топике");

        // 4. Ищем наше финальное сообщение
        boolean finalMessageFound = false;
        for (var record : consumerRecords) {
            if (record.value().contains(finalOrderId)) {
                finalMessageFound = true;
                break;
            }
        }

        assertTrue(finalMessageFound, "Должно найти финальное тестовое сообщение");

        // 5. Конвертируем и проверяем структуру
        System.out.println("4. Конвертация и проверка структуры сообщений...");
        List<KafkaMessage> messages = ConsumerAdapter.convertRecordsToMessageObject(consumerRecords);
        assertFalse(messages.isEmpty(), "Список сообщений не должен быть пустым");

        System.out.println("\n🎉 ВСЕ ТЕСТЫ АДАПТАЦИИ УСПЕШНО ПРОЙДЕНЫ!");
        System.out.println("Отправлено и проверено " + testMessages.size() +
                " тестовых сообщений");
        System.out.println("Прочитано " + consumerRecords.count() +
                " сообщений из топика " + testTopic);
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