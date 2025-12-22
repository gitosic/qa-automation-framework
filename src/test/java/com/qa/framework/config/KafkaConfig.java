package com.qa.framework.config;

import com.qa.framework.config.ConfigurationManager;
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
        kafkaProps.put("bootstrap.servers", getBootstrapServers());
        kafkaProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        kafkaProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        kafkaProps.put("acks", "all");
        kafkaProps.put("retries", 3);
        kafkaProps.put("linger.ms", 1);
        return kafkaProps;
    }

    public Properties getKafkaConsumerProperties(String groupId) {
        Properties kafkaProps = new Properties();
        kafkaProps.put("bootstrap.servers", getBootstrapServers());
        kafkaProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaProps.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaProps.put("group.id", groupId);
        kafkaProps.put("auto.offset.reset", "earliest");
        kafkaProps.put("enable.auto.commit", "true");
        kafkaProps.put("max.poll.records", 500);
        kafkaProps.put("session.timeout.ms", "10000");
        return kafkaProps;
    }

    // Дополнительные методы для удобства
    public Properties getKafkaConsumerProperties(String groupId, int maxPollRecords) {
        Properties props = getKafkaConsumerProperties(groupId);
        props.put("max.poll.records", maxPollRecords);
        return props;
    }

    public Properties getKafkaConsumerProperties(String groupId, String autoOffsetReset) {
        Properties props = getKafkaConsumerProperties(groupId);
        props.put("auto.offset.reset", autoOffsetReset);
        return props;
    }
}