package com.qa.framework.kafka;

import lombok.Builder;
import lombok.Getter;
import org.apache.kafka.common.header.Header;

@Getter
@Builder
public class KafkaMessage {
    private Header header;
    private String body;
    private int partition;
    private long offset;
    private long timestamp;
    private String key;
    private String topic;

    @Override
    public String toString() {
        return String.format("KafkaMessage{topic='%s', partition=%d, offset=%d, body='%s...'}",
                topic, partition, offset,
                body.length() > 50 ? body.substring(0, 50) : body);
    }
}