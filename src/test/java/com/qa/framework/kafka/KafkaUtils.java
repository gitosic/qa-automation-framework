package com.qa.framework.kafka;

import com.qa.framework.config.KafkaConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class KafkaUtils {

    private final KafkaConfig kafkaConfig;
    private final ObjectMapper objectMapper;

    public KafkaUtils() {
        this.kafkaConfig = new KafkaConfig();
        this.objectMapper = new ObjectMapper();
    }

    // ========== МЕТОДЫ ОТПРАВКИ СООБЩЕНИЙ ==========

    /**
     * Отправляет JSON сообщение в указанный топик Kafka
     * @param topic Название топика
     * @param key Ключ сообщения
     * @param message Сообщение (объект или JSON строка)
     * @return true если сообщение успешно отправлено
     */
    public boolean sendJsonMessage(String topic, String key, Object message) {
        try (KafkaProducer<String, String> producer =
                     new KafkaProducer<>(kafkaConfig.getKafkaProducerProperties())) {

            String jsonMessage;
            if (message instanceof String) {
                jsonMessage = (String) message;
            } else {
                jsonMessage = objectMapper.writeValueAsString(message);
            }

            ProducerRecord<String, String> record =
                    new ProducerRecord<>(topic, key, jsonMessage);

            Future<RecordMetadata> future = producer.send(record);
            producer.flush();

            // Ждем подтверждения отправки
            RecordMetadata metadata = future.get(10, TimeUnit.SECONDS);

            System.out.println("✅ Message sent successfully to topic: " + metadata.topic() +
                    ", partition: " + metadata.partition() +
                    ", offset: " + metadata.offset());
            return true;

        } catch (Exception e) {
            System.err.println("❌ Failed to send message to Kafka topic " + topic + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Отправляет сообщение в топик incoming_orders
     */
    public boolean sendToIncomingOrders(String key, Object message) {
        return sendJsonMessage(kafkaConfig.getIncomingOrdersTopic(), key, message);
    }

    /**
     * Отправляет сообщение в топик user_activities
     */
    public boolean sendToUserActivities(String key, Object message) {
        return sendJsonMessage(kafkaConfig.getUserActivitiesTopic(), key, message);
    }

    /**
     * Отправляет сообщение в топик system_logs
     */
    public boolean sendToSystemLogs(String key, Object message) {
        return sendJsonMessage(kafkaConfig.getSystemLogsTopic(), key, message);
    }

    // ========== ОСНОВНЫЕ МЕТОДЫ ПОИСКА ==========

    /**
     * Поиск сообщений за последние N минут по заданному полю и значению
     */
    public List<String> findMessagesInLastMinutes(String topic, String searchField,
                                                  String searchValue, int minutes) {
        List<String> foundMessages = new ArrayList<>();

        Properties props = kafkaConfig.getKafkaConsumerProperties("search-group-" + UUID.randomUUID());

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(topic));

            long endTime = System.currentTimeMillis() + (minutes * 60 * 1000);

            while (System.currentTimeMillis() < endTime) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));

                records.forEach(record -> {
                    try {
                        JsonNode jsonNode = objectMapper.readTree(record.value());
                        JsonNode fieldNode = jsonNode.path(searchField);

                        if (!fieldNode.isMissingNode() &&
                                fieldNode.asText().equals(searchValue)) {
                            foundMessages.add(record.value());
                        }
                    } catch (Exception e) {
                        // Если сообщение не JSON, ищем строковое совпадение
                        if (record.value().contains(searchValue)) {
                            foundMessages.add(record.value());
                        }
                    }
                });
            }

        } catch (Exception e) {
            throw new RuntimeException("Error reading from Kafka topic: " + topic, e);
        }

        return foundMessages;
    }

    /**
     * Поиск в последних N сообщениях по заданному полю и значению
     */
    public List<String> findMessagesInLastRecords(String topic, String searchField,
                                                  String searchValue, int maxRecords) {
        List<String> foundMessages = new ArrayList<>();

        Properties props = kafkaConfig.getKafkaConsumerProperties("search-group-" + UUID.randomUUID(), maxRecords);

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(topic));

            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5));

            int count = 0;
            for (var record : records) {
                if (count >= maxRecords) break;

                try {
                    JsonNode jsonNode = objectMapper.readTree(record.value());
                    JsonNode fieldNode = jsonNode.path(searchField);

                    if (!fieldNode.isMissingNode() &&
                            fieldNode.asText().equals(searchValue)) {
                        foundMessages.add(record.value());
                    }
                } catch (Exception e) {
                    // Если сообщение не JSON, ищем строковое совпадение
                    if (record.value().contains(searchValue)) {
                        foundMessages.add(record.value());
                    }
                }

                count++;
            }

        } catch (Exception e) {
            throw new RuntimeException("Error reading from Kafka topic: " + topic, e);
        }

        return foundMessages;
    }

    // ========== МЕТОДЫ ПОИСКА С ИСПОЛЬЗОВАНИЕМ JSONPATH ==========

    /**
     * Поиск по JSONPath выражению в последние N минут
     */
    public List<String> findMessagesByJsonPathInLastMinutes(String topic, String jsonPath,
                                                            String expectedValue, int minutes) {
        List<String> foundMessages = new ArrayList<>();

        Properties props = kafkaConfig.getKafkaConsumerProperties("jsonpath-group-" + UUID.randomUUID());

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(topic));

            long endTime = System.currentTimeMillis() + (minutes * 60 * 1000);

            while (System.currentTimeMillis() < endTime) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));

                records.forEach(record -> {
                    try {
                        Object value = JsonPath.read(record.value(), jsonPath);

                        if (value != null && value.toString().equals(expectedValue)) {
                            foundMessages.add(record.value());
                        }
                    } catch (PathNotFoundException e) {
                        // Поле не найдено в JSON - пропускаем
                    } catch (Exception e) {
                        System.err.println("Error parsing JSON with JsonPath: " + e.getMessage());
                    }
                });
            }

        } catch (Exception e) {
            throw new RuntimeException("Error searching messages by JSONPath in topic: " + topic, e);
        }

        return foundMessages;
    }

    /**
     * Поиск по JSONPath выражению в последних N сообщениях
     */
    public List<String> findMessagesByJsonPathInLastRecords(String topic, String jsonPath,
                                                            String expectedValue, int maxRecords) {
        List<String> foundMessages = new ArrayList<>();

        Properties props = kafkaConfig.getKafkaConsumerProperties("jsonpath-group-" + UUID.randomUUID(), maxRecords);

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(topic));

            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5));

            int count = 0;
            for (var record : records) {
                if (count >= maxRecords) break;

                try {
                    Object value = JsonPath.read(record.value(), jsonPath);

                    if (value != null && value.toString().equals(expectedValue)) {
                        foundMessages.add(record.value());
                    }
                } catch (PathNotFoundException e) {
                    // Поле не найдено
                } catch (Exception e) {
                    System.err.println("Error parsing JSON: " + e.getMessage());
                }

                count++;
            }

        } catch (Exception e) {
            throw new RuntimeException("Error searching messages by JSONPath in topic: " + topic, e);
        }

        return foundMessages;
    }

    /**
     * Расширенный поиск по JSONPath с извлечением метаданных
     */
    public List<Map<String, Object>> findAndExtractByJsonPath(String topic, String jsonPath,
                                                              String expectedValue, int maxMessages) {
        List<Map<String, Object>> results = new ArrayList<>();

        Properties props = kafkaConfig.getKafkaConsumerProperties("extract-group-" + UUID.randomUUID());

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(topic));

            int messagesProcessed = 0;
            boolean keepReading = true;

            while (keepReading && messagesProcessed < maxMessages) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

                if (records.isEmpty()) {
                    keepReading = false;
                }

                for (var record : records) {
                    if (messagesProcessed >= maxMessages) break;

                    try {
                        Object value = JsonPath.read(record.value(), jsonPath);

                        if (value != null && value.toString().equals(expectedValue)) {
                            Map<String, Object> result = new HashMap<>();
                            result.put("message", record.value());
                            result.put("topic", record.topic());
                            result.put("partition", record.partition());
                            result.put("offset", record.offset());
                            result.put("key", record.key());
                            result.put("foundValue", value);
                            result.put("timestamp", record.timestamp());

                            // Парсим весь JSON для дополнительной информации
                            Map<String, Object> fullMessage = objectMapper.readValue(
                                    record.value(),
                                    new TypeReference<Map<String, Object>>() {}
                            );
                            result.put("fullData", fullMessage);

                            results.add(result);
                        }
                    } catch (PathNotFoundException e) {
                        // Поле не найдено
                    } catch (Exception e) {
                        System.err.println("Error processing message: " + e.getMessage());
                    }

                    messagesProcessed++;
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error extracting messages by JSONPath: " + topic, e);
        }

        return results;
    }

    // ========== ДОПОЛНИТЕЛЬНЫЕ УТИЛИТНЫЕ МЕТОДЫ ==========

    /**
     * Чтение всех сообщений из топика (максимальное количество)
     */
    public List<String> readAllMessagesFromTopic(String topic, int maxMessages) {
        List<String> messages = new ArrayList<>();

        Properties props = kafkaConfig.getKafkaConsumerProperties("read-all-group-" + UUID.randomUUID());
        props.put("auto.offset.reset", "earliest");

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(topic));

            int count = 0;
            boolean hasMoreMessages = true;

            while (hasMoreMessages && count < maxMessages) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

                if (records.isEmpty()) {
                    hasMoreMessages = false;
                }

                for (var record : records) {
                    if (count >= maxMessages) break;

                    messages.add(record.value());
                    count++;
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error reading messages from topic: " + topic, e);
        }

        return messages;
    }

    /**
     * Получение информации о топике (partitions, offsets)
     */
    public Map<Integer, Long[]> getTopicInfo(String topic) {
        Map<Integer, Long[]> topicInfo = new HashMap<>();

        Properties props = kafkaConfig.getKafkaConsumerProperties("info-group-" + UUID.randomUUID());

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            List<TopicPartition> partitions = new ArrayList<>();

            // Получаем все partitions для топика
            var partitionsInfo = consumer.partitionsFor(topic);
            for (var partitionInfo : partitionsInfo) {
                partitions.add(new TopicPartition(topic, partitionInfo.partition()));
            }

            // Assign consumer to partitions
            consumer.assign(partitions);

            // Получаем beginning и end offsets
            Map<TopicPartition, Long> beginningOffsets = consumer.beginningOffsets(partitions);
            Map<TopicPartition, Long> endOffsets = consumer.endOffsets(partitions);

            for (TopicPartition partition : partitions) {
                Long beginningOffset = beginningOffsets.get(partition);
                Long endOffset = endOffsets.get(partition);

                topicInfo.put(partition.partition(), new Long[]{beginningOffset, endOffset});
            }

        } catch (Exception e) {
            throw new RuntimeException("Error getting topic info for: " + topic, e);
        }

        return topicInfo;
    }

    /**
     * Печать информации о топике
     */
    public void printTopicInfo(String topic) {
        System.out.println("\n📊 Информация о топике: " + topic);

        try {
            Map<Integer, Long[]> info = getTopicInfo(topic);
            long totalMessages = countMessagesInTopic(topic);

            System.out.println("Всего сообщений: " + totalMessages);
            System.out.println("Partitions:");

            for (Map.Entry<Integer, Long[]> entry : info.entrySet()) {
                Integer partition = entry.getKey();
                Long[] offsets = entry.getValue();
                long messagesInPartition = offsets[1] - offsets[0];

                System.out.printf("  Partition %d: offsets [%d - %d], сообщений: %d%n",
                        partition, offsets[0], offsets[1], messagesInPartition);
            }

            // Дополнительная информация
            List<String> recentMessages = readAllMessagesFromTopic(topic, 3);
            if (!recentMessages.isEmpty()) {
                System.out.println("\nПоследние сообщения в топике:");
                for (int i = 0; i < Math.min(recentMessages.size(), 3); i++) {
                    String msg = recentMessages.get(i);
                    System.out.printf("  [%d] %s...%n", i+1,
                            msg.length() > 50 ? msg.substring(0, 50) + "..." : msg);
                }
            }

            System.out.println("==============================");

        } catch (Exception e) {
            System.err.println("Ошибка при получении информации о топике " + topic + ": " + e.getMessage());
        }
    }

    /**
     * Поиск сообщений по регулярному выражению
     */
    public List<String> findMessagesByPattern(String topic, Pattern pattern, int maxRecords) {
        List<String> foundMessages = new ArrayList<>();

        Properties props = kafkaConfig.getKafkaConsumerProperties("pattern-group-" + UUID.randomUUID(), maxRecords);

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(topic));

            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5));

            int count = 0;
            for (var record : records) {
                if (count >= maxRecords) break;

                if (pattern.matcher(record.value()).find()) {
                    foundMessages.add(record.value());
                }

                count++;
            }

        } catch (Exception e) {
            throw new RuntimeException("Error searching by pattern in topic: " + topic, e);
        }

        return foundMessages;
    }

    /**
     * Получение последнего сообщения из топика
     */
    public String getLatestMessage(String topic) {
        Properties props = kafkaConfig.getKafkaConsumerProperties("latest-group-" + UUID.randomUUID());
        props.put("auto.offset.reset", "latest");

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(topic));

            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(2));

            if (!records.isEmpty()) {
                var lastRecord = records.iterator().next();
                return lastRecord.value();
            }

        } catch (Exception e) {
            throw new RuntimeException("Error getting latest message from topic: " + topic, e);
        }

        return null;
    }

    /**
     * Подсчет сообщений в топике
     */
    public long countMessagesInTopic(String topic) {
        long totalMessages = 0;

        Properties props = kafkaConfig.getKafkaConsumerProperties("count-group-" + UUID.randomUUID());

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            List<TopicPartition> partitions = new ArrayList<>();

            var partitionsInfo = consumer.partitionsFor(topic);
            for (var partitionInfo : partitionsInfo) {
                partitions.add(new TopicPartition(topic, partitionInfo.partition()));
            }

            consumer.assign(partitions);

            // Получаем beginning и end offsets
            Map<TopicPartition, Long> beginningOffsets = consumer.beginningOffsets(partitions);
            Map<TopicPartition, Long> endOffsets = consumer.endOffsets(partitions);

            for (TopicPartition partition : partitions) {
                Long beginningOffset = beginningOffsets.get(partition);
                Long endOffset = endOffsets.get(partition);

                if (beginningOffset != null && endOffset != null) {
                    totalMessages += (endOffset - beginningOffset);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error counting messages in topic: " + topic, e);
        }

        return totalMessages;
    }

    /**
     * Проверка подключения к Kafka
     */
    public boolean testConnection() {
        try (KafkaProducer<String, String> producer =
                     new KafkaProducer<>(kafkaConfig.getKafkaProducerProperties())) {

            // Простой тест подключения
            producer.partitionsFor("system_logs");
            System.out.println("✅ Kafka connection successful");
            return true;

        } catch (Exception e) {
            System.err.println("❌ Kafka connection failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Генерация тестового сообщения
     */
    public Map<String, Object> generateTestMessage(String orderId, String customerId,
                                                   double amount, String status) {
        Map<String, Object> message = new HashMap<>();
        message.put("orderId", orderId);
        message.put("customerId", customerId);
        message.put("amount", amount);
        message.put("status", status);
        message.put("timestamp", System.currentTimeMillis());
        message.put("uuid", UUID.randomUUID().toString());

        // Добавляем случайные данные для разнообразия
        Random random = new Random();
        message.put("randomValue", random.nextInt(1000));
        message.put("processed", false);

        return message;
    }
}