package com.qa.framework.kafka;

import java.util.Properties;

public class CommonConfig {

    public static Properties getKafkaProperties(){
        Properties props = new Properties();
        // Основные настройки как в вашем коде
        props.put("auto.offset.reset", "earliest");

        // SSL настройки (если нужно)
        // Если ваша Kafka использует SSL, раскомментируйте и настройте:
        /*
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
        props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, "path/to/truststore.jks");
        props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "password");
        props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, "path/to/keystore.jks");
        props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, "password");
        props.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, "PKCS12");
        */

        return props;
    }
}