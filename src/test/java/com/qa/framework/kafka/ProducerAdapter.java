package com.qa.framework.kafka;

import com.google.gson.Gson;
import com.qa.framework.config.KafkaConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Header;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ProducerAdapter {

    private static final KafkaConfig KAFKA_CONFIG = new KafkaConfig();

    private static KafkaProducer<String, String> createProducer() {
        // Использование централизованного метода из KafkaConfig
        final Properties props = KAFKA_CONFIG.getKafkaProducerProperties();
        return new KafkaProducer<>(props);
    }

    public static boolean sendMessage(String bootstrapServers,
                                      String topicName,
                                      Object message) {

        final KafkaProducer<String, String> producer = createProducer();

        try {
            String jsonMessage = new Gson().toJson(message);
            ProducerRecord<String, String> record = new ProducerRecord<>(topicName, jsonMessage);

            RecordMetadata metadata = producer.send(record).get(10, TimeUnit.SECONDS);

            if (!metadata.hasOffset()) {
                System.err.println("❌ Record has not posted to topic " + topicName);
                return false;
            } else {
                System.out.println("✅ Message was sent to topic " + topicName);
                System.out.println("   Partition: " + metadata.partition());
                System.out.println("   Offset: " + metadata.offset());
                return true;
            }

        } catch (TimeoutException e) {
            System.err.println("❌ Timeout while sending message to Kafka topic " + topicName);
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("❌ Interrupted while sending message to Kafka topic " + topicName);
            return false;
        } catch (ExecutionException e) {
            System.err.println("❌ Failed to send message to Kafka topic " + topicName +
                    ": " + e.getCause().getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("❌ Failed to send message to Kafka topic " + topicName +
                    ": " + e.getMessage());
            return false;
        } finally {
            producer.close();
        }
    }

    public static boolean sendMessageWithHeaders(String bootstrapServers,
                                                 String topicName,
                                                 Object message,
                                                 Map<String, String> headers) {

        final KafkaProducer<String, String> producer = createProducer();

        try {
            String jsonMessage = new Gson().toJson(message);
            ProducerRecord<String, String> record = new ProducerRecord<>(topicName, jsonMessage);

            // Добавляем заголовки
            if (headers != null) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    record.headers().add(header.getKey(), header.getValue().getBytes());
                }
            }

            RecordMetadata metadata = producer.send(record).get(10, TimeUnit.SECONDS);

            if (!metadata.hasOffset()) {
                System.err.println("❌ Record has not posted to topic " + topicName);
                return false;
            } else {
                System.out.println("✅ Message with headers was sent to topic " + topicName);
                if (headers != null && headers.containsKey("X-Transaction-Req-Id")) {
                    System.out.println("   X-Transaction-Req-Id: " +
                            headers.get("X-Transaction-Req-Id"));
                }
                return true;
            }

        } catch (Exception e) {
            System.err.println("❌ Failed to send message with headers to Kafka topic " +
                    topicName + ": " + e.getMessage());
            return false;
        } finally {
            producer.close();
        }
    }

    public static String sendMessageWithTransactionId(String bootstrapServers,
                                                      String topicName,
                                                      Object message) {

        final KafkaProducer<String, String> producer = createProducer();
        String transactionId = UUID.randomUUID().toString();

        try {
            String jsonMessage = new Gson().toJson(message);
            ProducerRecord<String, String> record = new ProducerRecord<>(topicName, jsonMessage);

            // Добавляем заголовки как в вашем коде
            record.headers().add("X-Transaction-Req-Id", transactionId.getBytes());
            record.headers().add("X-Initiator-Service", "test-service".getBytes());

            RecordMetadata metadata = producer.send(record).get(10, TimeUnit.SECONDS);

            if (!metadata.hasOffset()) {
                System.err.println("❌ Record has not posted to topic " + topicName);
                return null;
            } else {
                System.out.println("✅ Message with X-Transaction-Req-Id = " + transactionId +
                        " was sent to topic " + topicName);
                return transactionId;
            }

        } catch (Exception e) {
            System.err.println("❌ Failed to send message with X-Transaction-Req-Id to Kafka topic " +
                    topicName + ": " + e.getMessage());
            return null;
        } finally {
            producer.close();
        }
    }
}