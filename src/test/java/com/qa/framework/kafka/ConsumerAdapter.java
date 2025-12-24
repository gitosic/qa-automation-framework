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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ConsumerAdapter {

    private static final int NUMBER_LAST_MESSAGE = 5;
    private static final int DEFAULT_MAX_WAIT_ATTEMPTS = 20; // 10 секунд при poll 500ms
    private static final KafkaConfig KAFKA_CONFIG = new KafkaConfig();
    private static final SimpleDateFormat TIMESTAMP_FORMATTER =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * Создание Consumer с указанным groupId
     */
    private static KafkaConsumer<String, String> createConsumer(String bootstrapServers,
                                                                String topicName,
                                                                String groupName) {
        final Properties props = KAFKA_CONFIG.getKafkaConsumerProperties(groupName);
        final KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList(topicName));
        return consumer;
    }

    /**
     * Форматирование timestamp в читаемую строку
     */
    private static String formatTimestamp(long timestampMs) {
        return TIMESTAMP_FORMATTER.format(new Date(timestampMs)) + " (" + timestampMs + ")";
    }

    /**
     * Читает сообщения из топика, начиная с указанной временной метки.
     * ВНИМАНИЕ: Этот метод читает ВСЕ сообщения НАЧИНАЯ с указанного timestamp,
     * без ограничения по конечной дате. Для ограниченного диапазона используйте
     * метод readMessagesInTimeRange.
     *
     * @param startTimestampMs временная метка, начиная с которой нужно читать сообщения (в миллисекундах).
     * @return List<KafkaMessage> список прочитанных и конвертированных сообщений.
     */
    public static List<KafkaMessage> readMessagesFromTimestamp(
            String bootstrapServers,
            String topicName,
            String groupName,
            long startTimestampMs,
            int maxWaitSeconds) {

        System.out.println("\n🔎 Начинаем чтение сообщений с timestamp: " + formatTimestamp(startTimestampMs));

        final Properties props = KAFKA_CONFIG.getKafkaConsumerProperties(groupName);
        props.setProperty("enable.auto.commit", "false");
        final KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList(topicName));

        // 1. Ожидаем assignment и получаем разделы
        consumer.poll(Duration.ofSeconds(2));
        Set<TopicPartition> partitions = consumer.assignment();

        // ИСПРАВЛЕННАЯ ПРОВЕРКА: Улучшенное ожидание назначения partitions
        if (partitions.isEmpty()) {
            System.out.println("⏳ No partitions assigned yet, waiting for assignment...");

            // Делаем несколько попыток получить partitions
            int attempts = 0;
            while (partitions.isEmpty() && attempts < 5) {
                System.out.println("  Attempt " + (attempts + 1) + "/5 to get partitions...");
                consumer.poll(Duration.ofMillis(500));
                partitions = consumer.assignment();
                attempts++;

                // Проверяем существование топика после нескольких неудачных попыток
                if (attempts >= 3 && partitions.isEmpty()) {
                    try {
                        var partitionsInfo = consumer.partitionsFor(topicName);
                        if (partitionsInfo == null || partitionsInfo.isEmpty()) {
                            System.err.println("❌ Topic " + topicName + " does not exist or has no partitions");
                            consumer.close();
                            return Collections.emptyList();
                        } else {
                            System.out.println("ℹ️ Topic exists with " + partitionsInfo.size() +
                                    " partitions, but consumer not assigned yet");
                        }
                    } catch (Exception e) {
                        System.err.println("❌ Error checking topic existence: " + e.getMessage());
                    }
                }
            }

            if (partitions.isEmpty()) {
                System.err.println("❌ Failed to get partition assignment for topic " + topicName +
                        " after 5 attempts. Possible reasons:");
                System.err.println("   - Topic doesn't exist");
                System.err.println("   - Kafka cluster issues");
                System.err.println("   - Consumer group coordination delay");
                consumer.close();
                return Collections.emptyList();
            }
        }

        System.out.println("✅ Assigned partitions: " + partitions);

        // 2. Получаем OffsetAndTimestamp для каждой партиции
        Map<TopicPartition, Long> timestampsToSearch = new HashMap<>();
        for (TopicPartition partition : partitions) {
            timestampsToSearch.put(partition, startTimestampMs);
        }
        Map<TopicPartition, OffsetAndTimestamp> offsets = consumer.offsetsForTimes(timestampsToSearch);

        // 3. Перемещаемся (Seek) к найденному offset
        for (Map.Entry<TopicPartition, OffsetAndTimestamp> entry : offsets.entrySet()) {
            TopicPartition partition = entry.getKey();
            OffsetAndTimestamp offsetAndTimestamp = entry.getValue();

            if (offsetAndTimestamp != null) {
                consumer.seek(partition, offsetAndTimestamp.offset());
                System.out.println("   Partition " + partition.partition() +
                        ": Seeking to offset " + offsetAndTimestamp.offset() +
                        " (timestamp: " + formatTimestamp(offsetAndTimestamp.timestamp()) + ")");
            } else {
                // Если не нашли offset для указанного timestamp, начинаем с самого начала
                consumer.seekToBeginning(Collections.singleton(partition));
                System.out.println("   Partition " + partition.partition() +
                        ": Seeking to beginning (no offset found for timestamp " +
                        formatTimestamp(startTimestampMs) + ")");
            }
        }

        // 4. Читаем записи
        List<ConsumerRecord<String, String>> collectedRecords = new ArrayList<>();
        int attempts = 0;
        int maxAttempts = maxWaitSeconds * 2; // Каждые 0.5 секунды

        while (attempts < maxAttempts) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));

            if (!records.isEmpty()) {
                records.forEach(collectedRecords::add);
                System.out.println("   Found " + records.count() +
                        " records. Total collected: " + collectedRecords.size());
            } else {
                System.out.println("   No new records found in this poll.");
            }

            attempts++;
            if (collectedRecords.isEmpty() && attempts >= maxAttempts) {
                System.out.println("   Stopping: no records found after " + maxAttempts + " attempts");
                break;
            } else if (!collectedRecords.isEmpty() && attempts >= maxAttempts) {
                System.out.println("   Stopping: collected " + collectedRecords.size() +
                        " records after " + maxAttempts + " attempts");
                break;
            }
        }

        consumer.close();

        if (collectedRecords.isEmpty()) {
            System.out.println("❌ No messages found starting from " + formatTimestamp(startTimestampMs));
        } else {
            System.out.println("✅ Collected " + collectedRecords.size() + " messages");
        }

        return convertRawRecordsToMessageObject(collectedRecords);
    }

    /**
     * ИСПРАВЛЕННЫЙ МЕТОД: Читает последние N сообщений из топика.
     * Улучшенная логика ожидания назначения partitions.
     */
    public static ConsumerRecords<String, String> readMessage(String bootstrapServers,
                                                              String topicName,
                                                              String groupName) {
        final KafkaConsumer<String, String> consumer =
                createConsumer(bootstrapServers, topicName, groupName);

        ConsumerRecords<String, String> consumerRecords = null;
        final int giveUp = 100;
        int noRecordsCount = 0;

        // 1. Улучшенная логика получения назначенных partitions
        Set<TopicPartition> setOfPartitions = consumer.assignment();
        int partitionWaitAttempts = 0;
        int maxPartitionWaitAttempts = 10;

        while (setOfPartitions.isEmpty() && partitionWaitAttempts < maxPartitionWaitAttempts) {
            System.out.printf("⏳ Waiting for partition assignment (attempt %d/%d)...%n",
                    partitionWaitAttempts + 1, maxPartitionWaitAttempts);

            consumer.poll(Duration.ofSeconds(1));
            setOfPartitions = consumer.assignment();
            partitionWaitAttempts++;

            // Проверяем существование топика после нескольких неудачных попыток
            if (partitionWaitAttempts >= 3 && setOfPartitions.isEmpty()) {
                try {
                    var partitionsInfo = consumer.partitionsFor(topicName);
                    if (partitionsInfo == null || partitionsInfo.isEmpty()) {
                        System.err.println("❌ Topic " + topicName + " does not exist or has no partitions");
                        consumer.close();
                        return ConsumerRecords.empty();
                    } else {
                        System.out.println("ℹ️ Topic exists with " + partitionsInfo.size() +
                                " partitions, waiting for assignment...");
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error checking topic " + topicName + ": " + e.getMessage());
                }
            }
        }

        if (setOfPartitions.isEmpty()) {
            System.err.println("❌ Failed to get partition assignment for topic " +
                    topicName + " after " + maxPartitionWaitAttempts + " attempts");
            consumer.close();
            return ConsumerRecords.empty();
        }

        System.out.println("✅ Assigned partitions: " + setOfPartitions);

        // 2. Вычисляем и устанавливаем смещения (seek logic)
        for (TopicPartition topicPartition : setOfPartitions) {
            consumer.seekToBeginning(Collections.singleton(topicPartition));
            long startPosition = consumer.position(topicPartition);
            consumer.seekToEnd(Collections.singleton(topicPartition));
            long endPosition = consumer.position(topicPartition);

            if (endPosition - startPosition > NUMBER_LAST_MESSAGE) {
                // Если сообщений больше, чем NUMBER_LAST_MESSAGE, читаем только последние N
                consumer.seek(topicPartition, endPosition - NUMBER_LAST_MESSAGE);
                System.out.printf("   Partition %d: reading last %d messages (offset %d to %d)%n",
                        topicPartition.partition(), NUMBER_LAST_MESSAGE,
                        endPosition - NUMBER_LAST_MESSAGE, endPosition);
            } else {
                // Иначе читаем с начала
                consumer.seek(topicPartition, startPosition);
                System.out.printf("   Partition %d: reading all %d messages (offset %d to %d)%n",
                        topicPartition.partition(), endPosition - startPosition,
                        startPosition, endPosition);
            }
        }

        System.out.println("📥 Consumes records from topic " + topicName + ":");

        // 3. Основной цикл чтения
        while (true) {
            try {
                consumerRecords = consumer.poll(Duration.ofMillis(500));
            } catch (Exception e) {
                System.err.println("❌ Error polling messages: " + e.getMessage());
                e.printStackTrace();
                break;
            }

            if (consumerRecords == null || consumerRecords.count() == 0) {
                noRecordsCount++;
                System.out.printf("   No messages found (attempt %d/%d)%n",
                        noRecordsCount, giveUp);

                if (noRecordsCount > giveUp) {
                    System.out.println("   Giving up after " + giveUp + " attempts");
                    break;
                }
                continue;
            }

            System.out.printf("✅ Found %d messages in topic %s%n",
                    consumerRecords.count(), topicName);

            // Логируем найденные сообщения
            consumerRecords.forEach(record -> {
                System.out.printf("   Partition %d, Offset %d: %s%n",
                        record.partition(), record.offset(),
                        record.value().length() > 100 ?
                                record.value().substring(0, 100) + "..." : record.value());
            });

            consumer.commitAsync();
            break;
        }

        consumer.close();
        return consumerRecords != null ? consumerRecords : ConsumerRecords.empty();
    }

    /**
     * Читает сообщения из топика за указанный временной диапазон.
     * Этот метод предпочтительнее readMessagesFromTimestamp, так как ограничивает
     * как начальную, так и конечную дату.
     *
     * @param startTimestampMs начало диапазона (в миллисекундах)
     * @param endTimestampMs   конец диапазона (в миллисекундах), если 0 - читает до текущего момента
     * @param maxWaitSeconds   максимальное время ожидания в секундах
     * @return List<KafkaMessage> список прочитанных сообщений в указанном диапазоне
     */
    public static List<KafkaMessage> readMessagesInTimeRange(
            String bootstrapServers,
            String topicName,
            String groupName,
            long startTimestampMs,
            long endTimestampMs,
            int maxWaitSeconds) {

        System.out.println("\n🔎 Чтение сообщений в диапазоне времени:");
        System.out.println("   С: " + formatTimestamp(startTimestampMs));
        System.out.println("   По: " + (endTimestampMs > 0 ? formatTimestamp(endTimestampMs) : "текущее время"));

        final Properties props = KAFKA_CONFIG.getKafkaConsumerProperties(groupName);
        props.setProperty("enable.auto.commit", "false");
        final KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList(topicName));

        // 1. Ожидаем assignment и получаем разделы
        consumer.poll(Duration.ofSeconds(2));
        Set<TopicPartition> partitions = consumer.assignment();

        // Улучшенная проверка назначения partitions
        if (partitions.isEmpty()) {
            System.out.println("⏳ No partitions assigned, waiting...");
            int attempts = 0;
            while (partitions.isEmpty() && attempts < 5) {
                consumer.poll(Duration.ofMillis(500));
                partitions = consumer.assignment();
                attempts++;
            }

            if (partitions.isEmpty()) {
                System.err.println("❌ Failed to get partition assignment");
                consumer.close();
                return Collections.emptyList();
            }
        }

        System.out.println("✅ Assigned partitions: " + partitions);

        // 2. Получаем OffsetAndTimestamp для начала диапазона
        Map<TopicPartition, Long> timestampsToSearch = new HashMap<>();
        for (TopicPartition partition : partitions) {
            timestampsToSearch.put(partition, startTimestampMs);
        }
        Map<TopicPartition, OffsetAndTimestamp> startOffsets = consumer.offsetsForTimes(timestampsToSearch);

        // 3. Перемещаемся к началу диапазона
        for (Map.Entry<TopicPartition, OffsetAndTimestamp> entry : startOffsets.entrySet()) {
            TopicPartition partition = entry.getKey();
            OffsetAndTimestamp offsetAndTimestamp = entry.getValue();

            if (offsetAndTimestamp != null) {
                consumer.seek(partition, offsetAndTimestamp.offset());
                System.out.println("   Partition " + partition.partition() +
                        ": Seeking to offset " + offsetAndTimestamp.offset() +
                        " (timestamp: " + formatTimestamp(offsetAndTimestamp.timestamp()) + ")");
            } else {
                // Если не нашли offset для указанного timestamp, начинаем с самого начала
                consumer.seekToBeginning(Collections.singleton(partition));
                System.out.println("   Partition " + partition.partition() +
                        ": Seeking to beginning (timestamp not found)");
            }
        }

        // 4. Читаем записи с фильтрацией по времени
        List<ConsumerRecord<String, String>> collectedRecords = new ArrayList<>();
        int attempts = 0;
        int maxAttempts = maxWaitSeconds * 2; // Каждые 0.5 секунды
        boolean keepReading = true;

        while (attempts < maxAttempts && keepReading) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));

            if (!records.isEmpty()) {
                int recordsInRange = 0;

                for (ConsumerRecord<String, String> record : records) {
                    // Проверяем, попадает ли запись в диапазон времени
                    boolean withinTimeRange;

                    if (endTimestampMs > 0) {
                        // Есть конечная граница диапазона
                        withinTimeRange = record.timestamp() >= startTimestampMs &&
                                record.timestamp() <= endTimestampMs;
                    } else {
                        // Нет конечной границы - читаем все от startTimestampMs
                        withinTimeRange = record.timestamp() >= startTimestampMs;
                    }

                    if (withinTimeRange) {
                        collectedRecords.add(record);
                        recordsInRange++;
                    } else if (endTimestampMs > 0 && record.timestamp() > endTimestampMs) {
                        // Если запись вышла за пределы диапазона, останавливаем чтение
                        keepReading = false;
                        System.out.println("   ⏹️ Record timestamp " + formatTimestamp(record.timestamp()) +
                                " is beyond end range, stopping");
                        break;
                    }
                }

                if (recordsInRange > 0) {
                    System.out.println("   Found " + recordsInRange +
                            " records in time range. Total: " + collectedRecords.size());
                }
            } else {
                System.out.println("   No new records found in this poll.");
            }

            attempts++;

            // Останавливаем, если:
            // 1. Превышено максимальное количество попыток
            // 2. Нашли записи и достигли конца диапазона
            // 3. Не нашли записей и проверили достаточно раз
            if ((!keepReading) ||
                    (attempts >= maxAttempts && !collectedRecords.isEmpty()) ||
                    (attempts >= maxAttempts && collectedRecords.isEmpty())) {
                break;
            }
        }

        System.out.println("📊 Total collected records in time range: " + collectedRecords.size());

        // Фильтруем по времени еще раз на всякий случай
        List<ConsumerRecord<String, String>> filteredRecords = collectedRecords.stream()
                .filter(record -> {
                    if (endTimestampMs > 0) {
                        return record.timestamp() >= startTimestampMs &&
                                record.timestamp() <= endTimestampMs;
                    }
                    return record.timestamp() >= startTimestampMs;
                })
                .collect(Collectors.toList());

        if (filteredRecords.size() != collectedRecords.size()) {
            System.out.println("⚠️ Filtered out " + (collectedRecords.size() - filteredRecords.size()) +
                    " records outside time range");
        }

        consumer.close();
        return convertRawRecordsToMessageObject(filteredRecords);
    }

    /**
     * Читает сообщения за последние N минут
     * @param minutes количество минут для поиска назад
     * @param maxWaitSeconds максимальное время ожидания в секундах
     * @return List<KafkaMessage> сообщения за указанный период
     */
    public static List<KafkaMessage> readMessagesFromLastMinutes(
            String bootstrapServers,
            String topicName,
            String groupName,
            int minutes,
            int maxWaitSeconds) {

        long endTimestampMs = System.currentTimeMillis();
        long startTimestampMs = endTimestampMs - (minutes * 60 * 1000L);

        System.out.println("\n⏱️ Чтение сообщений за последние " + minutes + " минут");

        return readMessagesInTimeRange(
                bootstrapServers,
                topicName,
                groupName,
                startTimestampMs,
                endTimestampMs,
                maxWaitSeconds
        );
    }

    /**
     * Читает сообщения за последние N часов
     * @param hours количество часов для поиска назад
     * @param maxWaitSeconds максимальное время ожидания в секундах
     * @return List<KafkaMessage> сообщения за указанный период
     */
    public static List<KafkaMessage> readMessagesFromLastHours(
            String bootstrapServers,
            String topicName,
            String groupName,
            int hours,
            int maxWaitSeconds) {

        return readMessagesFromLastMinutes(
                bootstrapServers,
                topicName,
                groupName,
                hours * 60,
                maxWaitSeconds
        );
    }

    /**
     * Поиск сообщений по условию в указанном временном диапазоне
     */
    public static List<KafkaMessage> findMessagesInTimeRange(
            String bootstrapServers,
            String topicName,
            String groupName,
            long startTimestampMs,
            long endTimestampMs,
            java.util.function.Predicate<String> condition,
            int maxWaitSeconds) {

        List<KafkaMessage> allMessages = readMessagesInTimeRange(
                bootstrapServers, topicName, groupName,
                startTimestampMs, endTimestampMs, maxWaitSeconds
        );

        return allMessages.stream()
                .filter(msg -> condition.test(msg.getBody()))
                .collect(Collectors.toList());
    }

    /**
     * Поиск сообщений по условию за последние N минут
     */
    public static List<KafkaMessage> findMessagesFromLastMinutes(
            String bootstrapServers,
            String topicName,
            String groupName,
            int minutes,
            java.util.function.Predicate<String> condition,
            int maxWaitSeconds) {

        List<KafkaMessage> allMessages = readMessagesFromLastMinutes(
                bootstrapServers, topicName, groupName, minutes, maxWaitSeconds
        );

        return allMessages.stream()
                .filter(msg -> condition.test(msg.getBody()))
                .collect(Collectors.toList());
    }

    // Вспомогательный метод для конвертации сырых записей
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

    /**
     * Конвертирует ConsumerRecords в List<KafkaMessage>
     */
    public static List<KafkaMessage> convertRecordsToMessageObject(
            ConsumerRecords<String, String> consumerRecords) {

        List<ConsumerRecord<String, String>> rawRecords = new ArrayList<>();
        consumerRecords.forEach(rawRecords::add);
        return convertRawRecordsToMessageObject(rawRecords);
    }

    /**
     * Конвертирует записи в отсортированную по времени карту
     */
    public static SortedMap<Long, String> convertRecordsToMessageSortedByTimestamp(
            ConsumerRecords<String, String> consumerRecords) {

        SortedMap<Long, String> map = new TreeMap<>();
        consumerRecords.forEach(record -> {
            map.put(record.timestamp(), record.value());
        });

        return map;
    }

    /**
     * Чтение сообщений с ожиданием (основной метод для тестов)
     */
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

        // Инициализация assignment
        consumer.poll(Duration.ofMillis(100));

        Set<TopicPartition> setOfPartitions = consumer.assignment();

        // Улучшенное ожидание partitions
        if (setOfPartitions.isEmpty()) {
            System.out.println("⏳ Waiting for partition assignment...");
            int waitAttempts = 0;
            while (setOfPartitions.isEmpty() && waitAttempts < 5) {
                consumer.poll(Duration.ofMillis(500));
                setOfPartitions = consumer.assignment();
                waitAttempts++;
            }
        }

        if (!setOfPartitions.isEmpty()) {
            consumer.seekToBeginning(setOfPartitions);
            System.out.println("✅ Reading from beginning of " + setOfPartitions.size() + " partitions");
        }

        while (attempts < maxAttempts) {
            consumerRecords = consumer.poll(Duration.ofMillis(500));

            if (consumerRecords != null && consumerRecords.count() > 0) {
                System.out.println("✅ Found " + consumerRecords.count() +
                        " messages in topic " + topicName);
                break;
            }

            attempts++;
            if (attempts < maxAttempts) {
                System.out.println("⏳ No messages found, attempt " + attempts +
                        " of " + maxAttempts);
            }
        }

        if (consumerRecords != null && consumerRecords.count() > 0) {
            consumer.commitAsync();
        }

        consumer.close();
        return consumerRecords != null ? consumerRecords : ConsumerRecords.empty();
    }

    /**
     * Получение информации о топике (количество сообщений, partitions и т.д.)
     */
    public static Map<String, Object> getTopicInfo(String bootstrapServers, String topicName) {
        String groupId = "info-group-" + System.currentTimeMillis();

        try (KafkaConsumer<String, String> consumer =
                     createConsumer(bootstrapServers, topicName, groupId)) {

            Map<String, Object> info = new HashMap<>();

            // Получаем информацию о partitions
            var partitionsInfo = consumer.partitionsFor(topicName);
            if (partitionsInfo == null || partitionsInfo.isEmpty()) {
                System.err.println("❌ Topic " + topicName + " not found");
                info.put("error", "Topic not found");
                return info;
            }

            // Подписываемся и ждем assignment
            consumer.poll(Duration.ofMillis(500));
            Set<TopicPartition> partitions = consumer.assignment();

            // Улучшенное ожидание assignment
            if (partitions.isEmpty()) {
                int attempts = 0;
                while (partitions.isEmpty() && attempts < 3) {
                    consumer.poll(Duration.ofMillis(500));
                    partitions = consumer.assignment();
                    attempts++;
                }
            }

            if (partitions.isEmpty()) {
                info.put("error", "Could not assign partitions");
                return info;
            }

            // Получаем offsets
            Map<TopicPartition, Long> beginningOffsets = consumer.beginningOffsets(partitions);
            Map<TopicPartition, Long> endOffsets = consumer.endOffsets(partitions);

            List<Map<String, Object>> partitionsData = new ArrayList<>();
            long totalMessages = 0;

            for (TopicPartition partition : partitions) {
                Long beginning = beginningOffsets.get(partition);
                Long end = endOffsets.get(partition);
                long partitionMessages = (end != null && beginning != null) ? end - beginning : 0;

                Map<String, Object> partitionInfo = new HashMap<>();
                partitionInfo.put("partition", partition.partition());
                partitionInfo.put("beginningOffset", beginning);
                partitionInfo.put("endOffset", end);
                partitionInfo.put("messageCount", partitionMessages);

                partitionsData.add(partitionInfo);
                totalMessages += partitionMessages;
            }

            info.put("topic", topicName);
            info.put("partitionCount", partitionsInfo.size());
            info.put("totalMessages", totalMessages);
            info.put("partitions", partitionsData);
            info.put("checkTime", formatTimestamp(System.currentTimeMillis()));

            return info;

        } catch (Exception e) {
            System.err.println("❌ Error getting topic info: " + e.getMessage());
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("error", e.getMessage());
            return errorInfo;
        }
    }
}