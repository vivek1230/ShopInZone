package com.walmart.thor.endgame.orderingestion.service;

import com.walmart.thor.endgame.orderingestion.common.BaseConsumerTest;
import com.walmart.thor.endgame.orderingestion.common.dao.Allocation;
import com.walmart.thor.endgame.orderingestion.common.dao.Allocation.AllocationID;
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
import com.walmart.thor.endgame.orderingestion.exception.FMSOrderException;
import com.walmart.thor.endgame.orderingestion.service.orderhandler.FmsOrderHandlerDefault;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FmsOrderHandlerDefaultTest extends BaseConsumerTest {

  @MockBean AllocationRepository allocationRepository;

  @MockBean GlobalGtinInventoryRepository globalGtinInventoryRepository;

  @MockBean CustomerPurchaseOrderRepository customerPurchaseOrderRepository;

  @MockBean private PickTicketIDGenerator pickTicketIDGenerator;

  @MockBean private FmsKafkaPublisher fmsKafkaProducer;

  @Captor
  ArgumentCaptor<String> fmsUpdateMessage;

  FmsOrderHandlerDefault fmsOrderHandlerSpy;

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
    AllocationService allocationService = new AllocationService(
            allocationRepository,
            globalGtinInventoryRepository,
            pickTicketIDGenerator);
    var fmsOrderHandler =
        new FmsOrderHandlerDefault(
            customerPurchaseOrderRepository,
            fcDetailsConfig,
            fmsKafkaProducer,
            mapper,
            micrometerService,
            allocationService);
    fmsOrderHandlerSpy = spy(fmsOrderHandler);
  }

  @Test
  void testIfInventoryAvailable() throws IOException, FMSOrderException {
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

    List<String> pickticketIds = new ArrayList<>();
    pickticketIds.add("11A");
    pickticketIds.add("11B");
    when(pickTicketIDGenerator.getNextPickTicketIDs(anyString(), anyInt()))
        .thenReturn(pickticketIds);
    when(globalGtinInventoryRepository.findAllById(any())).thenReturn(GtinInventory);
    when(allocationRepository.findByOrderId(any())).thenReturn(new ArrayList<>());
    fmsOrderHandlerSpy.process(fmsOrderEvent);
    verify(globalGtinInventoryRepository, atLeastOnce()).saveAll(any());
    verify(allocationRepository, atLeastOnce()).saveAll(any());
    verify(customerPurchaseOrderRepository, atLeastOnce()).save(any());
    verify(counter, atLeastOnce()).increment();
  }

  @Test
  void testIfGtinMissingInInvenotry() throws IOException, FMSOrderException {
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

    when(globalGtinInventoryRepository.findAllById(any())).thenReturn(GtinInventory);
    when(allocationRepository.findByOrderId(any())).thenReturn(new ArrayList<>());
    fmsOrderHandlerSpy.process(fmsOrderEvent);
    verify(globalGtinInventoryRepository, never()).saveAll(any());
    verify(allocationRepository, never()).saveAll(any());
    verify(counter, atLeastOnce()).increment();
  }

  @Test
  void testIfInventoryIsShort() throws IOException, FMSOrderException {
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
            .withAllocatedCount(5)
            .withSellableCount(5)
            .withId(
                new GlobalGtinInventoryID()
                    .withGtin("00193159050369")
                    .withFcId("9610")
                    .withSellerId("455A2F43226F41319399794332C71B7F")));

    when(globalGtinInventoryRepository.findAllById(any())).thenReturn(GtinInventory);
    when(allocationRepository.findByOrderId(any())).thenReturn(new ArrayList<>());
    fmsOrderHandlerSpy.process(fmsOrderEvent);
    verify(globalGtinInventoryRepository, never()).saveAll(any());
    verify(allocationRepository, never()).saveAll(any());
    verify(counter, atLeastOnce()).increment();
  }

  @Test
  void testIfAlreadyAllocated() throws IOException, FMSOrderException {
    var fmsOrderEvent = getEntity("/fmsOrder1.json", FmsOrderEvent.class);
    List<Allocation> allocations = new ArrayList<>();
    allocations.add(
        new Allocation()
            .withQuantity(1)
            .withId(
                new AllocationID()
                    .withPickTicketId("PT1")
                    .withGtin("00193159050371")
                    .withOrderId("200001001570090")
                    .withFcId("9610")
                    .withSellerId("455A2F43226F41319399794332C71B7F")));
    allocations.add(
        new Allocation()
            .withQuantity(1)
            .withId(
                new AllocationID()
                     .withPickTicketId("PT2")
                    .withGtin("00193159050369")
                    .withOrderId("200001001570090")
                    .withFcId("9610")
                    .withSellerId("455A2F43226F41319399794332C71B7F")));
    when(allocationRepository.findByOrderId(any())).thenReturn(allocations);
    fmsOrderHandlerSpy.process(fmsOrderEvent);
    verify(globalGtinInventoryRepository, never()).saveAll(any());
    verify(allocationRepository, never()).saveAll(any());
  }

  @Test
  void testIfOrderPresentInCosmos() throws IOException, FMSOrderException {
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

    verify(fmsKafkaProducer, times(1)).send(any(), fmsUpdateMessage.capture());
    String fmsUpdatepayload = fmsUpdateMessage.getValue();
    CustomerPOStatusUpdate fmsOrderEventCapture = mapper.readValue(fmsUpdatepayload, CustomerPOStatusUpdate.class);
    assertEquals(LineStatus.LW, fmsOrderEventCapture.getEventPayload().getPurchaseOrderLines().get(0).getStatus());
    assertEquals(LineStatus.LW, fmsOrderEventCapture.getEventPayload().getPurchaseOrderLines().get(1).getStatus());
    assertEquals("", fmsOrderEventCapture.getEventPayload().getPurchaseOrderLines().get(0).getChangeReason());
    assertEquals("", fmsOrderEventCapture.getEventPayload().getPurchaseOrderLines().get(1).getChangeReason());
  }

  @Test
  void testIfRejectedOrderPresentInCosmos() throws IOException, FMSOrderException {
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

    verify(fmsKafkaProducer, times(1)).send(any(), fmsUpdateMessage.capture());
    String fmsUpdatepayload = fmsUpdateMessage.getValue();
    CustomerPOStatusUpdate fmsOrderEventCapture = mapper.readValue(fmsUpdatepayload, CustomerPOStatusUpdate.class);
    assertEquals(LineStatus.LB, fmsOrderEventCapture.getEventPayload().getPurchaseOrderLines().get(0).getStatus());
    assertEquals(LineStatus.LB, fmsOrderEventCapture.getEventPayload().getPurchaseOrderLines().get(1).getStatus());
    assertEquals("999", fmsOrderEventCapture.getEventPayload().getPurchaseOrderLines().get(0).getChangeReason());
    assertEquals("999", fmsOrderEventCapture.getEventPayload().getPurchaseOrderLines().get(1).getChangeReason());
  }

  @Test
  void sendLWOrderUpdatesToFMS() throws IOException, FMSOrderException {
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
    fmsOrderHandlerSpy.process(fmsOrderEvent);
    verify(globalGtinInventoryRepository, atLeastOnce()).saveAll(any());
    verify(allocationRepository, atLeastOnce()).saveAll(any());
    verify(customerPurchaseOrderRepository, atLeastOnce()).save(any());
    verify(fmsKafkaProducer, times(1)).send(any(), fmsUpdateMessage.capture());
    String fmsUpdatepayload = fmsUpdateMessage.getValue();
    CustomerPOStatusUpdate fmsOrderEventCapture = mapper.readValue(fmsUpdatepayload, CustomerPOStatusUpdate.class);
    assertEquals(LineStatus.LW, fmsOrderEventCapture.getEventPayload().getPurchaseOrderLines().get(0).getStatus());
    assertEquals(LineStatus.LW, fmsOrderEventCapture.getEventPayload().getPurchaseOrderLines().get(1).getStatus());
    assertEquals("", fmsOrderEventCapture.getEventPayload().getPurchaseOrderLines().get(0).getChangeReason());
    assertEquals("", fmsOrderEventCapture.getEventPayload().getPurchaseOrderLines().get(1).getChangeReason());
    verify(counter, atLeastOnce()).increment();
  }

  @Test
  void sendLBOrderUpdatesToFMS() throws IOException, FMSOrderException {
    var fmsOrderEvent = getEntity("/fmsOrder1.json", FmsOrderEvent.class);
    List<GlobalGtinInventory> GtinInventory = new ArrayList<>();
    GtinInventory.add(
            new GlobalGtinInventory()
                    .withAllocatedCount(3)
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
    fmsOrderHandlerSpy.process(fmsOrderEvent);
    verify(customerPurchaseOrderRepository, atLeastOnce()).save(any());
    verify(fmsKafkaProducer, times(1)).send(any(), fmsUpdateMessage.capture());
    String fmsUpdatepayload = fmsUpdateMessage.getValue();
    CustomerPOStatusUpdate fmsOrderEventCapture = mapper.readValue(fmsUpdatepayload, CustomerPOStatusUpdate.class);
    assertEquals(LineStatus.LB, fmsOrderEventCapture.getEventPayload().getPurchaseOrderLines().get(0).getStatus());
    assertEquals(LineStatus.LB, fmsOrderEventCapture.getEventPayload().getPurchaseOrderLines().get(1).getStatus());
    assertEquals("199", fmsOrderEventCapture.getEventPayload().getPurchaseOrderLines().get(0).getChangeReason());
    assertEquals("255", fmsOrderEventCapture.getEventPayload().getPurchaseOrderLines().get(1).getChangeReason());
    assertEquals("", fmsOrderEventCapture.getEventPayload().getPickTicketNumber().get());
    verify(counter, atLeastOnce()).increment();
  }
}
