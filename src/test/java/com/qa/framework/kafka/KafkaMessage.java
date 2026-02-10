package com.qa.framework.kafka;

import org.apache.kafka.common.header.Header;

import java.util.List;
import java.util.Objects;

public class KafkaMessage {
    private List<Header> headers;
    private String body;
    private int partition;
    private long offset;
    private long timestamp;
    private String key;
    private String topic;

    // Конструкторы
    public KafkaMessage() {
    }

    public KafkaMessage(List<Header> headers, String body, int partition, long offset, long timestamp, String key, String topic) {
        this.headers = headers;
        this.body = body;
        this.partition = partition;
        this.offset = offset;
        this.timestamp = timestamp;
        this.key = key;
        this.topic = topic;
    }

    // Getters
    public List<Header> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public int getPartition() {
        return partition;
    }

    public long getOffset() {
        return offset;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getKey() {
        return key;
    }

    public String getTopic() {
        return topic;
    }

    // Setters
    public void setHeaders(List<Header> headers) {
        this.headers = headers;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    @Override
    public String toString() {
        return String.format("KafkaMessage{topic='%s', partition=%d, offset=%d, headers=%d, body='%s...'}",
                topic, partition, offset,
                headers != null ? headers.size() : 0,
                body != null && body.length() > 50 ? body.substring(0, 50) : body);
    }

    // Вспомогательный метод для получения конкретного заголовка
    public Header getHeader(String key) {
        if (headers == null) return null;
        return headers.stream()
                .filter(h -> h.key().equals(key))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KafkaMessage that = (KafkaMessage) o;
        return partition == that.partition &&
                offset == that.offset &&
                timestamp == that.timestamp &&
                Objects.equals(headers, that.headers) &&
                Objects.equals(body, that.body) &&
                Objects.equals(key, that.key) &&
                Objects.equals(topic, that.topic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(headers, body, partition, offset, timestamp, key, topic);
    }

    // Builder pattern (ручная реализация)
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<Header> headers;
        private String body;
        private int partition;
        private long offset;
        private long timestamp;
        private String key;
        private String topic;

        public Builder headers(List<Header> headers) {
            this.headers = headers;
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public Builder partition(int partition) {
            this.partition = partition;
            return this;
        }

        public Builder offset(long offset) {
            this.offset = offset;
            return this;
        }

        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public Builder topic(String topic) {
            this.topic = topic;
            return this;
        }

        public KafkaMessage build() {
            return new KafkaMessage(headers, body, partition, offset, timestamp, key, topic);
        }
    }
}