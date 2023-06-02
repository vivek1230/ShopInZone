package com.walmart.thor.endgame.orderingestion.producer;

import com.walmart.thor.endgame.orderingestion.common.config.kafka.FMSKafkaProducerConfig;
import com.walmart.thor.endgame.orderingestion.service.FmsKafkaPublisher;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FMSProducerTest {

  private final String testPayloadMessage = "test message";

  private FMSKafkaProducerConfig kafkaProducerConfig;

  @MockBean ProducerFactory producerFactory;

  @MockBean Environment environment;

  FmsKafkaPublisher fmsKafkaProducer;

  @Mock ListenableFuture listenableFuture;

  @MockBean private KafkaTemplate<String, String> kafkaTemplate;
  @Captor ArgumentCaptor<Message> messageCaptor;

  @BeforeAll
  void setUp() {
    when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});
  }

  @Test
  public void sendTest() {

    kafkaProducerConfig = new FMSKafkaProducerConfig();
    fmsKafkaProducer = new FmsKafkaPublisher(kafkaTemplate, environment, "test_topic");
    when(kafkaTemplate.send(any(Message.class))).thenReturn(listenableFuture);
    fmsKafkaProducer.send("test key", "test message");
    verify(kafkaTemplate, times(1)).send(messageCaptor.capture());
    Message<String> message = messageCaptor.getValue();
    assertEquals(message.getPayload(), testPayloadMessage);
  }
}
