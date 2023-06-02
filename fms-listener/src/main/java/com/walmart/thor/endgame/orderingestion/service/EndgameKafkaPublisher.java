package com.walmart.thor.endgame.orderingestion.service;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Slf4j
@Component
public class EndgameKafkaPublisher {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private String customerOrderTopic;

  @Autowired
  public EndgameKafkaPublisher(@Qualifier("endgameKafkaProducer") KafkaTemplate<String,
      String> kafkaTemplate, @Value("${spring.kafka.producer.endgame-producer-topic}") String customerOrderTopic){
    this.kafkaTemplate = kafkaTemplate;
    this.customerOrderTopic = customerOrderTopic;

  }

  public void send(String key, String payLoad, MessageHeaderAccessor headers) {
      headers.setHeader(KafkaHeaders.TOPIC, customerOrderTopic);
      headers.setHeader(KafkaHeaders.MESSAGE_KEY, key);
    var message =
        MessageBuilder.withPayload(payLoad)
            .setHeaders(headers)
            .build();
    if (Objects.nonNull(kafkaTemplate)) {
      ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(message);
      future.addCallback(
          new ListenableFutureCallback<>() {

            @Override
            public void onFailure(Throwable e) {
              log.error(
                  "failed to publish fmsorder to kafka: message: {}, error: {}", message, e);
            }

            @Override
            public void onSuccess(SendResult<String, String> result) {
              String key = result.getProducerRecord().key();
              log.info("fmsorder published to kafka, key: {}", key);
            }
          });
    }
  }
}
