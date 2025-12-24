package com.qa.framework.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class KafkaConfig {

    private final String bootstrapServers;

    public KafkaConfig() {
        this.bootstrapServers = ConfigurationManager.getKafkaBootstrapServers();
    }

    public String getBootstrapServers() {
        return bootstrapServers;
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

    /**
     * Возвращает общие настройки Kafka, включая настройки SSL, если они есть.
     */
    private Properties getCommonKafkaProperties() {
        Properties props = new Properties();
        // Настройки из старого CommonConfig
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Здесь можно добавить настройки SSL, если они есть в ConfigurationManager
        /*
        // Пример добавления SSL
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
        props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, ConfigurationManager.getProperty("ssl.truststore.location"));
        // и т.д.
        */

        return props;
    }

    public Properties getKafkaProducerProperties() {
        Properties kafkaProps = getCommonKafkaProperties();
        kafkaProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers());
        kafkaProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        kafkaProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Настройки для надежности
        kafkaProps.put(ProducerConfig.ACKS_CONFIG, "all");
        kafkaProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        kafkaProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");

        // Настройки из вашего старого KafkaConfig
        kafkaProps.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        kafkaProps.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 5000); // Таймаут для соединения

        return kafkaProps;
    }

    public Properties getKafkaConsumerProperties(String groupId) {
        Properties kafkaProps = getCommonKafkaProperties();
        kafkaProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers());
        kafkaProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        kafkaProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        kafkaProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        // Настройки из вашего старого KafkaConfig
        kafkaProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        kafkaProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
        kafkaProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "10000");
        kafkaProps.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "300000");

        return kafkaProps;
    }

    // Дополнительные методы для удобства - оставлены
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