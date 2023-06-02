package com.walmart.thor.endgame.orderingestion.config;

import com.walmart.thor.endgame.orderingestion.common.config.kafka.BaseKafkaConsumerConfig;
import com.walmart.thor.endgame.orderingestion.common.config.kafka.KafkaSecuredConfig;
import com.walmart.thor.endgame.orderingestion.common.config.serdes.SerDesConfig;
import com.walmart.thor.endgame.orderingestion.common.constants.Constants;
import com.walmart.thor.endgame.orderingestion.common.dto.customercancel.FmsCancellationEvent;
import com.walmart.thor.endgame.orderingestion.dto.FmsOrderEvent;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.MicrometerConsumerListener;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static com.walmart.thor.endgame.orderingestion.common.constants.Constants.FMS_EVENT_KEY;
import static com.walmart.thor.endgame.orderingestion.common.constants.Constants.GROUP_ID_TAG;

@EnableKafka
@Configuration
@Import({FCDetailsConfig.class})
@Slf4j
public class KafkaConsumerConfig extends BaseKafkaConsumerConfig {

  @Value("${spring.kafka.endgame-enable-ssl}")
  private boolean isSSLEnabled;

  @Value("${spring.kafka.consumer.cancel-order-group-id}")
  protected String cancelOrderGroupID;

  @Value("${spring.kafka.dlq-consumer.bootstrap-servers}")
  private String dlqBrokers;

  @Autowired
  private FCDetailsConfig fcDetailsConfig;

  public static final String FACILITY_NUM = "facilityNum";

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, FmsOrderEvent>
      newOrderKafkaFactory(
          final DefaultErrorHandler errorHandler, final KafkaProperties properties, final MeterRegistry meterRegistry) {

    Map<String, Object> consumerConfig = getConsumerConfig(properties, isSSLEnabled);
    var consumerFactory =
        new DefaultKafkaConsumerFactory<>(
            consumerConfig,
            new StringDeserializer(),
            new ErrorHandlingDeserializer<>(new JsonDeserializer<>(FmsOrderEvent.class)));
    consumerFactory.addListener(new MicrometerConsumerListener<>(meterRegistry,
            List.of(Tag.of(GROUP_ID_TAG, groupID))));
    ConcurrentKafkaListenerContainerFactory<String, FmsOrderEvent> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory);
    factory.setRecordFilterStrategy(
      consumerRecord -> {
        try {
          var eventType =
                  new String(
                          consumerRecord.headers().lastHeader(FMS_EVENT_KEY).value(),
                          StandardCharsets.UTF_8);
          return !Constants.FMS_EVENT_NAME.FULFILLMENT_ORDER_CREATE.name().equals(eventType);
        } catch (NullPointerException e) {
          log.info("eventName header not found in FMS message, defaulting to create order {}",
                  e.getMessage());
          return false;
        }
      });
    factory.setCommonErrorHandler(errorHandler);
    return factory;
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, FmsCancellationEvent>
  orderCancellationKafkaFactory(
          final DefaultErrorHandler errorHandler, final KafkaProperties properties, final MeterRegistry meterRegistry) {

    Map<String, Object> consumerConfig = getConsumerConfig(properties, isSSLEnabled);
    var consumerFactory =
            new DefaultKafkaConsumerFactory<>(
                    consumerConfig,
                    new StringDeserializer(),
                    new ErrorHandlingDeserializer<>(new JsonDeserializer<>(FmsCancellationEvent.class)));
    consumerFactory.addListener(new MicrometerConsumerListener<>(meterRegistry,
            List.of(Tag.of(GROUP_ID_TAG, cancelOrderGroupID))));
    ConcurrentKafkaListenerContainerFactory<String, FmsCancellationEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory);
    factory.setRecordFilterStrategy(
      consumerRecord -> {
        try {
          var eventType =
                  new String(
                          consumerRecord.headers().lastHeader(FMS_EVENT_KEY).value(),
                          StandardCharsets.UTF_8);
          return !Constants.FMS_EVENT_NAME.FULFILLMENT_ORDER_CANCEL.name().equals(eventType);
        } catch (NullPointerException e) {
          log.info("eventName header not found in FMS message, defaulting to create order {}",
                  e.getMessage());
          return true;
        }
      });
    factory.setCommonErrorHandler(errorHandler);
    return factory;
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, String> dlqKafkaListenerContainerFactory(
          final DefaultErrorHandler errorHandler,
          final KafkaProperties properties,
          final MeterRegistry meterRegistry) {

    Map<String, Object> consumerConfig = getConsumerConfig(properties, isSSLEnabled);
    consumerConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, dlqBrokers);
    var consumerFactory =
            new DefaultKafkaConsumerFactory<>(
                    consumerConfig, new StringDeserializer(), new StringDeserializer());
    consumerFactory.addListener(
            new MicrometerConsumerListener<>(meterRegistry, List.of(Tag.of(GROUP_ID_TAG, groupID))));
    ConcurrentKafkaListenerContainerFactory<String, String> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory);
    factory.setRecordFilterStrategy(
            consumerRecord -> {
              var eventFcNumber =
                      new String(
                              consumerRecord.headers().lastHeader(FACILITY_NUM).value(),
                              StandardCharsets.UTF_8);
              return !fcDetailsConfig.getShipNodeToFcidMap().containsValue(eventFcNumber);
            });
    factory.setCommonErrorHandler(errorHandler);
    return factory;
  }
}
