package com.qa.framework.tests.KafkaTests;

import com.qa.framework.kafka.KafkaUtils;
import org.junit.jupiter.api.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class KafkaIntegrationTests {

    private static final KafkaUtils kafkaUtils = new KafkaUtils();
    private static String uniqueOrderId;
    private static Map<String, Object> testOrder;

    @BeforeAll
    static void setup() {
        // Генерируем уникальный ID для всех тестов
        uniqueOrderId = "TEST-ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);

        // Создаем тестовое сообщение
        testOrder = new HashMap<>();
        testOrder.put("orderId", uniqueOrderId);
        testOrder.put("customerId", "TEST-CUST-001");
        testOrder.put("amount", 999.99);
        testOrder.put("status", "CREATED");
        testOrder.put("timestamp", System.currentTimeMillis());
        testOrder.put("testGroup", "KAFKA_INTEGRATION_TEST");

        System.out.println("🚀 Настройка тестов Kafka");
        System.out.println("Unique Order ID: " + uniqueOrderId);
    }

    @AfterAll
    static void cleanup() {
        System.out.println("\n🧹 Очистка завершена");
    }

    @Test
    @Order(1)
    @DisplayName("Тест подключения к Kafka")
    void testKafkaConnection() {
        System.out.println("\n🔌 Тест подключения к Kafka");

        boolean isConnected = kafkaUtils.testConnection();
        assertTrue(isConnected, "Подключение к Kafka должно быть успешным");

        System.out.println("✅ Подключение к Kafka успешно");
    }

    @Test
    @Order(2)
    @DisplayName("Отправка тестового сообщения в Kafka")
    void testSendMessageToKafka() {
        System.out.println("\n📤 Тест отправки сообщения в Kafka");
        System.out.println("Отправляем Order ID: " + uniqueOrderId);

        boolean sent = kafkaUtils.sendToIncomingOrders(uniqueOrderId, testOrder);

        assertTrue(sent, "Сообщение должно быть успешно отправлено в Kafka");

        // Небольшая пауза для гарантии доставки
        try {
            Thread.sleep(2000);
            System.out.println("⏳ Ожидание 2 секунды для гарантии доставки...");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("✅ Сообщение успешно отправлено");
    }

    @Test
    @Order(3)
    @DisplayName("Поиск по JSONPath в последних 5 сообщениях")
    void testFindByJsonPathInLast5Records() {
        System.out.println("\n🔍 Тест поиска по JSONPath в последних 5 сообщениях");
        System.out.println("Ищем Order ID: " + uniqueOrderId);

        List<String> foundMessages = kafkaUtils.findMessagesByJsonPathInLastRecords(
                "incoming_orders",
                "$.orderId",
                uniqueOrderId,
                5  // Ищем в последних 5 сообщениях
        );

        System.out.println("Найдено сообщений: " + foundMessages.size());

        assertFalse(foundMessages.isEmpty(),
                "Должно найти хотя бы одно сообщение в последних 5 записях");

        // Выводим найденные сообщения
        foundMessages.forEach(msg -> {
            System.out.println("✅ Найдено: " + msg.substring(0, Math.min(msg.length(), 100)) + "...");
        });

        assertEquals(1, foundMessages.size(),
                "Должно найти ровно одно сообщение с нашим уникальным Order ID");

        System.out.println("✅ Поиск в последних 5 сообщениях успешен");
    }

    @Test
    @Order(4)
    @DisplayName("Поиск по JSONPath за последнюю 1 минуту")
    void testFindByJsonPathInLast1Minute() {
        System.out.println("\n⏱️ Тест поиска по JSONPath за последнюю 1 минуту");
        System.out.println("Ищем Order ID: " + uniqueOrderId);

        List<String> foundMessages = kafkaUtils.findMessagesByJsonPathInLastMinutes(
                "incoming_orders",
                "$.orderId",
                uniqueOrderId,
                1  // Ищем за последнюю 1 минуту
        );

        System.out.println("Найдено сообщений за 1 минуту: " + foundMessages.size());

        assertFalse(foundMessages.isEmpty(),
                "Должно найти хотя бы одно сообщение за последнюю 1 минуту");

        foundMessages.forEach(msg -> {
            System.out.println("✅ Найдено за 1 минуту: " + msg.substring(0, Math.min(msg.length(), 100)) + "...");
        });

        System.out.println("✅ Поиск за 1 минуту успешен");
    }

    @Test
    @Order(5)
    @DisplayName("Расширенный поиск с извлечением данных")
    void testExtendedSearchWithExtraction() {
        System.out.println("\n🔍 Тест расширенного поиска с извлечением данных");

        List<Map<String, Object>> results = kafkaUtils.findAndExtractByJsonPath(
                "incoming_orders",
                "$.orderId",
                uniqueOrderId,
                5  // Ищем в 5 сообщениях
        );

        System.out.println("Извлечено результатов: " + results.size());

        assertFalse(results.isEmpty(),
                "Должен извлечь хотя бы один результат");

        // Проверяем структуру извлеченных данных
        results.forEach(result -> {
            assertNotNull(result.get("message"), "Сообщение не должно быть null");
            assertNotNull(result.get("topic"), "Топик не должен быть null");
            assertNotNull(result.get("partition"), "Partition не должен быть null");
            assertNotNull(result.get("offset"), "Offset не должен быть null");
            assertNotNull(result.get("key"), "Ключ не должен быть null");
            assertNotNull(result.get("foundValue"), "Найденное значение не должно быть null");

            System.out.println("\n📊 Детали найденного сообщения:");
            System.out.println("Топик: " + result.get("topic"));
            System.out.println("Partition: " + result.get("partition"));
            System.out.println("Offset: " + result.get("offset"));
            System.out.println("Ключ: " + result.get("key"));
            System.out.println("Найденное значение: " + result.get("foundValue"));
            System.out.println("Timestamp: " + result.get("timestamp"));

            // Проверяем, что найденное значение совпадает с нашим Order ID
            assertEquals(uniqueOrderId, result.get("foundValue").toString(),
                    "Найденное значение должно совпадать с отправленным Order ID");
        });

        System.out.println("✅ Расширенный поиск успешен");
    }

    @Test
    @Order(6)
    @DisplayName("Поиск по другим полям JSON в последних 5 сообщениях")
    void testSearchByOtherFields() {
        System.out.println("\n🔍 Тест поиска по другим полям JSON");

        // Поиск по customerId
        System.out.println("Поиск по customerId...");
        List<String> foundByCustomer = kafkaUtils.findMessagesByJsonPathInLastRecords(
                "incoming_orders",
                "$.customerId",
                "TEST-CUST-001",
                5
        );

        System.out.println("Найдено сообщений с customerId TEST-CUST-001: " + foundByCustomer.size());
        assertFalse(foundByCustomer.isEmpty(),
                "Должно найти хотя бы одно сообщение по customerId");

        // Поиск по status
        System.out.println("\nПоиск по status...");
        List<String> foundByStatus = kafkaUtils.findMessagesByJsonPathInLastRecords(
                "incoming_orders",
                "$.status",
                "CREATED",
                5
        );

        System.out.println("Найдено сообщений со статусом CREATED: " + foundByStatus.size());
        assertFalse(foundByStatus.isEmpty(),
                "Должно найти хотя бы одно сообщение по статусу");

        // Поиск по testGroup (дополнительное поле)
        System.out.println("\nПоиск по testGroup...");
        List<String> foundByTestGroup = kafkaUtils.findMessagesByJsonPathInLastRecords(
                "incoming_orders",
                "$.testGroup",
                "KAFKA_INTEGRATION_TEST",
                5
        );

        System.out.println("Найдено сообщений с testGroup KAFKA_INTEGRATION_TEST: " + foundByTestGroup.size());
        assertFalse(foundByTestGroup.isEmpty(),
                "Должно найти хотя бы одно сообщение по testGroup");

        System.out.println("✅ Поиск по другим полям успешен");
    }

    @Test
    @Order(7)
    @DisplayName("Получение информации о топике")
    void testGetTopicInfo() {
        System.out.println("\n📊 Тест получения информации о топике");

        // Теперь метод существует
        kafkaUtils.printTopicInfo("incoming_orders");

        long messageCount = kafkaUtils.countMessagesInTopic("incoming_orders");
        System.out.println("Всего сообщений в топике incoming_orders: " + messageCount);

        assertTrue(messageCount >= 1,
                "В топике должно быть хотя бы одно сообщение");

        System.out.println("✅ Получение информации о топике успешно");
    }

    @Test
    @Order(8)
    @DisplayName("Тест отправки нескольких сообщений и поиска")
    void testMultipleMessagesAndSearch() {
        System.out.println("\n📨 Тест отправки нескольких сообщений и поиска");

        // Отправляем 3 тестовых сообщения
        for (int i = 1; i <= 3; i++) {
            Map<String, Object> multiMessage = new HashMap<>();
            String multiOrderId = "MULTI-ORD-" + System.currentTimeMillis() + "-" + i;

            multiMessage.put("orderId", multiOrderId);
            multiMessage.put("customerId", "MULTI-CUST-" + i);
            multiMessage.put("amount", 100.0 * i);
            multiMessage.put("status", i % 2 == 0 ? "PROCESSED" : "PENDING");
            multiMessage.put("sequence", i);
            multiMessage.put("timestamp", System.currentTimeMillis());

            boolean sent = kafkaUtils.sendToIncomingOrders(multiOrderId, multiMessage);
            assertTrue(sent, "Сообщение " + i + " должно быть успешно отправлено");

            System.out.println("Отправлено сообщение " + i + ": " + multiOrderId);

            // Небольшая пауза между сообщениями
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Ждем доставки всех сообщений
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Ищем все MULTI сообщения за последнюю 1 минуту
        System.out.println("\nПоиск MULTI сообщений за 1 минуту...");
        List<String> foundMultiMessages = kafkaUtils.findMessagesByJsonPathInLastMinutes(
                "incoming_orders",
                "$.orderId",
                "MULTI-ORD-",  // Ищем все начинающиеся с MULTI-ORD-
                1
        );

        // Фильтруем по префиксу (JsonPath не поддерживает LIKE, только точное совпадение)
        List<String> filteredMultiMessages = foundMultiMessages.stream()
                .filter(msg -> msg.contains("MULTI-ORD-"))
                .toList();

        System.out.println("Найдено MULTI сообщений: " + filteredMultiMessages.size());
        assertTrue(filteredMultiMessages.size() >= 3,
                "Должно найти хотя бы 3 MULTI сообщения");

        // Ищем в последних 10 сообщениях
        System.out.println("\nПоиск MULTI сообщений в последних 10 сообщениях...");
        List<String> last10Messages = kafkaUtils.readAllMessagesFromTopic("incoming_orders", 10);

        long multiCount = last10Messages.stream()
                .filter(msg -> msg.contains("MULTI-ORD-"))
                .count();

        System.out.println("MULTI сообщений в последних 10: " + multiCount);
        assertTrue(multiCount >= 3,
                "В последних 10 сообщениях должно быть хотя бы 3 MULTI сообщения");

        System.out.println("✅ Тест множественных сообщений успешен");
    }

    @Test
    @Order(9)
    @DisplayName("Негативный тест - поиск несуществующего сообщения")
    void testNegativeSearchForNonExistentMessage() {
        System.out.println("\n🚫 Негативный тест - поиск несуществующего сообщения");

        String nonExistentOrderId = "NON-EXISTENT-" + System.currentTimeMillis();

        // Ищем несуществующее сообщение в последних 5
        List<String> foundIn5 = kafkaUtils.findMessagesByJsonPathInLastRecords(
                "incoming_orders",
                "$.orderId",
                nonExistentOrderId,
                5
        );

        System.out.println("Найдено несуществующих в 5 сообщениях: " + foundIn5.size());
        assertTrue(foundIn5.isEmpty(),
                "Не должно найти несуществующее сообщение в последних 5");

        // Ищем несуществующее сообщение за 1 минуту
        List<String> foundIn1Min = kafkaUtils.findMessagesByJsonPathInLastMinutes(
                "incoming_orders",
                "$.orderId",
                nonExistentOrderId,
                1
        );

        System.out.println("Найдено несуществующих за 1 минуту: " + foundIn1Min.size());
        assertTrue(foundIn1Min.isEmpty(),
                "Не должно найти несуществующее сообщение за 1 минуту");

        System.out.println("✅ Негативный тест успешен - не найдено ложных совпадений");
    }

    @Test
    @Order(10)
    @DisplayName("Итоговый тест - проверка всех методов")
    void testFinalVerification() {
        System.out.println("\n🏁 Итоговый тест - проверка всех методов");

        // 1. Проверка подключения
        assertTrue(kafkaUtils.testConnection(), "Kafka должно быть доступно");

        // 2. Проверка, что наше тестовое сообщение все еще доступно
        List<String> finalCheck = kafkaUtils.findMessagesByJsonPathInLastRecords(
                "incoming_orders",
                "$.orderId",
                uniqueOrderId,
                5
        );

        System.out.println("Финальная проверка - найдено наших сообщений: " + finalCheck.size());
        assertFalse(finalCheck.isEmpty(),
                "Наше тестовое сообщение должно все еще быть доступно");

        // 3. Проверка количества сообщений в топике
        long totalMessages = kafkaUtils.countMessagesInTopic("incoming_orders");
        System.out.println("Всего сообщений в топике: " + totalMessages);
        assertTrue(totalMessages > 0, "В топике должно быть хотя бы одно сообщение");

        // 4. Получение последнего сообщения
        String latestMessage = kafkaUtils.getLatestMessage("incoming_orders");
        assertNotNull(latestMessage, "Последнее сообщение не должно быть null");
        System.out.println("Последнее сообщение (первые 100 символов): " +
                (latestMessage.length() > 100 ? latestMessage.substring(0, 100) + "..." : latestMessage));

        System.out.println("\n🎉 ВСЕ ТЕСТЫ УСПЕШНО ПРОЙДЕНЫ!");
        System.out.println("Отправленный Order ID: " + uniqueOrderId);
        System.out.println("Все проверки завершены успешно");
    }
}