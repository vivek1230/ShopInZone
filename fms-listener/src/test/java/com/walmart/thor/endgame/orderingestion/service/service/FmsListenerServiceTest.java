package com.walmart.thor.endgame.orderingestion.service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.walmart.thor.endgame.orderingestion.common.SerDesForTest;
import com.walmart.thor.endgame.orderingestion.common.dao.Allocation;
import com.walmart.thor.endgame.orderingestion.common.dao.FmsOrderDAO;
import com.walmart.thor.endgame.orderingestion.common.dao.FmsOrderDAO.FMSOrderStatus;
import com.walmart.thor.endgame.orderingestion.common.dao.ItemProjection;
import com.walmart.thor.endgame.orderingestion.common.dto.FmsOrderEvent;
import com.walmart.thor.endgame.orderingestion.common.dto.fmsupdates.CustomerPOStatusUpdate;
import com.walmart.thor.endgame.orderingestion.common.repository.FMSOrderRepository;
import com.walmart.thor.endgame.orderingestion.common.repository.ItemProjectionRepository;
import com.walmart.thor.endgame.orderingestion.config.FmsListenerConfig;
import com.walmart.thor.endgame.orderingestion.domain.OrderDomain;
import com.walmart.thor.endgame.orderingestion.service.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@TestInstance(Lifecycle.PER_METHOD)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Import(SerDesForTest.class)
public class FmsListenerServiceTest {

  FmsListenerService fmsListenerServiceSpy;
  ItemProjection itemProjection1;
  ItemProjection itemProjection2;
  MicrometerService micrometerService = Mockito.mock(MicrometerService.class);
  OrderDomain orderDomain = Mockito.mock(OrderDomain.class);
  FMSOrderRepository fmsOrderRepository = Mockito.mock(FMSOrderRepository.class);
  ItemProjectionRepository itemProjectionRepository = Mockito.mock(ItemProjectionRepository.class);
  FMSKafkaPublisher fmsKafkaPublisher = Mockito.mock(FMSKafkaPublisher.class);
  EndgameKafkaPublisher endgameKafkaPublisher = Mockito.mock(EndgameKafkaPublisher.class);
  FmsListenerConfig fmsListenerConfig = Mockito.mock(FmsListenerConfig.class);
  PackmanInputService packmanInputService = Mockito.mock(PackmanInputService.class);
  Counter counter = Mockito.mock(Counter.class);
  Map<String, String> fmsListenerConfigKvp = new HashMap<>();
  final String[] jsonFieldsToRemove = new String[] {"eventId", "eventTime"};
  @Captor ArgumentCaptor<String> customerPOStatusUpdateArgumentCaptor;

  @Captor ArgumentCaptor<String> fmsOrderArgumentCaptor;



  @Autowired ObjectMapper objectMapper;
  private FmsOrderDAO fmsOrderDAO;
  private FmsOrderEvent fmsOrderEvent;

  @BeforeEach
  void setup() throws IOException {
    when(micrometerService.getOrdersWithValidFCMetric(any())).thenReturn(counter);
    when(micrometerService.getValidOrdersMetric(any(), any())).thenReturn(counter);
    when(micrometerService.getOrdersUnparsableMetric()).thenReturn(counter);
    when(micrometerService.getInvalidOrdersMetric(any(), any())).thenReturn(counter);
    when(micrometerService.getInvalidGtinMetric(any(), any())).thenReturn(counter);
    fmsListenerServiceSpy =
        spy(
            new FmsListenerService(
                orderDomain,
                fmsOrderRepository,
                itemProjectionRepository,
                objectMapper,
                fmsKafkaPublisher,
                endgameKafkaPublisher,
                fmsListenerConfig,
                packmanInputService,
                micrometerService));

    fmsOrderEvent = getEntity("/json/fms-order-outbound.json", FmsOrderEvent.class);
    fmsListenerConfigKvp.put("9615", "9610");
    when(fmsListenerConfig.getShipNodeToFcid()).thenReturn(fmsListenerConfigKvp);

    fmsOrderDAO =
        FmsOrderDAO.builder()
            .fcId("9610")
            .purchaseOrderNo("200001001570074")
            .fmsOrderEvent(fmsOrderEvent)
            .build();
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

  @Test
  public void test_when_invalid_order_then_publish_LB() throws IOException, JSONException {
    JsonNode jsonNodeExpected;
    when(orderDomain.isValidOrder(any())).thenReturn(false);
    doNothing()
        .when(fmsKafkaPublisher)
        .send(Mockito.any(), customerPOStatusUpdateArgumentCaptor.capture());
    fmsListenerServiceSpy.handle(fmsOrderEvent);

    var customerPOStatusUpdateArgumentCaptorValue = customerPOStatusUpdateArgumentCaptor.getValue();
    JsonNode jsonNodeActual = getJsonNode(customerPOStatusUpdateArgumentCaptorValue);

    CustomerPOStatusUpdate expectedCustomerPOStatusUpdate =
        getEntity("/json/order-update-payload-LB.json", CustomerPOStatusUpdate.class);
    jsonNodeExpected = getJsonNode(objectMapper.writeValueAsString(expectedCustomerPOStatusUpdate));
    assertEquals("108715325337528-LB", jsonNodeActual.get("eventId").asText());

    trimJson(jsonNodeActual, jsonFieldsToRemove);
    trimJson(jsonNodeActual.get("eventPayload").get("purchaseOrderLines").get(0), "modifiedDate");
    trimJson(jsonNodeExpected.get("eventPayload").get("purchaseOrderLines").get(0), "modifiedDate");
    trimJson(jsonNodeActual.get("eventPayload").get("purchaseOrderLines").get(1), "modifiedDate");
    trimJson(jsonNodeExpected.get("eventPayload").get("purchaseOrderLines").get(1), "modifiedDate");
    JSONAssert.assertEquals(
        String.valueOf(jsonNodeExpected), jsonNodeActual.toString(), JSONCompareMode.STRICT);
    verify(counter, atLeastOnce()).increment();
  }

  @Test
  public void test_when_valid_order_then_publish_LU() throws IOException, JSONException {
    JsonNode jsonNodeExpected;
    when(orderDomain.isValidOrder(any())).thenReturn(true);
    doNothing().when(fmsKafkaPublisher).send(any(), customerPOStatusUpdateArgumentCaptor.capture());
    when(fmsOrderRepository.save(any())).thenReturn(fmsOrderDAO);
    fmsListenerServiceSpy.handle(fmsOrderEvent);

    var customerPOStatusUpdateArgumentCaptorValue = customerPOStatusUpdateArgumentCaptor.getValue();
    JsonNode jsonNodeActual = getJsonNode(customerPOStatusUpdateArgumentCaptorValue);

    CustomerPOStatusUpdate expectedCustomerPOStatusUpdate =
        getEntity("/json/order-update-payload-LU.json", CustomerPOStatusUpdate.class);
    jsonNodeExpected = getJsonNode(objectMapper.writeValueAsString(expectedCustomerPOStatusUpdate));
    assertEquals("108715325337528-LU", jsonNodeActual.get("eventId").asText());

    trimJson(jsonNodeActual, jsonFieldsToRemove);
    trimJson(jsonNodeActual.get("eventPayload").get("purchaseOrderLines").get(0), "modifiedDate");
    trimJson(jsonNodeExpected.get("eventPayload").get("purchaseOrderLines").get(0), "modifiedDate");
    trimJson(jsonNodeActual.get("eventPayload").get("purchaseOrderLines").get(1), "modifiedDate");
    trimJson(jsonNodeExpected.get("eventPayload").get("purchaseOrderLines").get(1), "modifiedDate");
    JSONAssert.assertEquals(
        String.valueOf(jsonNodeExpected), jsonNodeActual.toString(), JSONCompareMode.STRICT);
    verify(counter, atLeastOnce()).increment();
  }

  @Test
  public void test_when_valid_order_order_already_present_then_doNothing() {

    when(orderDomain.isValidOrder(any())).thenReturn(true);
    doNothing().when(fmsKafkaPublisher).send(any(), customerPOStatusUpdateArgumentCaptor.capture());
    when(fmsOrderRepository.findById(any(), any())).thenReturn(Optional.of(fmsOrderDAO));
    fmsListenerServiceSpy.handle(fmsOrderEvent);
    verify(fmsOrderRepository, never()).save(any());
    verify(counter, atLeastOnce()).increment();
  }

  @Test
  public void test_when_item_details_gtin_mismatch_itemProjection_gtin_then_return_line_unIdentified_LU()
      throws IOException, JSONException {
    JsonNode jsonNodeExpected;
    when(orderDomain.isValidOrder(any())).thenReturn(true);
    doNothing().when(fmsKafkaPublisher).send(any(), customerPOStatusUpdateArgumentCaptor.capture());
    when(fmsOrderRepository.save(any())).thenReturn(fmsOrderDAO);
    fmsListenerServiceSpy.handle(fmsOrderEvent);
    var customerPOStatusUpdateArgumentCaptorValue = customerPOStatusUpdateArgumentCaptor.getValue();
    JsonNode jsonNodeActual = getJsonNode(customerPOStatusUpdateArgumentCaptorValue);
    assertEquals(fmsOrderDAO.getStatus().toString(), FMSOrderStatus.LINE_UNIDENTIFIED.toString());

    CustomerPOStatusUpdate expectedCustomerPOStatusUpdate =
        getEntity("/json/order-update-payload-LU.json", CustomerPOStatusUpdate.class);
    jsonNodeExpected = getJsonNode(objectMapper.writeValueAsString(expectedCustomerPOStatusUpdate));
    assertEquals("108715325337528-LU", jsonNodeActual.get("eventId").asText());

    trimJson(jsonNodeActual, jsonFieldsToRemove);
    trimJson(jsonNodeActual.get("eventPayload").get("purchaseOrderLines").get(0), "modifiedDate");
    trimJson(jsonNodeExpected.get("eventPayload").get("purchaseOrderLines").get(0), "modifiedDate");
    trimJson(jsonNodeActual.get("eventPayload").get("purchaseOrderLines").get(1), "modifiedDate");
    trimJson(jsonNodeExpected.get("eventPayload").get("purchaseOrderLines").get(1), "modifiedDate");
    JSONAssert.assertEquals(
        String.valueOf(jsonNodeExpected), jsonNodeActual.toString(), JSONCompareMode.STRICT);
    verify(counter, atLeastOnce()).increment();
  }

  @Test
  public void test_when_item_details_gtin_mismatch_itemProjection_gtin_then_return_line_unIdentified_LB()
      throws IOException, JSONException {
    JsonNode jsonNodeExpected;
    when(fmsListenerConfig.getShipNodeToFcid()).thenReturn(fmsListenerConfigKvp);
    when(orderDomain.isValidOrder(any())).thenReturn(true);

    String json1 = IOUtils.resourceToString("/json/item-projection1.json", StandardCharsets.UTF_8);
    itemProjection1 = objectMapper.readValue(json1, ItemProjection.class);

    String json2 = IOUtils.resourceToString("/json/item-projection2.json", StandardCharsets.UTF_8);
    itemProjection2 = objectMapper.readValue(json2, ItemProjection.class);

    List<ItemProjection> itemProjections = new ArrayList<>();
    itemProjections.add(itemProjection1);
    itemProjections.add(itemProjection2);
    Iterable<ItemProjection> itemProjectionIterable = itemProjections;

    when(fmsOrderRepository.save(any())).thenReturn(fmsOrderDAO);
    when(itemProjectionRepository.findAll(
            new PartitionKey(ItemProjection.getPartitionKey("00193159050368"))))
        .thenReturn(itemProjectionIterable);
    doNothing().when(fmsKafkaPublisher).send(any(), customerPOStatusUpdateArgumentCaptor.capture());
    fmsListenerServiceSpy.handle(fmsOrderEvent);

    assertEquals(fmsOrderDAO.getStatus().toString(), FMSOrderStatus.LINE_UNIDENTIFIED.toString());

    var customerPOStatusUpdateArgumentCaptorValue = customerPOStatusUpdateArgumentCaptor.getValue();
    JsonNode jsonNodeActual = getJsonNode(customerPOStatusUpdateArgumentCaptorValue);

    CustomerPOStatusUpdate expectedCustomerPOStatusUpdate =
        getEntity("/json/order-update-payload-LB.json", CustomerPOStatusUpdate.class);
    jsonNodeExpected = getJsonNode(objectMapper.writeValueAsString(expectedCustomerPOStatusUpdate));
    Assertions.assertEquals("108715325337528-LB", jsonNodeActual.get("eventId").asText());

    trimJson(jsonNodeActual, jsonFieldsToRemove);
    trimJson(jsonNodeActual.get("eventPayload").get("purchaseOrderLines").get(0), "modifiedDate");
    trimJson(jsonNodeExpected.get("eventPayload").get("purchaseOrderLines").get(0), "modifiedDate");
    trimJson(jsonNodeActual.get("eventPayload").get("purchaseOrderLines").get(1), "modifiedDate");
    trimJson(jsonNodeExpected.get("eventPayload").get("purchaseOrderLines").get(1), "modifiedDate");
    JSONAssert.assertEquals(
        String.valueOf(jsonNodeExpected), jsonNodeActual.toString(), JSONCompareMode.STRICT);
    verify(counter, atLeastOnce()).increment();
  }

  @Test
  public void test_when_boxing_details_present_then_publish_to_endgame()
      throws IOException, JSONException {
    fmsOrderEvent = getEntity("/json/fms-order-outbound-boxing-details.json", FmsOrderEvent.class);

    fmsOrderDAO =
        FmsOrderDAO.builder()
            .fcId("9610")
            .purchaseOrderNo("200001001570074")
            .fmsOrderEvent(fmsOrderEvent)
            .build();

    when(orderDomain.isValidOrder(any())).thenReturn(true);

    String json1 = IOUtils.resourceToString("/json/item-projection1.json", StandardCharsets.UTF_8);
    itemProjection1 = objectMapper.readValue(json1, ItemProjection.class);

    List<ItemProjection> itemProjections = new ArrayList<>();
    itemProjections.add(itemProjection1);

    Iterable<ItemProjection> itemProjectionIterable = itemProjections;

    when(fmsOrderRepository.save(any())).thenReturn(fmsOrderDAO);
    when(itemProjectionRepository.findAll(
            new PartitionKey(ItemProjection.getPartitionKey("00193159050368"))))
        .thenReturn(itemProjectionIterable);
    doNothing().when(endgameKafkaPublisher).send(any(), fmsOrderArgumentCaptor.capture(), any());
    fmsListenerServiceSpy.handle(fmsOrderEvent);

    assertEquals(FMSOrderStatus.PACKMAN_RECOMMENDATION_NOT_REQUIRED, fmsOrderDAO.getStatus());

    var fmsOrderCaptorValue = fmsOrderArgumentCaptor.getValue();
    JsonNode jsonNodeActual = getJsonNode(fmsOrderCaptorValue);
    JsonNode jsonNodeExpected = getJsonNode(objectMapper.writeValueAsString(fmsOrderEvent));
    JSONAssert.assertEquals(
        String.valueOf(jsonNodeExpected), jsonNodeActual.toString(), JSONCompareMode.STRICT);
    verify(counter, atLeastOnce()).increment();
  }

  @Test
  public void test_when_boxing_details_not_present_then_publish_to_packMan()
      throws IOException, JSONException {
    when(orderDomain.isValidOrder(any())).thenReturn(true);

    String json1 = IOUtils.resourceToString("/json/item-projection1.json", StandardCharsets.UTF_8);
    itemProjection1 = objectMapper.readValue(json1, ItemProjection.class);

    List<ItemProjection> itemProjections = new ArrayList<>();
    itemProjections.add(itemProjection1);

    Iterable<ItemProjection> itemProjectionIterable = itemProjections;

    when(fmsOrderRepository.save(any())).thenReturn(fmsOrderDAO);
    when(itemProjectionRepository.findAll(
            new PartitionKey(ItemProjection.getPartitionKey("00193159050368"))))
        .thenReturn(itemProjectionIterable);
    fmsListenerServiceSpy.handle(fmsOrderEvent);

    assertEquals(FMSOrderStatus.PACKMAN_RECOMMENDATION_REQUESTED, fmsOrderDAO.getStatus());
    verify(counter, atLeastOnce()).increment();
  }

  @Test
  public void test_when_fcMap_not_present_then_publish_fms_FC_MAP_MISSING() throws IOException {
    when(orderDomain.isValidOrder(any())).thenReturn(true);

    String json1 =
        IOUtils.resourceToString("/json/item-projection-no-fc-map.json", StandardCharsets.UTF_8);
    ItemProjection itemProjectionNoFcMap = objectMapper.readValue(json1, ItemProjection.class);

    List<ItemProjection> itemProjections = new ArrayList<>();
    itemProjections.add(itemProjectionNoFcMap);

    Iterable<ItemProjection> itemProjectionIterable = itemProjections;

    when(fmsOrderRepository.save(any())).thenReturn(fmsOrderDAO);
    when(itemProjectionRepository.findAll(
            new PartitionKey(ItemProjection.getPartitionKey("00193159050368"))))
        .thenReturn(itemProjectionIterable);
    fmsListenerServiceSpy.handle(fmsOrderEvent);

    assertEquals(FMSOrderStatus.FC_MAP_MISSING, fmsOrderDAO.getStatus());
    verify(counter, atLeastOnce()).increment();
  }
}
