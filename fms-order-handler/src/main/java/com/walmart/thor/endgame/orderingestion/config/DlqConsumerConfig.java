package com.walmart.thor.endgame.orderingestion.config;

import com.walmart.thor.endgame.orderingestion.common.config.ccm.DlqProcessingConfig;
import io.strati.ccm.utils.client.annotation.ManagedConfiguration;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DlqConsumerConfig {

    @ManagedConfiguration
    DlqProcessingConfig dlqProcessingConfig;

    @Value("${customer-po-topic}")
    String cpoTopic;
}
