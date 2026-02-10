package com.qa.framework.tests.kafkaTests;

import com.qa.framework.config.ConfigurationManager;
import com.qa.framework.kafka.KafkaMessage;
import com.qa.framework.kafka.KafkaUtils;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Isolated;

import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Isolated
@Tag("kafka-search")
public class KafkaSearchTest {

    private String bootstrapServers;
    private String testTopic;

    @BeforeAll
    void setup() {
        bootstrapServers = ConfigurationManager.getKafkaBootstrapServers();
        testTopic = ConfigurationManager.getProperty("test.kafka.topic", "test_topic");

        System.out.println("🔧 Настройка теста поиска в Kafka");
        System.out.println("   Сервер: " + bootstrapServers);
        System.out.println("   Топик: " + testTopic);
    }

    @Test
    @DisplayName("Поиск сообщения по testRunId за последние 300 минут")
    void testFindMessageByTestRunId() {
        System.out.println("\n🔍 Тест: поиск сообщения по testRunId");
        String TargetTestRunId = "TEST-1770751225681";

        // 1. Ищем сообщение за последние 300 минут
        Optional<KafkaMessage> foundMessage = KafkaUtils.findMessageByTestRunId(
                bootstrapServers,
                testTopic,
                TargetTestRunId,
                300 // последние 300 минут
        );

        // 2. Проверяем результат
        assertTrue(foundMessage.isPresent(),
                "Сообщение с testRunId = " + TargetTestRunId +
                        " должно быть найдено за последние 300 минут");

        // 3. Выводим детали найденного сообщения
        KafkaUtils.printMessageDetails(foundMessage.get());

        System.out.println("✅ Тест пройден успешно!");
    }

    @Test
    @DisplayName("Проверка существования сообщения по testRunId за последние 300 минут")
    void testHasMessageWithTestRunId() {
        System.out.println("\n🔍 Тест: проверка существования сообщения");
        String TargetTestRunId = "TEST-1770751225681";

        boolean exists = KafkaUtils.hasMessageWithTestRunId(
                bootstrapServers,
                testTopic,
                TargetTestRunId,
                300 // последние 300 минут
        );

        assertTrue(exists,
                "Сообщение с testRunId = " + TargetTestRunId +
                        " должно существовать в топике");

        System.out.println("✅ Сообщение существует: " + exists);
    }

    @Test
    @DisplayName("Поиск сообщения по любому полю JSON за последние 300 минут")
    void testFindMessageByAnyField() {
        System.out.println("\n🔍 Тест: поиск по любому полю JSON");

        // Можно искать по любому полю, например по orderId
        String targetOrderId = "ORD-TX-1770751225681";

        Optional<KafkaMessage> foundMessage = KafkaUtils.findMessageByField(
                bootstrapServers,
                testTopic,
                "orderId",      // имя поля
                targetOrderId,  // значение поля
                300              // последние 300 минут
        );

        assertTrue(foundMessage.isPresent(),
                "Сообщение с orderId = " + targetOrderId +
                        " должно быть найдено");

        System.out.println("✅ Найдено сообщение с orderId: " + targetOrderId);
    }

    @Test
    @DisplayName("Проверка временного диапазона сообщений в топике")
    void testCheckTopicTimeRange() {
        System.out.println("\n📊 Тест: проверка временного диапазона топика");

        var timeRange = KafkaUtils.getTopicTimeRange(
                bootstrapServers,
                testTopic
        );

        assertTrue((Boolean) timeRange.getOrDefault("hasMessages", false),
                "В топике должны быть сообщения");

        System.out.println("📈 Информация о топике:");
        System.out.println("   Количество сообщений: " + timeRange.get("messageCount"));
        System.out.println("   Самое раннее: " + timeRange.get("earliestTime"));
        System.out.println("   Самое позднее: " + timeRange.get("latestTime"));
        System.out.println("   Диапазон: " + timeRange.get("timeRangeMinutes") + " минут");

        System.out.println("✅ Информация о топике получена");
    }
}