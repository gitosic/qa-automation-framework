package com.qa.framework.kafka;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class KafkaUtils {

    private static final com.google.gson.Gson GSON = new com.google.gson.Gson();

    /**
     * Поиск сообщения по testRunId за последние N минут
     */
    public static Optional<KafkaMessage> findMessageByTestRunId(
            String bootstrapServers,
            String topicName,
            String testRunId,
            int lastMinutes) {

        System.out.println("\n🔍 Поиск сообщения с testRunId = " + testRunId);
        System.out.println("   За последние " + lastMinutes + " минут");
        System.out.println("   Топик: " + topicName);

        // Создаем уникальный groupId для поиска
        String groupId = "search-testrunid-" + System.currentTimeMillis();

        // Читаем сообщения за последние N минут
        List<KafkaMessage> messages = ConsumerAdapter.readMessagesFromLastMinutes(
                bootstrapServers,
                topicName,
                groupId,
                lastMinutes,
                10 // 10 секунд максимальное ожидание
        );

        if (messages.isEmpty()) {
            System.out.println("❌ Сообщений не найдено за последние " + lastMinutes + " минут");
            return Optional.empty();
        }

        System.out.println("✅ Найдено " + messages.size() + " сообщений для анализа");

        // Ищем сообщение с нужным testRunId
        return messages.stream()
                .filter(msg -> hasTestRunId(msg, testRunId))
                .findFirst();
    }

    /**
     * Проверяет, содержит ли сообщение нужный testRunId
     */
    private static boolean hasTestRunId(KafkaMessage message, String expectedTestRunId) {
        try {
            Map<String, Object> bodyMap = GSON.fromJson(message.getBody(), Map.class);
            Object actualTestRunId = bodyMap.get("testRunId");
            return expectedTestRunId.equals(actualTestRunId);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Поиск всех сообщений с заданным testRunId
     */
    public static List<KafkaMessage> findAllMessagesByTestRunId(
            String bootstrapServers,
            String topicName,
            String testRunId,
            int lastMinutes) {

        String groupId = "find-all-testrunid-" + System.currentTimeMillis();

        List<KafkaMessage> messages = ConsumerAdapter.readMessagesFromLastMinutes(
                bootstrapServers,
                topicName,
                groupId,
                lastMinutes,
                10
        );

        return messages.stream()
                .filter(msg -> hasTestRunId(msg, testRunId))
                .collect(Collectors.toList());
    }

    /**
     * Поиск сообщения по любому JSON полю
     */
    public static Optional<KafkaMessage> findMessageByField(
            String bootstrapServers,
            String topicName,
            String fieldName,
            String fieldValue,
            int lastMinutes) {

        String groupId = "search-field-" + System.currentTimeMillis();

        List<KafkaMessage> messages = ConsumerAdapter.readMessagesFromLastMinutes(
                bootstrapServers,
                topicName,
                groupId,
                lastMinutes,
                10
        );

        return messages.stream()
                .filter(msg -> {
                    try {
                        Map<String, Object> bodyMap = GSON.fromJson(msg.getBody(), Map.class);
                        Object value = bodyMap.get(fieldName);
                        return fieldValue.equals(value != null ? value.toString() : null);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .findFirst();
    }

    /**
     * Поиск сообщения по предикату (условию)
     */
    public static Optional<KafkaMessage> findMessageByCondition(
            String bootstrapServers,
            String topicName,
            Predicate<String> condition,
            int lastMinutes) {

        String groupId = "search-condition-" + System.currentTimeMillis();

        List<KafkaMessage> messages = ConsumerAdapter.readMessagesFromLastMinutes(
                bootstrapServers,
                topicName,
                groupId,
                lastMinutes,
                10
        );

        return messages.stream()
                .filter(msg -> condition.test(msg.getBody()))
                .findFirst();
    }

    /**
     * Получение информации о найденном сообщении в читаемом формате
     */
    public static void printMessageDetails(KafkaMessage message) {
        if (message == null) {
            System.out.println("❌ Сообщение не найдено");
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(TimeZone.getDefault().toZoneId());

        String messageTime = formatter.format(Instant.ofEpochMilli(message.getTimestamp()));

        System.out.println("\n📄 НАЙДЕНО СООБЩЕНИЕ:");
        System.out.println("========================");
        System.out.println("Топик:       " + message.getTopic());
        System.out.println("Partition:   " + message.getPartition());
        System.out.println("Offset:      " + message.getOffset());
        System.out.println("Время:       " + messageTime);
        System.out.println("Заголовков:  " + (message.getHeaders() != null ? message.getHeaders().size() : 0));

        try {
            // Пытаемся красиво отформатировать JSON
            Object jsonObject = GSON.fromJson(message.getBody(), Object.class);
            String prettyJson = GSON.toJson(jsonObject);
            System.out.println("Тело сообщения:");
            System.out.println(prettyJson);
        } catch (Exception e) {
            System.out.println("Тело сообщения (сырой JSON):");
            System.out.println(message.getBody());
        }
        System.out.println("========================");
    }

    /**
     * Проверка существования сообщения с заданным testRunId
     */
    public static boolean hasMessageWithTestRunId(
            String bootstrapServers,
            String topicName,
            String testRunId,
            int lastMinutes) {

        return findMessageByTestRunId(bootstrapServers, topicName, testRunId, lastMinutes)
                .isPresent();
    }

    /**
     * Получение временного диапазона сообщений в топике
     */
    public static Map<String, Object> getTopicTimeRange(
            String bootstrapServers,
            String topicName) {

        String groupId = "time-range-" + System.currentTimeMillis();

        // Читаем все сообщения за последние 24 часа
        List<KafkaMessage> messages = ConsumerAdapter.readMessagesFromLastMinutes(
                bootstrapServers,
                topicName,
                groupId,
                24 * 60, // 24 часа
                5
        );

        Map<String, Object> result = new HashMap<>();

        if (messages.isEmpty()) {
            result.put("hasMessages", false);
            return result;
        }

        long earliest = messages.stream()
                .mapToLong(KafkaMessage::getTimestamp)
                .min()
                .orElse(0);

        long latest = messages.stream()
                .mapToLong(KafkaMessage::getTimestamp)
                .max()
                .orElse(0);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(TimeZone.getDefault().toZoneId());

        result.put("hasMessages", true);
        result.put("messageCount", messages.size());
        result.put("earliestTimestamp", earliest);
        result.put("latestTimestamp", latest);
        result.put("earliestTime", formatter.format(Instant.ofEpochMilli(earliest)));
        result.put("latestTime", formatter.format(Instant.ofEpochMilli(latest)));
        result.put("timeRangeMinutes", (latest - earliest) / (60 * 1000));

        return result;
    }

    private void waitForMessages(int seconds) {
        System.out.println("⏳ Ожидание " + seconds + " секунд для доставки сообщений...");
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static Map<String, Object> createTestMessage(String orderId, String customerId,
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


}