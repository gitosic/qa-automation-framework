package com.qa.framework.testcontainers.kafkaTests;

import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Конфигурация Kafka контейнера для тестов.
 *
 * Используем образ confluentinc/cp-kafka (официальный образ от Confluent)
 * с поддержкой KRaft (режим без ZooKeeper) для более быстрого запуска.
 */
public class KafkaTestContainerConfig {

    /**
     * Создаёт и настраивает Kafka контейнер
     *
     * @return настроенный KafkaContainer
     */
    public static KafkaContainer createContainer() {
        return new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"))
                .withKraft()  // Используем KRaft (без ZooKeeper) - быстрее запуск
                .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true")  // Автосоздание топиков
                .withEnv("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", "1")  // Для тестов достаточно 1
                .withEnv("KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR", "1")
                .withEnv("KAFKA_TRANSACTION_STATE_LOG_MIN_ISR", "1");
    }

    /**
     * Получить строку подключения к Kafka брокеру
     *
     * @param container запущенный Kafka контейнер
     * @return bootstrap servers в формате "host:port"
     */
    public static String getBootstrapServers(KafkaContainer container) {
        return container.getBootstrapServers();
    }
}