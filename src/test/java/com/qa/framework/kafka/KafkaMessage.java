package com.qa.framework.kafka;

import lombok.Builder;
import lombok.Getter;
import org.apache.kafka.common.header.Header;

import java.util.List;

@Getter
@Builder
public class KafkaMessage {
    private List<Header> headers; // Изменено на List<Header>
    private String body;
    private int partition;
    private long offset;
    private long timestamp;
    private String key;
    private String topic;

    @Override
    public String toString() {
        return String.format("KafkaMessage{topic='%s', partition=%d, offset=%d, headers=%d, body='%s...'}",
                topic, partition, offset,
                headers != null ? headers.size() : 0,
                body.length() > 50 ? body.substring(0, 50) : body);
    }

    // Вспомогательный метод для получения конкретного заголовка, если нужно
    public Header getHeader(String key) { // Требует аргумент (ключ)
        if (headers == null) return null;
        return headers.stream()
                .filter(h -> h.key().equals(key))
                .findFirst()
                .orElse(null);
    }
}