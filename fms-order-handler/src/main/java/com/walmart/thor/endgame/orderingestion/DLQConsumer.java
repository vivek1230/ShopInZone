package com.walmart.thor.endgame.orderingestion;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.walmart.thor.endgame.orderingestion.FmsOrderConsumer;
import com.walmart.thor.endgame.orderingestion.config.DlqConsumerConfig;
import com.walmart.thor.endgame.orderingestion.config.FCDetailsConfig;
import com.walmart.thor.endgame.orderingestion.dto.FmsOrderEvent;
import com.walmart.thor.endgame.orderingestion.exception.FMSOrderException;
import io.strati.ccm.utils.client.annotation.PostRefresh;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DLQConsumer {

    public static final String ORIGINAL_TOPIC_HEADER = "kafka_dlt-original-topic";

    private DlqConsumerConfig dlqConsumerConfig;
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;
    private FmsOrderConsumer fmsOrderConsumer;
    private ObjectMapper objectMapper;


    @KafkaListener(
            id="dlq_fms_listener_id",
            autoStartup = "false",
            groupId = "${spring.kafka.dlq-consumer.group-id}",
            topics = "${spring.kafka.dlq-consumer.topic}",
            containerFactory = "dlqKafkaListenerContainerFactory")
    public void handleDlqInventoryUpdate(final ConsumerRecord<String, String> consumerRecord)
            throws FMSOrderException, JsonProcessingException {
        log.info("Starting DLQ processing for fms-order-handler consumer");
        String originalTopic = new String(consumerRecord.headers().lastHeader(ORIGINAL_TOPIC_HEADER).value());
        if(originalTopic.equals(dlqConsumerConfig.getCpoTopic())) {
            log.info("processing customer purchase order DLQ message for key: {} ", consumerRecord.key());
            FmsOrderEvent fmsOrderEvent =
                    objectMapper.readValue(consumerRecord.value(), FmsOrderEvent.class);
            fmsOrderConsumer.handleNewFmsOrder(fmsOrderEvent, consumerRecord);
        }
    }

    @PostRefresh(configName = "ALL")
    public void onConfigUpdate() {
        log.info("CCM2 config dlqProcessingConfig fms-order-handler changed to {}",
                dlqConsumerConfig.getDlqProcessingConfig().enableDlqConsumer());

        if(dlqConsumerConfig.getDlqProcessingConfig().enableDlqConsumer()) {
            startDlqListener();
        } else {
            stopDlqListener();
        }
    }

    public void startDlqListener() {
        kafkaListenerEndpointRegistry.getListenerContainer("dlq_fms_listener_id").start();
        log.info("DLQ Listener Started");
    }

    public void stopDlqListener() {
        kafkaListenerEndpointRegistry.getListenerContainer("dlq_fms_listener_id").stop(()->{
            log.info("DLQ Listener Stopped.");
        });
    }
}
