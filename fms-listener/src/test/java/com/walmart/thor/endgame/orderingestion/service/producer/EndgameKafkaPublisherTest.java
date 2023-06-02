package com.walmart.thor.endgame.orderingestion.service.producer;

import static com.walmart.thor.endgame.orderingestion.common.constants.Constants.FMS_EVENT_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.walmart.thor.endgame.orderingestion.common.config.kafka.EndgameKafkaProducerConfig;
import com.walmart.thor.endgame.orderingestion.common.config.kafka.FMSKafkaProducerConfig;
import com.walmart.thor.endgame.orderingestion.common.constants.Constants;
import com.walmart.thor.endgame.orderingestion.service.EndgameKafkaPublisher;
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
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.concurrent.ListenableFuture;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EndgameKafkaPublisherTest {

  private final String testPayloadMessage = "test message";

  private EndgameKafkaProducerConfig kafkaProducerConfig;

  @MockBean
  ProducerFactory producerFactory;

  @MockBean
  Environment environment;

  EndgameKafkaPublisher kafkaProducer;

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
    kafkaProducer = new EndgameKafkaPublisher(kafkaTemplate, "test_topic");
    when(kafkaTemplate.send(any(Message.class))).thenReturn(listenableFuture);
    MessageHeaderAccessor headers = new MessageHeaderAccessor();
    headers.setHeader("testHeaderKey", "testHeaderValue");
    kafkaProducer.send("test key", "test message", headers);
    verify(kafkaTemplate, times(1)).send(messageCaptor.capture());
    Message<String> message = messageCaptor.getValue();
    assertEquals(message.getPayload(), testPayloadMessage);
    assertEquals(message.getHeaders().get("testHeaderKey"), "testHeaderValue");
  }
}
