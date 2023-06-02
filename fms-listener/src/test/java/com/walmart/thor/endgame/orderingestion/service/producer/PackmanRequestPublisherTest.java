package com.walmart.thor.endgame.orderingestion.service.producer;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.walmart.thor.endgame.orderingestion.common.config.kafka.EndgameKafkaProducerConfig;
import com.walmart.thor.endgame.orderingestion.common.config.kafka.FMSKafkaProducerConfig;
import com.walmart.thor.endgame.orderingestion.common.config.kafka.HawkeyeKafkaProducerConfig;
import com.walmart.thor.endgame.orderingestion.service.PackmanRequestPublisher;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.messaging.Message;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.concurrent.ListenableFuture;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PackmanRequestPublisherTest {
  private final String testPayloadMessage = "test message";

  private EndgameKafkaProducerConfig kafkaProducerConfig;

  @MockBean
  ProducerFactory producerFactory;

  @MockBean
  Environment environment;

  PackmanRequestPublisher kafkaProducer;

  @Mock
  ListenableFuture listenableFuture;

  @MockBean
  private KafkaTemplate<String, String> kafkaTemplate;
  @Captor
  ArgumentCaptor<Message> messageCaptor;

  @BeforeAll
  void setUp() {
    when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});
  }

  @Test
  public void sendTest() {

    kafkaProducerConfig = new EndgameKafkaProducerConfig();
    kafkaProducer = new PackmanRequestPublisher(kafkaTemplate, "test_topic");
    when(kafkaTemplate.send(any(Message.class))).thenReturn(listenableFuture);
    kafkaProducer.send("test key", "test message", "9610");
    verify(kafkaTemplate, times(1)).send(messageCaptor.capture());
    Message<String> message = messageCaptor.getValue();
    assertEquals(message.getPayload(), testPayloadMessage);
  }
}
