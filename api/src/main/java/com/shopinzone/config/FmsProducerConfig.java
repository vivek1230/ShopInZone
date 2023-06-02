package com.shopinzone.config;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
@ConfigurationProperties(prefix = "fms.producer")
@Getter
@Setter
@Slf4j
public class FmsProducerConfig {

  private static final String SSL = "SSL";
  private static final String JKS = "JKS";

  private String topics;
  private String bootstrapServers;
  private boolean enableSslKafka;
  private String sslCertLocation;
  private String sslKeyPassword;
  private boolean enableBatchKafka;

  @Value("${fms.producer.batch.acks}")
  private String acks;

  @Value("${fms.producer.batch.lingerMS}")
  private String lingerMS;

  @Value("${fms.producer.batch.batchSize}")
  private String batchSize;

  @Value("${fms.producer.batch.compressionType}")
  private String compressionType;

  public KafkaTemplate<String, String> getKafkaTemplate() {

    Map<String, Object> producerConfig = getProducerConfig();
    DefaultKafkaProducerFactory<String, String> producerFactory =
        new DefaultKafkaProducerFactory<>(producerConfig);
    return new KafkaTemplate<>(producerFactory);
  }

  public Map<String, Object> getProducerConfig() {

    Map<String, Object> producerConfig = new HashMap<>();
    producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, getBrokers());
    producerConfig.put(
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    producerConfig.put(
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

    if (enableBatchKafka) {

      // Batch Config
      producerConfig.put(ProducerConfig.ACKS_CONFIG, acks);
      producerConfig.put(ProducerConfig.LINGER_MS_CONFIG, lingerMS);
      producerConfig.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
      producerConfig.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, compressionType);
    }

    if (!enableSslKafka) {
      return producerConfig;
    }

    // SSL Config
    producerConfig.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SSL);
    producerConfig.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, sslKeyPassword);
    producerConfig.put(SslConfigs.SSL_PROTOCOL_CONFIG, SSL);
    producerConfig.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, sslCertLocation);
    producerConfig.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, sslKeyPassword);
    producerConfig.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, JKS);
    producerConfig.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, sslCertLocation);
    producerConfig.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, sslKeyPassword);
    producerConfig.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, JKS);
    return producerConfig;
  }

  private String getBrokers() {
    return bootstrapServers;
  }

  public String getTopic() {
    return topics;
  }
}
