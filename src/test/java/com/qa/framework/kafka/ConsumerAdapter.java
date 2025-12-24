package com.qa.framework.kafka;

import com.qa.framework.config.KafkaConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetAndTimestamp;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ConsumerAdapter {

    private static final int NUMBER_LAST_MESSAGE = 6;
    private static final KafkaConfig KAFKA_CONFIG = new KafkaConfig();

    private static KafkaConsumer<String, String> createConsumer(String bootstrapServers,
                                                                String topicName,
                                                                String groupName) {
        final Properties props = KAFKA_CONFIG.getKafkaConsumerProperties(groupName);

        final KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList(topicName));

        return consumer;
    }

    private static KafkaConsumer<String, String> createConsumerAndSeekToTimestamp(
            String bootstrapServers, String topicName, String groupName, long timestampMs) {

        final Properties props = KAFKA_CONFIG.getKafkaConsumerProperties(groupName);
        // Важно: отключаем auto-commit, чтобы не влиять на группу.
        props.setProperty("enable.auto.commit", "false");

        final KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList(topicName));

        // Даем время на выполнение подписки и назначение разделов (assignment)
        consumer.poll(Duration.ofSeconds(2));

        Set<TopicPartition> partitions = consumer.assignment();
        if (partitions.isEmpty()) {
            System.err.println("❌ Consumer did not assign any partitions to topic " + topicName);
            return consumer;
        }

        Map<TopicPartition, Long> timestampsToSearch = new HashMap<>();
        for (TopicPartition partition : partitions) {
            timestampsToSearch.put(partition, timestampMs);
        }

        Map<TopicPartition, OffsetAndTimestamp> offsets = consumer.offsetsForTimes(timestampsToSearch);

        for (Map.Entry<TopicPartition, OffsetAndTimestamp> entry : offsets.entrySet()) {
            TopicPartition partition = entry.getKey();
            OffsetAndTimestamp offsetAndTimestamp = entry.getValue();

            if (offsetAndTimestamp != null) {
                // Если offset найден, перемещаемся к нему
                consumer.seek(partition, offsetAndTimestamp.offset());
                System.out.println("   Partition " + partition.partition() +
                        ": Seeking to offset " + offsetAndTimestamp.offset());
            } else {
                // Если offset не найден, перемещаемся в начало.
                consumer.seekToBeginning(Collections.singleton(partition));
                System.out.println("   Partition " + partition.partition() +
                        ": Seeking to beginning as offset was not found for timestamp.");
            }
        }

        return consumer;
    }

    /**
     * Читает сообщения из топика, начиная с указанной временной метки,
     * и возвращает их в виде списка объектов KafkaMessage.
     * @param startTimestampMs временная метка, начиная с которой нужно читать сообщения (в миллисекундах).
     * @param maxWaitSeconds максимальное время ожидания.
     * @return List<KafkaMessage> список прочитанных и конвертированных сообщений.
     */
    public static List<KafkaMessage> readMessagesFromTimestamp(
            String bootstrapServers,
            String topicName,
            String groupName,
            long startTimestampMs,
            int maxWaitSeconds) {

        System.out.println("\n🔎 Начинаем чтение сообщений с timestamp: " + startTimestampMs);

        final KafkaConsumer<String, String> consumer =
                createConsumerAndSeekToTimestamp(bootstrapServers, topicName, groupName, startTimestampMs);

        // Используем List для сбора всех записей (вместо ConsumerRecords, чтобы избежать проблемы с addAll)
        List<ConsumerRecord<String, String>> collectedRecords = new ArrayList<>();
        int attempts = 0;
        int maxAttempts = maxWaitSeconds * 2; // Проверяем каждые 0.5 секунды

        while (attempts < maxAttempts) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));

            if (!records.isEmpty()) {
                // Собираем все записи из текущего poll в наш изменяемый список
                records.forEach(collectedRecords::add);
                System.out.println("   Found " + records.count() +
                        " records. Total collected: " + collectedRecords.size());
            } else {
                System.out.println("   No new records found in this poll.");
            }

            attempts++;
            if (collectedRecords.isEmpty() && attempts >= maxAttempts) {
                System.out.println("   Exiting search: Max attempts reached without finding records.");
                break;
            } else if (!collectedRecords.isEmpty() && attempts >= maxAttempts) {
                // Если что-то нашли, даем еще один шанс прочитать, потом выходим
                System.out.println("   Exiting search: Max attempts reached after finding records.");
                break;
            }
        }

        consumer.close();

        // Конвертируем собранные сырые записи в KafkaMessage
        return convertRawRecordsToMessageObject(collectedRecords);
    }

    /**
     * Конвертирует список сырых ConsumerRecord в список KafkaMessage.
     * Эта логика была вынесена, чтобы ее можно было повторно использовать.
     */
    private static List<KafkaMessage> convertRawRecordsToMessageObject(
            List<ConsumerRecord<String, String>> rawRecords) {

        List<KafkaMessage> result = new ArrayList<>();

        rawRecords.forEach(record -> {
            Headers consumedHeaders = record.headers();

            // Сохраняем все заголовки
            List<Header> headersList = StreamSupport.stream(consumedHeaders.spliterator(), false)
                    .collect(Collectors.toList());

            KafkaMessage msg = KafkaMessage.builder()
                    .headers(headersList)
                    .body(record.value())
                    .partition(record.partition())
                    .offset(record.offset())
                    .timestamp(record.timestamp())
                    .key(record.key())
                    .topic(record.topic())
                    .build();

            result.add(msg);
        });

        return result;
    }


    // Оставшиеся методы, использующие ConsumerRecords, остаются прежними,
    // но convertRecordsToMessageObject теперь использует новую частную логику.

    public static List<KafkaMessage> convertRecordsToMessageObject(
            ConsumerRecords<String, String> consumerRecords) {

        // Временно конвертируем ConsumerRecords в List, чтобы использовать общую логику конвертации.
        List<ConsumerRecord<String, String>> rawRecords = new ArrayList<>();
        consumerRecords.forEach(rawRecords::add);
        return convertRawRecordsToMessageObject(rawRecords);
    }

    // ... (Остальные методы без изменений: readMessage, convertRecordsToMessageSortedByTimestamp, readMessagesWithWait) ...

    public static ConsumerRecords<String, String> readMessage(String bootstrapServers,
                                                              String topicName,
                                                              String groupName) {
        final KafkaConsumer<String, String> consumer =
                createConsumer(bootstrapServers, topicName, groupName);

        ConsumerRecords<String, String> consumerRecords = null;
        final int giveUp = 100;
        int noRecordsCount = 0;

        // Настраиваем позицию для чтения (последние N сообщений)
        Set<TopicPartition> setOfPartitions = consumer.assignment();
        consumer.poll(1000); // Для инициализации assignment

        for (TopicPartition topicPartition : setOfPartitions) {
            consumer.seekToBeginning(Collections.singleton(topicPartition));
            long startPosition = consumer.position(topicPartition);
            consumer.seekToEnd(Collections.singleton(topicPartition));
            long endPosition = consumer.position(topicPartition);

            if (endPosition - startPosition > NUMBER_LAST_MESSAGE) {
                consumer.seek(topicPartition, endPosition - NUMBER_LAST_MESSAGE);
                System.out.println("Setup: topicPartition = " + topicPartition +
                        ", position from = " + (endPosition - NUMBER_LAST_MESSAGE));
            } else {
                consumer.seek(topicPartition, startPosition);
                System.out.println("Setup: topicPartition = " + topicPartition +
                        ", position from = " + startPosition);
            }
        }

        System.out.println("Consumes records from topic " + topicName + ":");

        while (true) {
            try {
                consumerRecords = consumer.poll(Duration.ofMillis(200)); // Используем Duration
            } catch (Exception e) {
                e.printStackTrace();
            }
            // ... (логика проверки и выхода) ...
            if (consumerRecords == null || consumerRecords.count() == 0) {
                noRecordsCount++;
                if (noRecordsCount > giveUp) break;
                else continue;
            }

            consumerRecords.forEach(record -> {
                System.out.printf("Consumer Record from topic %s:(partition=%s, value=%s)\n",
                        topicName.toUpperCase(), record.partition(),
                        record.value().length() > 100 ?
                                record.value().substring(0, 100) + "..." : record.value());
            });

            consumer.commitAsync();
            break;
        }

        consumer.close();
        return consumerRecords != null ? consumerRecords : ConsumerRecords.empty();
    }


    public static SortedMap<Long, String> convertRecordsToMessageSortedByTimestamp(
            ConsumerRecords<String, String> consumerRecords) {

        SortedMap<Long, String> map = new TreeMap<>();
        consumerRecords.forEach(record -> {
            map.put(record.timestamp(), record.value());
        });

        return map;
    }

    public static ConsumerRecords<String, String> readMessagesWithWait(
            String bootstrapServers,
            String topicName,
            String groupName,
            int maxWaitSeconds) {

        final KafkaConsumer<String, String> consumer =
                createConsumer(bootstrapServers, topicName, groupName);

        ConsumerRecords<String, String> consumerRecords = null;
        int attempts = 0;
        int maxAttempts = maxWaitSeconds * 2;

        consumer.poll(Duration.ofMillis(100)); // Используем Duration

        Set<TopicPartition> setOfPartitions = consumer.assignment();
        if (!setOfPartitions.isEmpty()) {
            consumer.seekToBeginning(setOfPartitions);
        }

        while (attempts < maxAttempts) {
            consumerRecords = consumer.poll(Duration.ofMillis(500)); // Используем Duration

            if (consumerRecords.count() > 0) {
                System.out.println("Found " + consumerRecords.count() +
                        " messages in topic " + topicName);
                break;
            }

            attempts++;
            if (attempts < maxAttempts) {
                System.out.println("No messages found, attempt " + attempts +
                        " of " + maxAttempts);
            }
        }

        if (consumerRecords != null && consumerRecords.count() > 0) {
            consumer.commitAsync();
        }

        consumer.close();
        return consumerRecords != null ? consumerRecords : ConsumerRecords.empty();
    }
}