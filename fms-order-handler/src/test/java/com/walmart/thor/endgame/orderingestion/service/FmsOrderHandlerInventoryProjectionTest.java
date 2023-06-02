package com.walmart.thor.endgame.orderingestion.service;

import com.walmart.thor.endgame.orderingestion.common.BaseConsumerTest;
import com.walmart.thor.endgame.orderingestion.common.dao.CustomerPurchaseOrderDAO;
import com.walmart.thor.endgame.orderingestion.common.dao.GlobalGtinInventory;
import com.walmart.thor.endgame.orderingestion.common.dao.GlobalGtinInventory.GlobalGtinInventoryID;
import com.walmart.thor.endgame.orderingestion.common.dto.domain.LineStatus;
import com.walmart.thor.endgame.orderingestion.common.dto.fmsupdates.CustomerPOStatusUpdate;
import com.walmart.thor.endgame.orderingestion.common.repository.AllocationRepository;
import com.walmart.thor.endgame.orderingestion.common.repository.CustomerPurchaseOrderRepository;
import com.walmart.thor.endgame.orderingestion.common.repository.GlobalGtinInventoryRepository;
import com.walmart.thor.endgame.orderingestion.config.FCDetailsConfig;
import com.walmart.thor.endgame.orderingestion.dto.FmsOrderEvent;
import com.walmart.thor.endgame.orderingestion.dto.allocation.Error;
import com.walmart.thor.endgame.orderingestion.dto.allocation.*;
import com.walmart.thor.endgame.orderingestion.exception.FMSOrderRuntimeException;
import com.walmart.thor.endgame.orderingestion.service.orderhandler.FmsOrderHandlerInventoryProjection;
import io.micrometer.core.instrument.Counter;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.*;

import static com.walmart.thor.endgame.orderingestion.common.constants.Constants.ERROR_CODE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FmsOrderHandlerInventoryProjectionTest extends BaseConsumerTest {

    @MockBean AllocationRepository allocationRepository;

    @MockBean
    GlobalGtinInventoryRepository globalGtinInventoryRepository;

    @MockBean
    CustomerPurchaseOrderRepository customerPurchaseOrderRepository;

    @MockBean
    private PickTicketIDGenerator pickTicketIDGenerator;

    @MockBean
    private FmsKafkaPublisher fmsKafkaPublisher;

    @MockBean
    private CreateAllocationService createAllocationService;

    @Captor
    ArgumentCaptor<String> fmsUpdateMessage;

    FmsOrderHandlerInventoryProjection fmsOrderHandlerSpy;

    Counter counter = Mockito.mock(Counter.class);
    MicrometerService micrometerService = Mockito.mock(MicrometerService.class);

    @BeforeAll
    void setup() {
        Map<String, String> shipNodeToFcidMap = new HashMap<>();
        shipNodeToFcidMap.put("9615", "9610");
        FCDetailsConfig fcDetailsConfig = new FCDetailsConfig();
        fcDetailsConfig.setShipNodeToFcidMap(shipNodeToFcidMap);

        when(micrometerService.getAcceptedOrderMetric(any(), any())).thenReturn(counter);
        when(micrometerService.getRejectedOrderMetric(any(), any())).thenReturn(counter);
        var fmsOrderHandler =
                new FmsOrderHandlerInventoryProjection(
                        pickTicketIDGenerator,
                        customerPurchaseOrderRepository,
                        fcDetailsConfig,
                        fmsKafkaPublisher,
                        mapper,
                        micrometerService,
                        createAllocationService);
        fmsOrderHandlerSpy = spy(fmsOrderHandler);
    }

    @Test
    void testIfInventoryAvailable() throws IOException {
        var fmsOrderEvent = getEntity("/fmsOrder1.json", FmsOrderEvent.class);
        List<String> pickticketIds = new ArrayList<>();
        pickticketIds.add("11A");
        pickticketIds.add("11B");
        when(pickTicketIDGenerator.getNextPickTicketIDs(anyString(), anyInt()))
                .thenReturn(pickticketIds);
        when(createAllocationService.callCreateAllocation(any(), any())).thenReturn(getCreateAllocationResponse());
        fmsOrderHandlerSpy.process(fmsOrderEvent);
        verify(customerPurchaseOrderRepository, atLeastOnce()).save(any());
        verify(counter, atLeastOnce()).increment();
    }

    private CreateAllocationResponse getCreateAllocationResponse() {
        Allocation allocation = Allocation.builder()
                .pickTicketId("11A")
                .purchaseOrderNumber("1661142011443")
                .gtin("00032231512786")
                .fcid("9610")
                .id(123)
                .area("Undefined")
                .qty(40)
                .sellerId("F55CDC31AB754BB68FE0B39041159D63").build();
        List<Allocation> allocationList = new ArrayList<>();
        allocationList.add(allocation);
        Success success = Success.builder()
                .allocationList(allocationList)
                .build();
        return CreateAllocationResponse.builder()
                .success(success)
                .build();
    }

    private CreateAllocationResponse getCreateAllocationResponseError() {
        ItemKey itemKey = ItemKey.builder()
                .gtin("00193159050371")
                .fcid("9610")
                .sellerId("455A2F43226F41319399794332C71B7F").build();
        ItemKey itemKey1 = ItemKey.builder()
                .gtin("00193159050369")
                .fcid("9610")
                .sellerId("455A2F43226F41319399794332C71B7F").build();
        ItemStatus itemStatus = ItemStatus.builder()
                .availableInventoryQty(0)
                .requestedInventoryQty(40)
                .itemKey(itemKey).build();
        ItemStatus itemStatus1 = ItemStatus.builder()
                .availableInventoryQty(40)
                .requestedInventoryQty(25)
                .itemKey(itemKey1).build();

        ItemStatus itemStatus2 = ItemStatus.builder()
                .availableInventoryQty(15)
                .requestedInventoryQty(25)
                .itemKey(itemKey1).build();

        List<ItemStatus> itemStatusList = new ArrayList<>();
        itemStatusList.add(itemStatus);
        itemStatusList.add(itemStatus1);
        itemStatusList.add(itemStatus2);

        Error error = Error.builder()
                .itemStatusList(itemStatusList)
                .build();
        return CreateAllocationResponse.builder()
                .error(error)
                .build();

    }

    private CreateAllocationResponse getCreateAllocationResponseErrorCode() {

        Error error = Error.builder()
                .errorCode(ERROR_CODE)
                .errorDesc("SQL Exception")
                .build();
        return CreateAllocationResponse.builder()
                .error(error)
                .build();
    }

    @Test
    void testIfOrderPresentInCosmosWithStatusAccepted() throws IOException {
        var fmsOrderEvent = getEntity("/fmsOrder1.json", FmsOrderEvent.class);
        List<GlobalGtinInventory> GtinInventory = new ArrayList<>();
        GtinInventory.add(
                new GlobalGtinInventory()
                        .withAllocatedCount(2)
                        .withSellableCount(3)
                        .withId(
                                new GlobalGtinInventoryID()
                                        .withGtin("00193159050371")
                                        .withFcId("9610")
                                        .withSellerId("455A2F43226F41319399794332C71B7F")));
        GtinInventory.add(
                new GlobalGtinInventory()
                        .withAllocatedCount(3)
                        .withSellableCount(5)
                        .withId(
                                new GlobalGtinInventoryID()
                                        .withGtin("00193159050369")
                                        .withFcId("9610")
                                        .withSellerId("455A2F43226F41319399794332C71B7F")));

        when(globalGtinInventoryRepository.findAllById(any())).thenReturn(GtinInventory);
        when(allocationRepository.findByOrderId(any())).thenReturn(new ArrayList<>());
        CustomerPurchaseOrderDAO customerPurchaseOrderDAO = CustomerPurchaseOrderDAO.builder()
                .pickTicketDetails(List.of(CustomerPurchaseOrderDAO.PickTicketDetails.builder()
                        .pickTicketId("testPT1")
                        .build()))
                .fmsOrder(fmsOrderEvent.getEventPayload())
                .fcid("9610")
                .purchaseOrderNo("108710824602775")
                .status(CustomerPurchaseOrderDAO.Status.Accepted)
                .partitionKey("9610_108710824602775")
                .build();
        when(customerPurchaseOrderRepository.findById(any(), any()))
                .thenReturn(Optional.of(customerPurchaseOrderDAO));
        fmsOrderHandlerSpy.process(fmsOrderEvent);
        verify(globalGtinInventoryRepository, never()).saveAll(any());
        verify(allocationRepository, never()).saveAll(any());
        verify(customerPurchaseOrderRepository, never()).save(any());
    }


    @Test
    void testIfOrderPresentInCosmosWithStatusRejected() throws IOException {
        var fmsOrderEvent = getEntity("/fmsOrder1.json", FmsOrderEvent.class);
        List<GlobalGtinInventory> GtinInventory = new ArrayList<>();
        GtinInventory.add(
                new GlobalGtinInventory()
                        .withAllocatedCount(2)
                        .withSellableCount(3)
                        .withId(
                                new GlobalGtinInventoryID()
                                        .withGtin("00193159050371")
                                        .withFcId("9610")
                                        .withSellerId("455A2F43226F41319399794332C71B7F")));
        GtinInventory.add(
                new GlobalGtinInventory()
                        .withAllocatedCount(3)
                        .withSellableCount(5)
                        .withId(
                                new GlobalGtinInventoryID()
                                        .withGtin("00193159050369")
                                        .withFcId("9610")
                                        .withSellerId("455A2F43226F41319399794332C71B7F")));

        when(globalGtinInventoryRepository.findAllById(any())).thenReturn(GtinInventory);
        when(allocationRepository.findByOrderId(any())).thenReturn(new ArrayList<>());
        CustomerPurchaseOrderDAO customerPurchaseOrderDAO = CustomerPurchaseOrderDAO.builder()
                .pickTicketDetails(List.of(CustomerPurchaseOrderDAO.PickTicketDetails.builder()
                        .pickTicketId("testPT1")
                        .build()))
                .fmsOrder(fmsOrderEvent.getEventPayload())
                .fcid("9610")
                .purchaseOrderNo("108710824602775")
                .status(CustomerPurchaseOrderDAO.Status.Rejected)
                .partitionKey("9610_108710824602775")
                .build();
        when(customerPurchaseOrderRepository.findById(any(), any()))
                .thenReturn(Optional.of(customerPurchaseOrderDAO));
        fmsOrderHandlerSpy.process(fmsOrderEvent);
        verify(globalGtinInventoryRepository, never()).saveAll(any());
        verify(allocationRepository, never()).saveAll(any());
        verify(customerPurchaseOrderRepository, never()).save(any());
    }

    @Test
    void sendLWOrderUpdatesToFMS() throws IOException {
        var fmsOrderEvent = getEntity("/fmsOrder1.json", FmsOrderEvent.class);
        when(createAllocationService.callCreateAllocation(any(), any())).thenReturn(getCreateAllocationResponse());
        fmsOrderHandlerSpy.process(fmsOrderEvent);
        verify(customerPurchaseOrderRepository, atLeastOnce()).save(any());
        verify(fmsKafkaPublisher, times(1)).send(any(), fmsUpdateMessage.capture());
        String fmsUpdatepayload = fmsUpdateMessage.getValue();
        CustomerPOStatusUpdate fmsOrderEventCapture = mapper.readValue(fmsUpdatepayload, CustomerPOStatusUpdate.class);
        assertEquals(LineStatus.LW, fmsOrderEventCapture.getEventPayload().getPurchaseOrderLines().get(0).getStatus());
        assertEquals(LineStatus.LW, fmsOrderEventCapture.getEventPayload().getPurchaseOrderLines().get(1).getStatus());
        verify(counter, atLeastOnce()).increment();
    }

    @Test
    void sendLBOrderUpdatesToFMS() throws IOException {
        var fmsOrderEvent = getEntity("/fmsOrder1.json", FmsOrderEvent.class);
        when(createAllocationService.callCreateAllocation(any(), any())).thenReturn(getCreateAllocationResponseError());
        fmsOrderHandlerSpy.process(fmsOrderEvent);
        verify(customerPurchaseOrderRepository, atLeastOnce()).save(any());
        verify(fmsKafkaPublisher, times(1)).send(any(), fmsUpdateMessage.capture());
        String fmsUpdatepayload = fmsUpdateMessage.getValue();
        CustomerPOStatusUpdate fmsOrderEventCapture = mapper.readValue(fmsUpdatepayload, CustomerPOStatusUpdate.class);
        assertEquals(LineStatus.LB, fmsOrderEventCapture.getEventPayload().getPurchaseOrderLines().get(0).getStatus());
        assertEquals(LineStatus.LB, fmsOrderEventCapture.getEventPayload().getPurchaseOrderLines().get(1).getStatus());
        assertEquals("199", fmsOrderEventCapture.getEventPayload().getPurchaseOrderLines().get(0).getChangeReason());
        assertEquals("198", fmsOrderEventCapture.getEventPayload().getPurchaseOrderLines().get(1).getChangeReason());
        verify(counter, atLeastOnce()).increment();
    }

    @Test
    void testIPBCreateAllocationApiFailureWithErrorCode() throws IOException {
        var fmsOrderEvent = getEntity("/fmsOrder1.json", FmsOrderEvent.class);
        when(createAllocationService.callCreateAllocation(any(), any())).thenReturn(getCreateAllocationResponseErrorCode());
        assertThrows(FMSOrderRuntimeException.class, () -> fmsOrderHandlerSpy.process(fmsOrderEvent));
        verify(customerPurchaseOrderRepository, never()).save(any());
        verify(fmsKafkaPublisher, never()).send(any(), fmsUpdateMessage.capture());
    }
}