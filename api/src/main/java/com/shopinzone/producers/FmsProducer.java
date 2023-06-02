package com.shopinzone.producers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.walmart.thor.endgame.configs.FmsProducerConfig;
import com.walmart.thor.endgame.fms.FMSOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Slf4j
@Component
public class FmsProducer {

  private final FmsProducerConfig fmsProducerConfig;
  private final ObjectMapper objectMapper;
  KafkaTemplate<String, String> kafkaTemplate;

  @Autowired
  public FmsProducer(FmsProducerConfig fmsProducerConfig, ObjectMapper objectMapper) {
    this.fmsProducerConfig = fmsProducerConfig;
    this.objectMapper = objectMapper;
    this.kafkaTemplate = fmsProducerConfig.getKafkaTemplate();
  }

  public void publishOrders(FMSOrder fmsOrder) throws JsonProcessingException {

    Message<String> message =
        MessageBuilder.withPayload(objectMapper.writeValueAsString(fmsOrder))
            .setHeader(KafkaHeaders.TOPIC, fmsProducerConfig.getTopic())
            .setHeader(KafkaHeaders.MESSAGE_KEY, fmsOrder.getEventPayload().getPurchaseOrderNo())
            .build();
    log.info("FmsOrder payload: {}", message);

    try {
      kafkaTemplate
          .send(message)
          .addCallback(
              new ListenableFutureCallback<>() {

                @Override
                public void onSuccess(SendResult<String, String> obj) {
                  log.info(
                      "pushed msg successfully. Offset: {}, Partition: {}, Metadata: {}",
                      obj.getRecordMetadata().offset(),
                      obj.getRecordMetadata().partition(),
                      obj.getRecordMetadata().toString());
                }

                @Override
                public void onFailure(Throwable throwable) {
                  log.error("failed to push msg. {}", throwable.getMessage());
                }
              });
    } catch (RuntimeException e) {
      log.error("Exception posting kafka message {}", e);
    }
  }
}
