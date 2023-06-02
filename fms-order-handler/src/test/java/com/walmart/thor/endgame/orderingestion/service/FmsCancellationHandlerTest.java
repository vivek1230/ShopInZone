package com.walmart.thor.endgame.orderingestion.service;

import com.walmart.thor.endgame.orderingestion.common.BaseConsumerTest;
import com.walmart.thor.endgame.orderingestion.common.dao.CustomerPurchaseOrderDAO;
import com.walmart.thor.endgame.orderingestion.common.dto.customercancel.FmsCancellationEvent;
import com.walmart.thor.endgame.orderingestion.common.repository.CustomerPurchaseOrderRepository;
import com.walmart.thor.endgame.orderingestion.config.FCDetailsConfig;
import io.micrometer.core.instrument.Counter;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;


@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FmsCancellationHandlerTest extends BaseConsumerTest {

    @MockBean CustomerPurchaseOrderRepository customerPurchaseOrderRepository;
    @MockBean private EndgameKafkaPublisher endgameKafkaPublisher;
    MicrometerService micrometerService = Mockito.mock(MicrometerService.class);
    Counter counter = Mockito.mock(Counter.class);
    FmsCancellationHandler fmsCancellationHandlerSpy;

    @BeforeAll
    void setup() {
        Map<String, String> shipNodeToFcidMap = new HashMap<>();
        shipNodeToFcidMap.put("9615", "9610");
        FCDetailsConfig fcDetailsConfig = new FCDetailsConfig();
        fcDetailsConfig.setShipNodeToFcidMap(shipNodeToFcidMap);
        when(micrometerService.getIgnoredCancellationMetric(any())).thenReturn(counter);
        when(micrometerService.getPtCustomerCancelRequest(any())).thenReturn(counter);
        FmsCancellationHandler fmsCancellationHandler = new FmsCancellationHandler(
                customerPurchaseOrderRepository,
                endgameKafkaPublisher,
                fcDetailsConfig,
                micrometerService,
                mapper);
        fmsCancellationHandlerSpy = spy(fmsCancellationHandler);
    }

    @Test
    void testForOptimalAlgo_1() throws IOException {
        ArgumentCaptor<String> kafkaArgumentCaptor = ArgumentCaptor.forClass(String.class);
        CustomerPurchaseOrderDAO customerPurchaseOrderDAO =
                getEntity("/customer-purchase-order.json", CustomerPurchaseOrderDAO.class);
        when(customerPurchaseOrderRepository.findById(any(), any()))
                .thenReturn(Optional.of(customerPurchaseOrderDAO));
        var fmsCancelEvent = getEntity("/fms-cancel-event.json", FmsCancellationEvent.class);
        fmsCancellationHandlerSpy.process(fmsCancelEvent);
        verify(endgameKafkaPublisher, times(2))
                .send(kafkaArgumentCaptor.capture(), any(), any());
        List<String> cancelledPTs = kafkaArgumentCaptor.getAllValues();
        Collections.sort(cancelledPTs);
        assertEquals(2, cancelledPTs.size());
        assertEquals("96104750638C", cancelledPTs.get(0));
        assertEquals("96104750638D", cancelledPTs.get(1));
    }

    @Test
    void testForOptimalAlgo_2() throws IOException {
        ArgumentCaptor<String> kafkaArgumentCaptor = ArgumentCaptor.forClass(String.class);
        CustomerPurchaseOrderDAO customerPurchaseOrderDAO =
                getEntity("/customer-purchase-order.json", CustomerPurchaseOrderDAO.class);
        when(customerPurchaseOrderRepository.findById(any(), any()))
                .thenReturn(Optional.of(customerPurchaseOrderDAO));
        var fmsCancelEvent = getEntity("/fms-cancel-event_1.json", FmsCancellationEvent.class);
        fmsCancellationHandlerSpy.process(fmsCancelEvent);
        verify(endgameKafkaPublisher, times(2))
                .send(kafkaArgumentCaptor.capture(), any(), any());
        List<String> cancelledPTs = kafkaArgumentCaptor.getAllValues();
        Collections.sort(cancelledPTs);
        assertEquals(2, cancelledPTs.size());
        assertEquals("96104750638B", cancelledPTs.get(0));
        assertTrue(cancelledPTs.get(1).equals("96104750638D") || cancelledPTs.get(1).equals("96104750638C"));
    }

    @Test
    void testForOptimalAlgo_3() throws IOException {
        ArgumentCaptor<String> kafkaArgumentCaptor = ArgumentCaptor.forClass(String.class);
        CustomerPurchaseOrderDAO customerPurchaseOrderDAO =
                getEntity("/cpo-4.json", CustomerPurchaseOrderDAO.class);
        when(customerPurchaseOrderRepository.findById(any(), any()))
                .thenReturn(Optional.of(customerPurchaseOrderDAO));
        var fmsCancelEvent = getEntity("/fms-cancel-event_cpo_2.json", FmsCancellationEvent.class);
        fmsCancellationHandlerSpy.process(fmsCancelEvent);
        verify(endgameKafkaPublisher, times(15))
                .send(kafkaArgumentCaptor.capture(), any(), any());
        List<String> cancelledPTs = kafkaArgumentCaptor.getAllValues();
        Collections.sort(cancelledPTs);
        assertEquals(15, cancelledPTs.size());
        assertTrue(cancelledPTs.contains("96104750638A"));
        assertFalse(cancelledPTs.contains("96104750638B"));
    }

    @Test
    void testForGreedyAlgo_1() throws IOException {
        ArgumentCaptor<String> kafkaArgumentCaptor = ArgumentCaptor.forClass(String.class);
        CustomerPurchaseOrderDAO customerPurchaseOrderDAO =
                getEntity("/cpo-1.json", CustomerPurchaseOrderDAO.class);
        when(customerPurchaseOrderRepository.findById(any(), any()))
                .thenReturn(Optional.of(customerPurchaseOrderDAO));
        var fmsCancelEvent = getEntity("/fms-cancel-event_cpo_1.json", FmsCancellationEvent.class);
        fmsCancellationHandlerSpy.process(fmsCancelEvent);
        verify(endgameKafkaPublisher, times(1))
                .send(kafkaArgumentCaptor.capture(), any(), any());
        List<String> cancelledPTs = kafkaArgumentCaptor.getAllValues();
        Collections.sort(cancelledPTs);
        assertEquals(1, cancelledPTs.size());
        assertTrue(cancelledPTs.get(0).contains("96104993499"));
    }

    @Test
    void testForGreedyAlgo_2() throws IOException {
        ArgumentCaptor<String> kafkaArgumentCaptor = ArgumentCaptor.forClass(String.class);
        CustomerPurchaseOrderDAO customerPurchaseOrderDAO =
                getEntity("/cpo-3.json", CustomerPurchaseOrderDAO.class);
        when(customerPurchaseOrderRepository.findById(any(), any()))
                .thenReturn(Optional.of(customerPurchaseOrderDAO));
        var fmsCancelEvent = getEntity("/fms-cancel-event_cpo_2.json", FmsCancellationEvent.class);
        fmsCancellationHandlerSpy.process(fmsCancelEvent);
        verify(endgameKafkaPublisher, times(15))
                .send(kafkaArgumentCaptor.capture(), any(), any());
        List<String> cancelledPTs = kafkaArgumentCaptor.getAllValues();
        assertEquals(15, cancelledPTs.size());
        assertTrue(cancelledPTs.contains("96104750638A"));
    }

    @Test
    void testForWrongFC() throws IOException {
        CustomerPurchaseOrderDAO customerPurchaseOrderDAO =
                getEntity("/cpo-1.json", CustomerPurchaseOrderDAO.class);
        when(customerPurchaseOrderRepository.findById(any(), any()))
                .thenReturn(Optional.of(customerPurchaseOrderDAO));
        var fmsCancelEvent = getEntity("/cancel-event-wrong-fc.json", FmsCancellationEvent.class);
        fmsCancellationHandlerSpy.process(fmsCancelEvent);
        verify(endgameKafkaPublisher, times(0))
                .send(any(), any(), any());
    }

    @Test
    void testForOrderAbsent() throws IOException {
        when(customerPurchaseOrderRepository.findById(any(), any()))
                .thenReturn(Optional.empty());
        var fmsCancelEvent = getEntity("/fms-cancel-event.json", FmsCancellationEvent.class);
        fmsCancellationHandlerSpy.process(fmsCancelEvent);
        verify(endgameKafkaPublisher, times(0))
                .send(any(), any(), any());
    }

    @Test
    void testForRejectedOrder() throws IOException {
        CustomerPurchaseOrderDAO customerPurchaseOrderDAO =
                getEntity("/cpo-2.json", CustomerPurchaseOrderDAO.class);
        when(customerPurchaseOrderRepository.findById(any(), any()))
                .thenReturn(Optional.of(customerPurchaseOrderDAO));
        var fmsCancelEvent = getEntity("/fms-cancel-event.json", FmsCancellationEvent.class);
        fmsCancellationHandlerSpy.process(fmsCancelEvent);
        verify(endgameKafkaPublisher, times(0))
                .send(any(), any(), any());
    }

    @Test
    void testForMoreCancellationThanOrder() throws IOException {
        ArgumentCaptor<String> kafkaArgumentCaptor = ArgumentCaptor.forClass(String.class);
        CustomerPurchaseOrderDAO customerPurchaseOrderDAO =
                getEntity("/customer-purchase-order.json", CustomerPurchaseOrderDAO.class);
        when(customerPurchaseOrderRepository.findById(any(), any()))
                .thenReturn(Optional.of(customerPurchaseOrderDAO));
        var fmsCancelEvent = getEntity("/fms-cancel-event_2.json", FmsCancellationEvent.class);
        fmsCancellationHandlerSpy.process(fmsCancelEvent);
        verify(endgameKafkaPublisher, times(0))
                .send(any(), any(),any());
    }

    @Test
    void testForUnidentifiedLines() throws IOException {
        CustomerPurchaseOrderDAO customerPurchaseOrderDAO =
                getEntity("/customer-purchase-order.json", CustomerPurchaseOrderDAO.class);
        when(customerPurchaseOrderRepository.findById(any(), any()))
                .thenReturn(Optional.of(customerPurchaseOrderDAO));
        var fmsCancelEvent = getEntity("/fms-cancel-event_3.json", FmsCancellationEvent.class);
        fmsCancellationHandlerSpy.process(fmsCancelEvent);
        verify(endgameKafkaPublisher, times(0))
                .send(any(), any(),any());
    }
}