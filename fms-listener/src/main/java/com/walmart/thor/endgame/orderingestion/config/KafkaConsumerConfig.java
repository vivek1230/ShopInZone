package com.walmart.thor.endgame.orderingestion.config;

import com.walmart.thor.endgame.orderingestion.common.config.kafka.BaseKafkaConsumerConfig;

import java.util.List;
import java.util.Map;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.MicrometerConsumerListener;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import static com.walmart.thor.endgame.orderingestion.common.constants.Constants.GROUP_ID_TAG;

@EnableKafka
@Configuration
public class KafkaConsumerConfig extends BaseKafkaConsumerConfig {

  public static final String FACILITY_NUM = "facilityNum";

  @Value("${spring.kafka.fms-enable-ssl}")
  private boolean isSSLEnabled;

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, String>
      kafkaListenerContainerFactory(
          final DefaultErrorHandler errorHandler, final KafkaProperties properties, final MeterRegistry meterRegistry) {
    Map<String, Object> consumerConfig = getConsumerConfig(properties, isSSLEnabled);
    var consumerFactory =
        new DefaultKafkaConsumerFactory<>(
            consumerConfig,
            new StringDeserializer(),
            new ErrorHandlingDeserializer<>(new StringDeserializer()));


    consumerFactory.addListener(new MicrometerConsumerListener<>(meterRegistry,
            List.of(Tag.of(GROUP_ID_TAG, groupID))));
    ConcurrentKafkaListenerContainerFactory<String, String> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory);
    factory.setCommonErrorHandler(errorHandler);
    return factory;
  }
}
