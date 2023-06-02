package com.walmart.thor.endgame.orderingestion.service.service;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.walmart.thor.endgame.orderingestion.common.SerDesForTest;
import com.walmart.thor.endgame.orderingestion.common.dao.Bag;
import com.walmart.thor.endgame.orderingestion.common.dao.ItemProjection;
import com.walmart.thor.endgame.orderingestion.common.dao.StockBox;
import com.walmart.thor.endgame.orderingestion.common.dto.FmsOrderEvent;
import com.walmart.thor.endgame.orderingestion.common.dto.fmsListener.PackmanRequest;
import com.walmart.thor.endgame.orderingestion.common.repository.BagCatalogRepository;
import com.walmart.thor.endgame.orderingestion.common.repository.FMSOrderRepository;
import com.walmart.thor.endgame.orderingestion.common.repository.IStockBoxRepository;
import com.walmart.thor.endgame.orderingestion.common.repository.ItemProjectionRepository;
import com.walmart.thor.endgame.orderingestion.config.FmsListenerConfig;
import com.walmart.thor.endgame.orderingestion.config.ItemConfigWrapper;
import com.walmart.thor.endgame.orderingestion.service.PackmanInputService;
import com.walmart.thor.endgame.orderingestion.service.PackmanRequestPublisher;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
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
public class PackmanInputServiceTest {
  PackmanInputService packmanInputServiceSpy;
  ItemProjectionRepository itemProjectionRepository = Mockito.mock(ItemProjectionRepository.class);
  IStockBoxRepository stockBoxRepository = Mockito.mock(IStockBoxRepository.class);
  BagCatalogRepository bagCatalogRepository = Mockito.mock(BagCatalogRepository.class);
  PackmanRequestPublisher packmanRequestPublisher = Mockito.mock(PackmanRequestPublisher.class);
  @Autowired
  ObjectMapper objectMapper;
  FmsListenerConfig fmsListenerConfig = Mockito.mock(FmsListenerConfig.class);
  FMSOrderRepository fmsOrderRepository = Mockito.mock(FMSOrderRepository.class);
  ItemConfigWrapper itemConfigWrapper = Mockito.mock(ItemConfigWrapper.class);
  FmsOrderEvent fmsOrderEvent;
  ItemProjection itemProjection;
  @Captor
  ArgumentCaptor<String> packManRequestCaptor;
  @BeforeEach
  void setup() throws IOException {
    packmanInputServiceSpy = spy(
        new PackmanInputService(
            itemProjectionRepository,
            stockBoxRepository,
            bagCatalogRepository,
            packmanRequestPublisher,
            objectMapper,
            fmsListenerConfig,
            fmsOrderRepository,
            itemConfigWrapper

        )
    );
    String json = IOUtils.resourceToString("/json/item-projection-hazmat.json", StandardCharsets.UTF_8);
    itemProjection = objectMapper.readValue(json, ItemProjection.class);

    // bag and box repository output mocks.

    List<Bag> bagCatalogues = new ArrayList<>();


    Bag bagCatalog = getEntity("/json/bag-catalog.json", Bag.class);
    bagCatalogues.add(bagCatalog);



    Iterable<Bag> bagIterable = bagCatalogues;

    when(bagCatalogRepository.findAll(any(PartitionKey.class))).thenReturn(bagIterable);
    when(itemConfigWrapper.getBufferDimensions()).thenReturn(2);

  }

  @Test
  public void test_handleInput_When_Hazmat() throws IOException, JSONException {

    fmsOrderEvent = getEntity("/json/fms-order-outbound.json", FmsOrderEvent.class);
    List<StockBox> stockBoxes = new ArrayList<>();
    StockBox stockBox1 = getEntity("/json/stock-box.json", StockBox.class);
    stockBoxes.add(stockBox1);
    Iterable<StockBox> stockBoxIterable = stockBoxes;
    when(stockBoxRepository.findAll(any(PartitionKey.class))).thenReturn(stockBoxIterable);

    ArrayList<ItemProjection> itemProjections = new ArrayList<>();
    itemProjections.add(itemProjection);

    doNothing()
        .when(packmanRequestPublisher)
        .send(Mockito.any(), packManRequestCaptor.capture(), Mockito.any());

    packmanInputServiceSpy.handleInput(fmsOrderEvent, itemProjections, "9610");

    var packmanRequestArgumentCaptor = packManRequestCaptor.getValue();
    JsonNode jsonNodeActual = getJsonNode(packmanRequestArgumentCaptor);

    PackmanRequest packmanRequest =
        getEntity("/json/packman-request.json", PackmanRequest.class);
    JsonNode jsonNodeExpected = getJsonNode(objectMapper.writeValueAsString(packmanRequest));

    JSONAssert.assertEquals(
        String.valueOf(jsonNodeExpected), jsonNodeActual.toString(), JSONCompareMode.STRICT);
  }

  @Test
  public void test_handleInput_FOR_TEST_BOX() throws IOException, JSONException {

    fmsOrderEvent = getEntity("/json/FAT-fms-order-outbound.json", FmsOrderEvent.class);
    List<StockBox> stockBoxes = new ArrayList<>();
    StockBox stockBox1 = getEntity("/json/stock-box-test.json", StockBox.class);
    stockBoxes.add(stockBox1);
    Iterable<StockBox> stockBoxIterable = stockBoxes;
    when(stockBoxRepository.findAll(any(PartitionKey.class))).thenReturn(stockBoxIterable);

    ArrayList<ItemProjection> itemProjections = new ArrayList<>();
    itemProjections.add(itemProjection);

    doNothing()
            .when(packmanRequestPublisher)
            .send(Mockito.any(), packManRequestCaptor.capture(), Mockito.any());

    packmanInputServiceSpy.handleInput(fmsOrderEvent, itemProjections, "9610");

    var packmanRequestArgumentCaptor = packManRequestCaptor.getValue();
    JsonNode jsonNodeActual = getJsonNode(packmanRequestArgumentCaptor);

    PackmanRequest packmanRequest =
            getEntity("/json/FAT-packman-request.json", PackmanRequest.class);
    JsonNode jsonNodeExpected = getJsonNode(objectMapper.writeValueAsString(packmanRequest));

    JSONAssert.assertEquals(
            String.valueOf(jsonNodeExpected), jsonNodeActual.toString(), JSONCompareMode.STRICT);
  }

  @Test
  public void test_handleInput_When_Hazmat_Not_Present() throws IOException, JSONException {
    fmsOrderEvent = getEntity("/json/fms-order-outbound.json", FmsOrderEvent.class);
    List<StockBox> stockBoxes = new ArrayList<>();
    StockBox stockBox1 = getEntity("/json/stock-box.json", StockBox.class);
    stockBoxes.add(stockBox1);
    Iterable<StockBox> stockBoxIterable = stockBoxes;
    when(stockBoxRepository.findAll(any(PartitionKey.class))).thenReturn(stockBoxIterable);
    ArrayList<ItemProjection> itemProjections = new ArrayList<>();
    String json = IOUtils.resourceToString("/json/item-projection1.json", StandardCharsets.UTF_8);
    itemProjection = objectMapper.readValue(json, ItemProjection.class);
    itemProjections.add(itemProjection);
    doNothing()
        .when(packmanRequestPublisher)
        .send(Mockito.any(), packManRequestCaptor.capture(), Mockito.any());

    packmanInputServiceSpy.handleInput(fmsOrderEvent, itemProjections, "9610");

    var packmanRequestArgumentCaptor = packManRequestCaptor.getValue();
    JsonNode jsonNodeActual = getJsonNode(packmanRequestArgumentCaptor);

    PackmanRequest packmanRequest =
        getEntity("/json/packman-request-no-hazmat.json", PackmanRequest.class);
    JsonNode jsonNodeExpected = getJsonNode(objectMapper.writeValueAsString(packmanRequest));

    JSONAssert.assertEquals(
        String.valueOf(jsonNodeExpected), jsonNodeActual.toString(), JSONCompareMode.STRICT);
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


}
