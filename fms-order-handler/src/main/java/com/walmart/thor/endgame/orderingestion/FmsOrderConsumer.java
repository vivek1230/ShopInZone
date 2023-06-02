package com.walmart.thor.endgame.orderingestion;

import com.walmart.thor.endgame.orderingestion.common.dto.customercancel.FmsCancellationEvent;
import com.walmart.thor.endgame.orderingestion.config.FeatureFlagsConfigWrapper;
import com.walmart.thor.endgame.orderingestion.dto.FmsOrderEvent;
import com.walmart.thor.endgame.orderingestion.exception.FMSOrderException;
import com.walmart.thor.endgame.orderingestion.service.FmsCancellationHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import static com.walmart.thor.endgame.orderingestion.common.constants.Constants.FACILITY_NUM;

@Component
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class FmsOrderConsumer {

  private final FeatureFlagsConfigWrapper featureFlagsConfig;
  private final FmsCancellationHandler fmsCancellationHandler;

  @KafkaListener(
          groupId = "${spring.kafka.consumer.group-id}",
          topics = "${customer-po-topic}",
          containerFactory = "newOrderKafkaFactory")
  public void handleNewFmsOrder(
      @Payload FmsOrderEvent fmsOrderEvent,
      ConsumerRecord<String, String> consumerRecord) throws FMSOrderException {
    String fcId = new String(consumerRecord.headers().lastHeader(FACILITY_NUM).value());
    log.info(
        "Consumed FMS order message, key {},offset: {}, fcid: {}",
        consumerRecord.key(), consumerRecord.offset(), fcId);
    featureFlagsConfig.getFmsOrderHandler(fcId).process(fmsOrderEvent);
  }

  @KafkaListener(
          groupId = "${spring.kafka.consumer.cancel-order-group-id}",
          topics = "${customer-po-topic}",
          containerFactory = "orderCancellationKafkaFactory")
  public void handleFmsOrderCancellation(
          @Payload FmsCancellationEvent fmsCancellationEvent,
          ConsumerRecord<String, String> consumerRecord) {
    log.info(
            "Consumed FMS order cancellation message, key {}, offset: {}",
            consumerRecord.key(),
            consumerRecord.offset());
    if(featureFlagsConfig.isCustomerCancellationEnabled()) {
      fmsCancellationHandler.process(fmsCancellationEvent);
    } else {
      log.info(
              "Feature flag is off for customer cancellation, key {}, offset: {}",
              consumerRecord.key(),
              consumerRecord.offset());
    }
  }
}
