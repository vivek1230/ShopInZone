package com.walmart.thor.endgame.orderingestion.service;

import static com.walmart.thor.endgame.orderingestion.config.KafkaConsumerConfig.FACILITY_NUM;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Component
@Slf4j
public class PackmanRequestPublisher {

  private static final String VERSION = "version";
  private static final int version = 2;
  private final KafkaTemplate<String, String> kafkaTemplate;
  private String packmanInputTopic;

  @Autowired
  public PackmanRequestPublisher(@Qualifier("endgameKafkaProducer") KafkaTemplate<String,
      String> kafkaTemplate,
      @Value("${spring.kafka.producer.packman-input-topic}") String packmanInputTopic) {
    this.kafkaTemplate = kafkaTemplate;
    this.packmanInputTopic = packmanInputTopic;

  }

  public void send(String key, String payLoad, String fcId) {
    var message =
        MessageBuilder.withPayload(payLoad)
            .setHeader(KafkaHeaders.TOPIC, packmanInputTopic)
            .setHeader(KafkaHeaders.MESSAGE_KEY, key)
            .setHeader(FACILITY_NUM, fcId)
            .setHeader(VERSION, version)
            .build();
    if (Objects.nonNull(kafkaTemplate)) {
      ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(message);
      future.addCallback(
          new ListenableFutureCallback<>() {

            @Override
            public void onFailure(Throwable e) {
              log.error(
                  "failed to publish packman request to kafka: message: {}, error: {}", message, e);
            }

            @Override
            public void onSuccess(SendResult<String, String> result) {
              String key = result.getProducerRecord().key();
              log.info("packman request published to kafka, key: {}", key);
            }
          });
    }
  }
}