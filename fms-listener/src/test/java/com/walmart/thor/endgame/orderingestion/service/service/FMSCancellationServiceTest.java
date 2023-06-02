package com.walmart.thor.endgame.orderingestion.service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.walmart.thor.endgame.orderingestion.common.SerDesForTest;
import com.walmart.thor.endgame.orderingestion.common.dao.FmsOrderDAO;
import com.walmart.thor.endgame.orderingestion.common.dto.customercancel.FmsCancellationEvent;
import com.walmart.thor.endgame.orderingestion.common.dao.FmsOrderDAO.FMSOrderStatus;
import com.walmart.thor.endgame.orderingestion.common.dto.FmsOrderEvent;
import com.walmart.thor.endgame.orderingestion.common.repository.FMSOrderRepository;
import com.walmart.thor.endgame.orderingestion.config.FmsListenerConfig;
import com.walmart.thor.endgame.orderingestion.service.EndgameKafkaPublisher;
import com.walmart.thor.endgame.orderingestion.service.FMSCancellationService;
import com.walmart.thor.endgame.orderingestion.service.MicrometerService;
import io.micrometer.core.instrument.Counter;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.walmart.thor.endgame.orderingestion.common.constants.Constants.FMS_EVENT_NAME;
import static com.walmart.thor.endgame.orderingestion.common.constants.Constants.FMS_EVENT_KEY;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Import(SerDesForTest.class)
public class FMSCancellationServiceTest {

    FMSCancellationService fmsCancellationServiceSpy;
    MicrometerService micrometerService = Mockito.mock(MicrometerService.class);
    EndgameKafkaPublisher endgameKafkaPublisher = Mockito.mock(EndgameKafkaPublisher.class);
    FmsListenerConfig fmsListenerConfig = Mockito.mock(FmsListenerConfig.class);
    FMSOrderRepository fmsOrderRepository = Mockito.mock(FMSOrderRepository.class);
    Counter counter = Mockito.mock(Counter.class);
    Map<String, String> fmsListenerConfigKvp = new HashMap<>();
    final String[] jsonFieldsToRemove = new String[] {"eventId", "eventTime"};

    @Captor
    ArgumentCaptor<String> argumentCaptor1;
    @Captor
    ArgumentCaptor<String> argumentCaptor2;
    @Captor
    ArgumentCaptor<MessageHeaderAccessor> argumentCaptor3;

    @Autowired
    ObjectMapper objectMapper;
    private FmsCancellationEvent fmsCancellationEvent;

    @BeforeEach
    void setup() throws IOException {
        when(micrometerService.getCustomerCancelMetric(any())).thenReturn(counter);
        when(micrometerService.getIgnoredCancellationMetric(any())).thenReturn(counter);
        fmsCancellationServiceSpy = spy(new FMSCancellationService(
                objectMapper,
                endgameKafkaPublisher,
                fmsListenerConfig,
                micrometerService,
                fmsOrderRepository));

        fmsCancellationEvent = getEntity("/json/fms-cancel-event.json", FmsCancellationEvent.class);
        fmsListenerConfigKvp.put("9615", "9610");
        when(fmsListenerConfig.getShipNodeToFcid()).thenReturn(fmsListenerConfigKvp);
    }

    @Test
    public void test_cancellation() throws IOException, JSONException {
        when(fmsOrderRepository.findById(any(),any())).thenReturn(Optional.of(FmsOrderDAO.builder()
                .fcId("9610")
                .purchaseOrderNo("3862309610324")
                .status(FMSOrderStatus.PACKMAN_RECOMMENDATION_UPDATED)
                .fmsOrderEvent(getEntity("/json/fms-order-outbound.json", FmsOrderEvent.class))
                .build()));
        var fmsCancelEvent = getEntity("/json/fms-cancel-event-payload.json", FmsCancellationEvent.class);
        fmsCancellationServiceSpy.handle(fmsCancelEvent);
        verify(endgameKafkaPublisher, times(1))
                    .send(argumentCaptor1.capture(),argumentCaptor2.capture(),argumentCaptor3.capture());
        FmsCancellationEvent fmsCancelEventActual =
                objectMapper.readValue(argumentCaptor2.getValue(), FmsCancellationEvent.class);
        assertEquals(fmsCancelEvent.getBuId(), fmsCancelEventActual.getBuId());
        assertEquals(fmsCancelEvent.getPurchaseOrderNo(), fmsCancelEventActual.getPurchaseOrderNo());
        MessageHeaderAccessor header = argumentCaptor3.getValue();
        assertEquals(FMS_EVENT_NAME.FULFILLMENT_ORDER_CANCEL.name(), header.getHeader(FMS_EVENT_KEY));
        assertEquals("3862309610324", argumentCaptor1.getValue());
    }

    @Test
    public void test_cancellation_invalid_FC() throws IOException, JSONException {
        var fmsCancelEvent = getEntity("/json/fms-cancel-invalidFC.json", FmsCancellationEvent.class);
        fmsCancellationServiceSpy.handle(fmsCancelEvent);
        verify(endgameKafkaPublisher, times(0))
                .send(argumentCaptor1.capture(),argumentCaptor2.capture(),argumentCaptor3.capture());
    }

    @Test
    public void test_cancellation_invalid_status() throws IOException, JSONException {
        when(fmsOrderRepository.findById(any(),any())).thenReturn(Optional.of(FmsOrderDAO.builder()
                .fcId("9610")
                .purchaseOrderNo("3862309610324")
                .fmsOrderEvent(getEntity("/json/fms-order-outbound.json", FmsOrderEvent.class))
                .status(FMSOrderStatus.PACKMAN_RECOMMENDATION_REQUESTED)
                .build()));
        var fmsCancelEvent = getEntity("/json/fms-cancel-invalidFC.json", FmsCancellationEvent.class);
        fmsCancellationServiceSpy.handle(fmsCancelEvent);
        verify(endgameKafkaPublisher, times(0))
                .send(argumentCaptor1.capture(),argumentCaptor2.capture(),argumentCaptor3.capture());
    }

    private <T> T getEntity(String src, Class<T> clazz) throws IOException {
        return objectMapper.readValue(getJson(src), clazz);
    }

    private String getJson(final String src) throws IOException {
        return IOUtils.resourceToString(src, StandardCharsets.UTF_8);
    }

    private JsonNode getJsonNode(String customerPOStatusUpdate) throws JsonProcessingException {
        return objectMapper.readTree(customerPOStatusUpdate);
    }

    protected void trimJson(JsonNode jsonNode, String... fieldsToTrim) {
        Arrays.stream(fieldsToTrim).forEach(((ObjectNode) jsonNode)::remove);
    }
}
