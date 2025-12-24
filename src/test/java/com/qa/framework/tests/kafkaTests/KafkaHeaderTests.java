package com.qa.framework.tests.kafkaTests;

import com.qa.framework.config.ConfigurationManager;
import com.qa.framework.kafka.ConsumerAdapter;
import com.qa.framework.kafka.KafkaMessage;
import com.qa.framework.kafka.ProducerAdapter;
import org.apache.kafka.common.header.Header;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("kafka-headers")
public class KafkaHeaderTests {

    private String bootstrapServers;
    private String testTopic;

    @BeforeAll
    void setup() {
        bootstrapServers = ConfigurationManager.getKafkaBootstrapServers();
        testTopic = ConfigurationManager.getProperty("test.kafka.topic", "test_headers_topic");
    }

    @Test
    @DisplayName("Тест отправки сообщения с заголовком X-Prepare-Transaction-Req-Id")
    void testPrepareTransactionHeader() {
        System.out.println("\n🧾 Тест заголовка X-Prepare-Transaction-Req-Id");

        Map<String, Object> message = new HashMap<>();
        message.put("requestId", "REQ-" + System.currentTimeMillis());
        message.put("applicationId", "APP-001");
        message.put("type", "PREPARE");

        Map<String, String> headers = new HashMap<>();
        String transactionId = "TX-" + System.currentTimeMillis();
        headers.put("X-Prepare-Transaction-Req-Id", transactionId);
        headers.put("X-Initiator-Service", "test-adaptation-service");

        boolean sent = ProducerAdapter.sendMessageWithHeaders(
                bootstrapServers,
                testTopic,
                message,
                headers
        );

        assertTrue(sent, "Сообщение с transaction header должно быть отправлено");
        System.out.println("✅ Отправлено с Transaction ID: " + transactionId);
    }

    @Test
    @DisplayName("Тест чтения сообщений с различными заголовками")
    void testReadingMessagesWithVariousHeaders() {
        System.out.println("\n📋 Тест чтения сообщений с различными заголовками");

        // Отправляем несколько сообщений с разными заголовками
        for (int i = 1; i <= 3; i++) {
            Map<String, Object> message = new HashMap<>();
            message.put("messageNumber", i);
            message.put("content", "Тестовое сообщение " + i);

            Map<String, String> headers = new HashMap<>();
            headers.put("X-Message-Type", "TEST-" + i);
            headers.put("X-Sequence-Number", String.valueOf(i));
            headers.put("X-Test-Header", "Value" + i);

            ProducerAdapter.sendMessageWithHeaders(
                    bootstrapServers,
                    testTopic,
                    message,
                    headers
            );
        }

        // Даем время на доставку
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Читаем сообщения
        String groupId = "header-test-group-" + System.currentTimeMillis();
        var consumerRecords = ConsumerAdapter.readMessage(
                bootstrapServers,
                testTopic,
                groupId
        );

        assertTrue(consumerRecords.count() >= 3,
                "Должно найти хотя бы 3 сообщения с заголовками");

        // Конвертируем для проверки
        var messages = ConsumerAdapter.convertRecordsToMessageObject(consumerRecords);

        System.out.println("Найдено сообщений с заголовками: " + messages.size());
        AtomicInteger messageCount = new AtomicInteger(0);

        messages.forEach(msg -> {
            if (msg.getHeaders() != null && !msg.getHeaders().isEmpty()) {
                messageCount.incrementAndGet();
                System.out.println("   Сообщение " + messageCount.get() + " (Заголовков: " + msg.getHeaders().size() + "):");
                for (Header header : msg.getHeaders()) {
                    System.out.println("      -> " + header.key() +
                            " = " + new String(header.value()));
                }
            }
        });

        assertTrue(messageCount.get() >= 3, "Должно найти хотя бы 3 сообщения, содержащих заголовки");


        System.out.println("✅ Успешно прочитаны сообщения с различными заголовками");
    }
}