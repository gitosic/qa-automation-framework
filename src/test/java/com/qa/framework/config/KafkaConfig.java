package com.qa.framework.config;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SslConfigs;
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
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        // Установка времени таймаута для соединения (полезно для SSL)
        props.put(CommonClientConfigs.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        props.put(CommonClientConfigs.SOCKET_CONNECTION_SETUP_TIMEOUT_MS_CONFIG, 5000);

        // === ДОБАВЛЕНИЕ КОНФИГУРАЦИИ SSL ===
        if (ConfigurationManager.isKafkaSslEnabled()) {
            System.out.println("🔒 Применение настроек SSL для клиента Kafka...");

            // 1. Протокол безопасности
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");

            // 2. Truststore
            props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, ConfigurationManager.getKafkaSslTruststoreLocation());
            props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, ConfigurationManager.getKafkaSslTruststorePassword());

            // 3. Keystore (для аутентификации, если нужно)
            // В нашем случае, Kafka настроен на KAFKA_SSL_CLIENT_AUTH: none, но эти поля все равно нужны для рукопожатия
            props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, ConfigurationManager.getKafkaSslKeystoreLocation());
            props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, ConfigurationManager.getKafkaSslKeystorePassword());

            // 4. Пароль для приватного ключа
            props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, ConfigurationManager.getKafkaSslKeyPassword());

            // 5. Отключение проверки имени хоста (если бы мы использовали localhost:9094)
            // Поскольку мы используем 'kafka', это не обязательно, но полезно для отладки.
            String algo = ConfigurationManager.getKafkaSslEndpointIdentificationAlgorithm();
            if (algo != null) {
                props.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, algo);
            }
        }

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