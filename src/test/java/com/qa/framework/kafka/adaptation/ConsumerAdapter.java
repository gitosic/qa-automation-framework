package com.qa.framework.kafka.adaptation;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.*;

public class ConsumerAdapter {

    private static final int NUMBER_LAST_MESSAGE = 6;

    private static KafkaConsumer<String, String> createConsumer(String bootstrapServers,
                                                                String topicName,
                                                                String groupName) {
        final Properties props = CommonConfig.getKafkaProperties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupName);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());

        final KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList(topicName));

        return consumer;
    }

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
                consumerRecords = consumer.poll(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (consumerRecords.count() == 0) {
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
        return consumerRecords;
    }

    public static List<KafkaMessage> convertRecordsToMessageObject(
            ConsumerRecords<String, String> consumerRecords) {

        List<KafkaMessage> result = new ArrayList<>();

        consumerRecords.forEach(record -> {
            Headers consumedHeaders = record.headers();
            Header headerPrepTransactReqId = null;

            for (Header header : consumedHeaders) {
                if (header.key().equals("X-Prepare-Transaction-Req-Id")) {
                    headerPrepTransactReqId = header;
                }
            }

            KafkaMessage msg = KafkaMessage.builder()
                    .header(headerPrepTransactReqId)
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

    public static SortedMap<Long, String> convertRecordsToMessageSortedByTimestamp(
            ConsumerRecords<String, String> consumerRecords) {

        SortedMap<Long, String> map = new TreeMap<>();
        consumerRecords.forEach(record -> {
            map.put(record.timestamp(), record.value());
        });

        return map;
    }

    // Метод для чтения сообщений с ожиданием (по аналогии с вашим кодом)
    public static ConsumerRecords<String, String> readMessagesWithWait(
            String bootstrapServers,
            String topicName,
            String groupName,
            int maxWaitSeconds) {

        final KafkaConsumer<String, String> consumer =
                createConsumer(bootstrapServers, topicName, groupName);

        ConsumerRecords<String, String> consumerRecords = null;
        int attempts = 0;
        int maxAttempts = maxWaitSeconds * 2; // Каждые 0.5 секунды

        while (attempts < maxAttempts) {
            consumerRecords = consumer.poll(500); // 0.5 секунды

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

        consumer.close();
        return consumerRecords != null ? consumerRecords : ConsumerRecords.empty();
    }
}