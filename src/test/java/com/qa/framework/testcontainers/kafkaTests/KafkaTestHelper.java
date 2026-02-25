package com.qa.framework.testcontainers.kafkaTests;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.testcontainers.containers.KafkaContainer;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Helper класс для работы с Kafka в тестах.
 * Аналог ProducerAdapter и ConsumerAdapter, но для тестового контейнера.
 */
public class KafkaTestHelper {

    private final KafkaContainer container;
    private final String bootstrapServers;

    public KafkaTestHelper(KafkaContainer container) {
        this.container = container;
        this.bootstrapServers = container.getBootstrapServers();
    }

    /**
     * Создаёт продюсера для отправки сообщений
     */
    private KafkaProducer<String, String> createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");  // Ждём подтверждения от всех реплик
        props.put(ProducerConfig.RETRIES_CONFIG, 3);   // Повтор при ошибке
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);  // Идемпотентность (гарантия exactly-once)

        return new KafkaProducer<>(props);
    }

    /**
     * Создаёт консюмера для чтения сообщений
     */
    private KafkaConsumer<String, String> createConsumer(String groupId) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");  // Читаем с самого начала
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");    // Сами управляем коммитами

        return new KafkaConsumer<>(props);
    }

    /**
     * Отправить одно сообщение в топик
     *
     * @param topic имя топика
     * @param message сообщение (будет преобразовано в JSON)
     * @return true если успешно
     */
    public boolean sendMessage(String topic, Object message) {
        return sendMessage(topic, message, null);
    }

    /**
     * Отправить сообщение с заголовками
     *
     * @param topic имя топика
     * @param message сообщение
     * @param headers заголовки (может быть null)
     * @return true если успешно
     */
    public boolean sendMessage(String topic, Object message, Map<String, String> headers) {
        // Преобразуем объект в JSON
        String jsonMessage = message instanceof String ?
                (String) message :
                new com.google.gson.Gson().toJson(message);

        try (KafkaProducer<String, String> producer = createProducer()) {
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, jsonMessage);

            // Добавляем заголовки, если есть
            if (headers != null) {
                headers.forEach((key, value) ->
                        record.headers().add(key, value.getBytes())
                );
            }

            // Отправляем и ждём подтверждения
            RecordMetadata metadata = producer.send(record).get(10, TimeUnit.SECONDS);

            System.out.println("✅ Сообщение отправлено в топик " + topic);
            System.out.println("   Partition: " + metadata.partition());
            System.out.println("   Offset: " + metadata.offset());
            if (headers != null && headers.containsKey("X-Transaction-Req-Id")) {
                System.out.println("   X-Transaction-Req-Id: " + headers.get("X-Transaction-Req-Id"));
            }

            return true;

        } catch (TimeoutException e) {
            System.err.println("❌ Таймаут при отправке сообщения");
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("❌ Отправка прервана");
            return false;
        } catch (ExecutionException e) {
            System.err.println("❌ Ошибка при отправке: " + e.getCause().getMessage());
            return false;
        }
    }

    /**
     * Прочитать сообщения из топика
     *
     * @param topic имя топика
     * @param groupId ID группы консюмера
     * @param maxMessages максимальное количество сообщений для чтения
     * @param maxWaitSeconds максимальное время ожидания
     * @return список прочитанных сообщений
     */
    public List<ConsumerRecord<String, String>> readMessages(String topic,
                                                             String groupId,
                                                             int maxMessages,
                                                             int maxWaitSeconds) {
        List<ConsumerRecord<String, String>> messages = new ArrayList<>();

        try (KafkaConsumer<String, String> consumer = createConsumer(groupId)) {
            // Подписываемся на топик
            consumer.subscribe(Collections.singletonList(topic));

            // Ждём назначения партиций
            consumer.poll(Duration.ofMillis(100));

            // Перемещаемся в начало
            consumer.assignment().forEach(partition ->
                    consumer.seekToBeginning(Collections.singleton(partition))
            );

            int attempts = 0;
            int maxAttempts = maxWaitSeconds * 2; // poll каждые 500ms

            System.out.println("📥 Чтение сообщений из топика " + topic + "...");

            while (attempts < maxAttempts && messages.size() < maxMessages) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));

                if (!records.isEmpty()) {
                    records.forEach(record -> {
                        messages.add(record);
                        System.out.printf("   Найдено сообщение: partition=%d, offset=%d, value=%s%n",
                                record.partition(), record.offset(),
                                record.value().length() > 100 ?
                                        record.value().substring(0, 100) + "..." :
                                        record.value());
                    });
                } else {
                    System.out.println("   Попытка " + (attempts + 1) +
                            "/" + maxAttempts + ": сообщений пока нет");
                }

                attempts++;
            }

            System.out.println("✅ Прочитано " + messages.size() + " сообщений");
            return messages;

        } catch (Exception e) {
            System.err.println("❌ Ошибка при чтении сообщений: " + e.getMessage());
            return messages;
        }
    }

    /**
     * Прочитать последние N сообщений из топика
     */
    public List<ConsumerRecord<String, String>> readLastMessages(String topic,
                                                                 String groupId,
                                                                 int numberOfMessages,
                                                                 int maxWaitSeconds) {
        try (KafkaConsumer<String, String> consumer = createConsumer(groupId)) {
            consumer.subscribe(Collections.singletonList(topic));

            // Ждём назначения партиций
            int attempts = 0;
            while (consumer.assignment().isEmpty() && attempts < 5) {
                consumer.poll(Duration.ofMillis(500));
                attempts++;
            }

            if (consumer.assignment().isEmpty()) {
                System.err.println("❌ Не удалось получить партиции для топика " + topic);
                return Collections.emptyList();
            }

            // Для каждой партиции узнаём последний offset
            consumer.assignment().forEach(partition -> {
                consumer.seekToEnd(Collections.singleton(partition));
                long endOffset = consumer.position(partition);
                long startOffset = Math.max(0, endOffset - numberOfMessages);
                consumer.seek(partition, startOffset);

                System.out.printf("   Partition %d: читаем offset'ы с %d по %d%n",
                        partition.partition(), startOffset, endOffset);
            });

            // Читаем сообщения
            List<ConsumerRecord<String, String>> messages = new ArrayList<>();
            int attempts2 = 0;
            int maxAttempts = maxWaitSeconds * 2;

            while (attempts2 < maxAttempts && messages.size() < numberOfMessages) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                records.forEach(messages::add);

                if (!records.isEmpty()) {
                    System.out.println("   Получено " + records.count() +
                            " сообщений, всего: " + messages.size());
                }

                attempts2++;
            }

            return messages;

        } catch (Exception e) {
            System.err.println("❌ Ошибка при чтении последних сообщений: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Создать тестовое сообщение с уникальным testRunId
     */
    public static Map<String, Object> createTestMessage(String orderId,
                                                        String customerId,
                                                        double amount,
                                                        String status) {
        Map<String, Object> message = new HashMap<>();
        message.put("orderId", orderId);
        message.put("customerId", customerId);
        message.put("amount", amount);
        message.put("status", status);
        message.put("timestamp", System.currentTimeMillis());
        message.put("testRunId", "TEST-KAFKA-" + System.currentTimeMillis());
        message.put("randomValue", new Random().nextInt(1000));
        return message;
    }

    /**
     * Получить информацию о топике (количество сообщений, партиции)
     */
    public Map<String, Object> getTopicInfo(String topic) {
        String groupId = "info-group-" + System.currentTimeMillis();

        try (KafkaConsumer<String, String> consumer = createConsumer(groupId)) {
            Map<String, Object> info = new HashMap<>();

            // Получаем информацию о партициях
            var partitionsInfo = consumer.partitionsFor(topic);
            if (partitionsInfo == null || partitionsInfo.isEmpty()) {
                info.put("error", "Топик не найден");
                return info;
            }

            // Подписываемся и получаем партиции
            consumer.subscribe(Collections.singletonList(topic));
            consumer.poll(Duration.ofMillis(500));

            Set<org.apache.kafka.common.TopicPartition> partitions = consumer.assignment();

            if (partitions.isEmpty()) {
                info.put("error", "Не удалось получить партиции");
                return info;
            }

            // Получаем offsets
            Map<org.apache.kafka.common.TopicPartition, Long> endOffsets =
                    consumer.endOffsets(partitions);

            long totalMessages = endOffsets.values().stream()
                    .mapToLong(Long::longValue)
                    .sum();

            info.put("topic", topic);
            info.put("partitionCount", partitions.size());
            info.put("totalMessages", totalMessages);
            info.put("partitions", partitions.stream()
                    .map(p -> Map.of(
                            "partition", p.partition(),
                            "lastOffset", endOffsets.get(p)
                    ))
                    .collect(java.util.stream.Collectors.toList()));

            return info;

        } catch (Exception e) {
            System.err.println("❌ Ошибка получения информации о топике: " + e.getMessage());
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("error", e.getMessage());
            return errorInfo;
        }
    }
}