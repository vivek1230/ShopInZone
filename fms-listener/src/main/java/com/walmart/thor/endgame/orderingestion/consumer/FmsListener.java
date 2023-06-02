package com.walmart.thor.endgame.orderingestion.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.walmart.thor.endgame.orderingestion.common.dto.customercancel.FmsCancellationEventMessage;
import com.walmart.thor.endgame.orderingestion.common.dto.FmsOrderEvent;
import com.walmart.thor.endgame.orderingestion.service.FMSCancellationService;
import com.walmart.thor.endgame.orderingestion.service.FmsListenerService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class FmsListener {

  private final FmsListenerService fmsListenerService;
  private final FMSCancellationService cancellationService;
  private final ObjectMapper mapper;


  @KafkaListener(groupId = "${spring.kafka.consumer.group-id}",
          topics = "${spring.kafka.consumer.fms-consumer-topic}")
  public void handleFmsOrder(ConsumerRecord<String, String> consumerRecord)
      throws JsonProcessingException {
    var recordKey = consumerRecord.key();
    log.info(
            "Consumed FMS order message, key {}, offset {}, partition: {}",
            recordKey, consumerRecord.offset(), consumerRecord.partition());
    try {
      FmsOrderEvent fmsOrderEvent = mapper.readValue(consumerRecord.value(), FmsOrderEvent.class);

      if(!Objects.isNull(fmsOrderEvent.getEventPayload())) {
        log.info("Processing FMS order create message, key {}", consumerRecord.key());
        fmsListenerService.handle(fmsOrderEvent);
      } else {
        FmsCancellationEventMessage fmsCancellationEventMessage =
                mapper.readValue(consumerRecord.value(), FmsCancellationEventMessage.class);
        if(Objects.isNull(fmsCancellationEventMessage.getPayload())) {
          log.info("Found null payload for consumer record with key:{}, offset:{} and partition: {}",
                  consumerRecord.key(),consumerRecord.offset(), consumerRecord.partition());
          return;
        }
        log.info("Processing FMS order cancel message, eventid {}, key: {}",
                fmsCancellationEventMessage.getHeader().getEventID(), consumerRecord.key());
        cancellationService.handle(fmsCancellationEventMessage.getPayload());
      }
    } catch (JsonProcessingException e) {
      // in-case of un-parseable message log the issue, send status to fms and get out of the function.
      log.error("FMS Order Message Parsing failed for key:{}, body:{}, error: {}",
              recordKey, consumerRecord.value(), e.getMessage());
      fmsListenerService.sendFOREJ(consumerRecord.key(), consumerRecord.value());
    }
  }
}
