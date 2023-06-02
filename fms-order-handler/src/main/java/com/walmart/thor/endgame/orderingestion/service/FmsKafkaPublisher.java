package com.walmart.thor.endgame.orderingestion.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.Objects;

@Component
@Slf4j
public class FmsKafkaPublisher {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private String fmsStatusTopic;
  private Environment environment;

  @Autowired
  public FmsKafkaPublisher(
      @Qualifier("fmsKafkaProducer") KafkaTemplate<String, String> kafkaTemplate,
      Environment environment,
      @Value("${spring.kafka.producer.fms-producer-topic}") String fmsStatusTopic) {
    this.kafkaTemplate = kafkaTemplate;
    this.fmsStatusTopic = fmsStatusTopic;
    this.environment = environment;
  }

  public void send(String key, String payLoad) {
    var message =
        MessageBuilder.withPayload(payLoad)
            .setHeader(KafkaHeaders.TOPIC, fmsStatusTopic)
            .setHeader(KafkaHeaders.MESSAGE_KEY, key)
            // this is temporary, only for manual testing support.
            .setHeader("profile", this.environment.getActiveProfiles()[0])
            .build();
    if (Objects.nonNull(kafkaTemplate)) {
      ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(message);
      future.addCallback(
          new ListenableFutureCallback<>() {

            @Override
            public void onFailure(Throwable e) {
              log.error(
                  "failed to publish fmsorder status to kafka: message: {}, error: {}", message, e);
            }

            @Override
            public void onSuccess(SendResult<String, String> result) {
              String key = result.getProducerRecord().key();
              log.info("fmsorder status published to kafka, key: {}", key);
            }
          });
    }
  }
}
