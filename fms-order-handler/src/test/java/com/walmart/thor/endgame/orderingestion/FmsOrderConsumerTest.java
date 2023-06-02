package com.walmart.thor.endgame.orderingestion;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.walmart.thor.endgame.orderingestion.common.SerDesForTest;
import com.walmart.thor.endgame.orderingestion.common.config.ccm.FeatureFlagsConfig;
import com.walmart.thor.endgame.orderingestion.common.dto.customercancel.FmsCancellationEvent;
import com.walmart.thor.endgame.orderingestion.config.FeatureFlagsConfigWrapper;
import com.walmart.thor.endgame.orderingestion.dto.FmsOrderEvent;
import com.walmart.thor.endgame.orderingestion.exception.FMSOrderException;
import com.walmart.thor.endgame.orderingestion.service.FmsCancellationHandler;
import com.walmart.thor.endgame.orderingestion.service.orderhandler.FmsOrderHandler;
import com.walmart.thor.endgame.orderingestion.service.orderhandler.FmsOrderHandlerDefault;
import com.walmart.thor.endgame.orderingestion.service.orderhandler.FmsOrderHandlerInventoryProjection;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.apache.commons.io.IOUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static com.walmart.thor.endgame.orderingestion.common.constants.Constants.FACILITY_NUM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Import(SerDesForTest.class)
public class FmsOrderConsumerTest {

  @MockBean
  private FeatureFlagsConfigWrapper featureFlagsConfigWrapper;

  @MockBean
  private FmsCancellationHandler fmsCancellationHandler;

  @MockBean
  FeatureFlagsConfig featureFlagsConfig;

  @MockBean
  private FmsOrderHandlerDefault fmsOrderHandlerDefault;

  @MockBean
  private FmsOrderHandlerInventoryProjection fmsOrderHandlerInventoryProjection;

  private FmsOrderConsumer fmsOrderConsumerSpy;

  @Mock
  private Map<String, FmsOrderHandler> fmsOrderHandlers;

  @Captor
  private ArgumentCaptor<FmsOrderEvent> EventArgumentCaptor;

  @Captor
  private ArgumentCaptor<FmsCancellationEvent> CancelEventArgumentCaptor;

  private static final String PARTITION_KEY = "test_key";

  @Autowired protected ObjectMapper mapper;

  @BeforeAll
  void setup() {
    var FmsOrderConsumer = new FmsOrderConsumer(featureFlagsConfigWrapper, fmsCancellationHandler);
    fmsOrderConsumerSpy = spy(FmsOrderConsumer);
  }

  protected String getJson(final String src) throws IOException {
    return IOUtils.resourceToString(src, StandardCharsets.UTF_8);
  }

  protected <T> T getEntity(String src, Class<T> clazz) throws IOException {
    return mapper.readValue(getJson(src), clazz);
  }

  @Test
  void testSuccess_001() throws IOException, FMSOrderException {
    var fmsOrderEvent = getEntity("/fmsOrder.json", FmsOrderEvent.class);
    var event = mapper.writeValueAsString(fmsOrderEvent);
    var consumerRecord =
            new ConsumerRecord("", 0, 0L, PARTITION_KEY, mapper.writeValueAsString(event));
    consumerRecord.headers().add(FACILITY_NUM,"7441".getBytes(StandardCharsets.UTF_8));
    when(fmsOrderHandlers.get(anyString())).thenReturn(fmsOrderHandlerDefault);
    when(featureFlagsConfigWrapper.getFeatureFlagsConfig()).thenReturn(featureFlagsConfig);
    when(featureFlagsConfigWrapper.getFmsOrderHandler(anyString())).thenReturn(fmsOrderHandlerInventoryProjection);
    doNothing().when(fmsOrderHandlerInventoryProjection).process(any());

    fmsOrderConsumerSpy.handleNewFmsOrder(fmsOrderEvent, consumerRecord);
    verify(fmsOrderHandlerInventoryProjection, times(1)).process(EventArgumentCaptor.capture());

    var processedEvent = EventArgumentCaptor.getValue();

    assertEquals("66c54789-9583-45e9-9641-4f69c0536613", processedEvent.getEventId());
    assertEquals("FULFILLMENT_ORDER_CREATE", processedEvent.getEventName());
  }

  @Test
  void testSuccess_cancel_001() throws IOException, FMSOrderException {
    FmsCancellationEvent fmsCancellationEvent = getEntity("/fms-cancel-event.json", FmsCancellationEvent.class);
    var event = mapper.writeValueAsString(fmsCancellationEvent);
    var consumerRecord =
            new ConsumerRecord("", 0, 0L, PARTITION_KEY, mapper.writeValueAsString(event));

    when(featureFlagsConfigWrapper.getFeatureFlagsConfig()).thenReturn(featureFlagsConfig);
    when(featureFlagsConfigWrapper.isCustomerCancellationEnabled()).thenReturn(true);
    doNothing().when(fmsCancellationHandler).process(any());

    fmsOrderConsumerSpy.handleFmsOrderCancellation(fmsCancellationEvent, consumerRecord);
    verify(fmsCancellationHandler, times(1)).process(CancelEventArgumentCaptor.capture());

    FmsCancellationEvent processedEvent = CancelEventArgumentCaptor.getValue();

    assertEquals("4871836418414", processedEvent.getPurchaseOrderNo());
    assertEquals("9615", processedEvent.getShipNode().getId());
  }

  @Test
  void testCancelOff_001() throws IOException, FMSOrderException {
    FmsCancellationEvent fmsCancellationEvent = getEntity("/fms-cancel-event.json", FmsCancellationEvent.class);
    var event = mapper.writeValueAsString(fmsCancellationEvent);
    var consumerRecord =
            new ConsumerRecord("", 0, 0L, PARTITION_KEY, mapper.writeValueAsString(event));

    when(featureFlagsConfigWrapper.getFeatureFlagsConfig()).thenReturn(featureFlagsConfig);
    when(featureFlagsConfigWrapper.isCustomerCancellationEnabled()).thenReturn(false);
    doNothing().when(fmsCancellationHandler).process(any());

    fmsOrderConsumerSpy.handleFmsOrderCancellation(fmsCancellationEvent, consumerRecord);
    verify(fmsCancellationHandler, times(0)).process(CancelEventArgumentCaptor.capture());
  }
}
