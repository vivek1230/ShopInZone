package com.walmart.thor.endgame.orderingestion.service.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.walmart.thor.endgame.orderingestion.common.SerDesForTest;
import com.walmart.thor.endgame.orderingestion.common.constants.Constants.FMS_EVENT_NAME;
import com.walmart.thor.endgame.orderingestion.common.dto.customercancel.FmsCancellationEvent;
import com.walmart.thor.endgame.orderingestion.common.dto.customercancel.FmsCancellationEventMessage;
import com.walmart.thor.endgame.orderingestion.common.dto.FmsOrderEvent;
import com.walmart.thor.endgame.orderingestion.consumer.FmsListener;
import com.walmart.thor.endgame.orderingestion.service.FMSCancellationService;
import com.walmart.thor.endgame.orderingestion.service.FmsListenerService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.apache.commons.io.IOUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.walmart.thor.endgame.orderingestion.common.constants.Constants.FMS_EVENT_ID;
import static com.walmart.thor.endgame.orderingestion.common.constants.Constants.FMS_EVENT_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Import(SerDesForTest.class)
public class FmsListenerTest {
  @MockBean
  private FmsListenerService fmsListenerService;

  @MockBean
  private FMSCancellationService fmsCancellationService;

  @Autowired private ObjectMapper objectMapper;

  private FmsListener fmsOrderConsumerSpy;

  @Captor
  private ArgumentCaptor<FmsOrderEvent> EventArgumentCaptor;

  @Captor
  private ArgumentCaptor<FmsCancellationEvent> fmsCancellationEventArgumentCaptor;

  private static final String PARTITION_KEY = "test_key";

  @Autowired
  protected ObjectMapper mapper;

  @BeforeAll
  void setup() {
    var FmsOrderConsumer = new FmsListener(fmsListenerService, fmsCancellationService, objectMapper);
    fmsOrderConsumerSpy = spy(FmsOrderConsumer);
  }

  protected String getJson(final String src) throws IOException {
    return IOUtils.resourceToString(src, StandardCharsets.UTF_8);
  }

  protected <T> T getEntity(String src, Class<T> clazz) throws IOException {
    return mapper.readValue(getJson(src), clazz);
  }

  @Test
  void test_fms_order_event_consumption() throws IOException {
    var fmsOrderEvent = getEntity("/json/fms-order-outbound.json", FmsOrderEvent.class);
    var consumerRecord =
        new ConsumerRecord("", 0, 0L, PARTITION_KEY, mapper.writeValueAsString(fmsOrderEvent));
    consumerRecord.headers().add(new RecordHeader(FMS_EVENT_KEY,
            FMS_EVENT_NAME.FULFILLMENT_ORDER_CREATE.name().getBytes(StandardCharsets.UTF_8)));
    consumerRecord.headers().add(new RecordHeader(FMS_EVENT_ID,
            "eventId".getBytes(StandardCharsets.UTF_8)));
    fmsOrderConsumerSpy.handleFmsOrder(consumerRecord);
    verify(fmsListenerService, times(1)).handle(EventArgumentCaptor.capture());

    var processedEvent = EventArgumentCaptor.getValue();

    assertEquals("092d7a89-da55-454b-8b85-1574016f7588", processedEvent.getEventId());
    assertEquals("FULFILLMENT_ORDER_CREATE", processedEvent.getEventName());
  }

  @Test
  void test_fms_order_event_consumption_no_header() throws IOException {
    var fmsOrderEvent = getEntity("/json/fms-order-outbound.json", FmsOrderEvent.class);
    var consumerRecord =
            new ConsumerRecord("", 0, 0L, PARTITION_KEY, mapper.writeValueAsString(fmsOrderEvent));
    fmsOrderConsumerSpy.handleFmsOrder(consumerRecord);
    verify(fmsListenerService, times(1)).handle(EventArgumentCaptor.capture());

    var processedEvent = EventArgumentCaptor.getValue();

    assertEquals("092d7a89-da55-454b-8b85-1574016f7588", processedEvent.getEventId());
    assertEquals("FULFILLMENT_ORDER_CREATE", processedEvent.getEventName());
  }

  @Test
  void test_fms_cancel_event_consumption() throws IOException {
    var fmsCancelEvent =
            getEntity("/json/fms-cancel-event.json", FmsCancellationEventMessage.class);
    ConsumerRecord consumerRecord =
            new ConsumerRecord("", 0, 0L, PARTITION_KEY, mapper.writeValueAsString(fmsCancelEvent));
    fmsOrderConsumerSpy.handleFmsOrder(consumerRecord);
    verify(fmsCancellationService, times(1))
            .handle(fmsCancellationEventArgumentCaptor.capture());

    FmsCancellationEvent processedEvent = fmsCancellationEventArgumentCaptor.getValue();

    assertEquals("3862309610324", processedEvent.getPurchaseOrderNo());
    assertEquals(1, processedEvent.getFulfillmentLines().size());
  }

  @Test
  void test_fms_order_event_consumption_forej() throws IOException {
    var fmsOrderEvent = getEntity("/json/fms-order-outbound.json", FmsOrderEvent.class);
    var event = mapper.writeValueAsString(fmsOrderEvent);
    var consumerRecord =
        new ConsumerRecord("", 0, 0L, PARTITION_KEY, mapper.writeValueAsString(event));
    fmsOrderConsumerSpy.handleFmsOrder(consumerRecord);
    verify(fmsListenerService, times(1)).sendFOREJ(any(), any());
  }
}
