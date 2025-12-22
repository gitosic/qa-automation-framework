package com.qa.framework.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class KafkaConfig {

    public String getBootstrapServers() {
        return ConfigurationManager.getKafkaBootstrapServers();
    }

    public String getIncomingOrdersTopic() {
        return ConfigurationManager.getKafkaIncomingOrdersTopic();
    }

    public String getUserActivitiesTopic() {
        return ConfigurationManager.getKafkaUserActivitiesTopic();
    }

    public String getSystemLogsTopic() {
        return ConfigurationManager.getKafkaSystemLogsTopic();
    }

    public Properties getKafkaProducerProperties() {
        Properties kafkaProps = new Properties();
        kafkaProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers());
        kafkaProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        kafkaProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        kafkaProps.put(ProducerConfig.ACKS_CONFIG, "all");
        kafkaProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        kafkaProps.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        kafkaProps.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 5000); // Таймаут для соединения
        return kafkaProps;
    }

    public Properties getKafkaConsumerProperties(String groupId) {
        Properties kafkaProps = new Properties();
        kafkaProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers());
        kafkaProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        kafkaProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        kafkaProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        kafkaProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        kafkaProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        kafkaProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
        kafkaProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "10000");
        kafkaProps.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "300000");
        return kafkaProps;
    }

    // Дополнительные методы для удобства
    public Properties getKafkaConsumerProperties(String groupId, int maxPollRecords) {
        Properties props = getKafkaConsumerProperties(groupId);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        return props;
    }

    public Properties getKafkaConsumerProperties(String groupId, String autoOffsetReset) {
        Properties props = getKafkaConsumerProperties(groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        return props;
    }
}