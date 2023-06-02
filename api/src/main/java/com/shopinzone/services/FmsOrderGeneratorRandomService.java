package com.shopinzone.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.walmart.thor.endgame.configs.FmsProducerConfig;
import com.walmart.thor.endgame.configs.ShipNodeConfig;
import com.walmart.thor.endgame.exceptions.OrderIngestionException;
import com.walmart.thor.endgame.fms.FMSOrder;
import com.walmart.thor.endgame.fms.FMSRequestRandom;
import com.walmart.thor.endgame.fms.FMSResponse;
import com.walmart.thor.endgame.producers.FmsProducer;
import com.walmart.thor.endgame.utils.CommonUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.walmart.thor.endgame.orderingestion.common.constants.Constants.FAT_ORDER_SUFFIX;

@Component
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class FmsOrderGeneratorRandomService {

  public static final int N_THREADS = 20;
  private final FmsProducer fmsProducer;
  private final CommonUtils commonUtils;
  private final ObjectMapper objectMapper;
  private final ShipNodeConfig shipNodeConfig;
  private final FmsProducerConfig fmsProducerConfig;

  public FMSResponse createMassOrderRandom(String fcId, FMSRequestRandom fmsRequestRandom)
      throws IOException, OrderIngestionException {

    log.info("fmsRequestRandom : {}", fmsRequestRandom);
    long millis = System.currentTimeMillis();
    String fmsOrderJsonString = commonUtils.readJson("/jsonFiles/fmsOrder.json");
    FMSOrder orderPayload = objectMapper.readValue(fmsOrderJsonString, FMSOrder.class);
    setStaticDataRandom(fcId, fmsRequestRandom, orderPayload);

    FMSResponse responseRandom = new FMSResponse();
    for (int orderCount = 1;
        orderCount <= fmsRequestRandom.getCountOfOrders()
            && fmsRequestRandom.getAvailableGtinMap().size() > 0;
        orderCount++) {

      long startTime = System.currentTimeMillis();
      String orderId = millis + "_" + orderCount;
      setDynamicDataRandom(fmsRequestRandom, orderPayload, orderId);
      fmsProducer.publishOrders(orderPayload);

      String purchaseOrderNo = orderPayload.getEventPayload().getPurchaseOrderNo();
      if (orderCount == 1) responseRandom.setFirstPurchaseOrderNo(purchaseOrderNo);
      responseRandom.setLastPurchaseOrderNo(purchaseOrderNo);
      long timeTaken = System.currentTimeMillis() - startTime;
      log.info("PurchaseOrderNo: {}, TIME_TAKEN_IN_MILI: {}", purchaseOrderNo, timeTaken);
    }
    return responseRandom;
  }

  private void setStaticDataRandom(
      String fcId, FMSRequestRandom fmsRequestRandom, FMSOrder orderPayload)
      throws OrderIngestionException {

    orderPayload.getEventPayload().setCarrierMethodId(fmsRequestRandom.getCarrierMethodID());

    setShipNodeDetails(shipNodeConfig.getShipNodeDetails(fcId), orderPayload);
    setLineDates(fmsRequestRandom.getCutOff(), orderPayload);
  }

  private void setShipNodeDetails(FMSOrder.ShipNodeDetails shipNodeDetails, FMSOrder orderPayload) {

    FMSOrder.ShipNode shipNode = orderPayload.getEventPayload().getShipNode();
    shipNode.setName(shipNodeDetails.getName());
    shipNode.setId(shipNodeDetails.getId());
    shipNode.setWmReferenceNumber(shipNodeDetails.getId());
  }

  private void setLineDates(OffsetDateTime cutOff, FMSOrder orderPayload) {

    FMSOrder.LineDates lineDates = orderPayload.getEventPayload().getLineDates();
    OffsetDateTime dayAfterCutOff = cutOff.plusDays(2);

    lineDates.setExpectedShipDate(cutOff);
    lineDates.setOrderProcessingDate(cutOff);
    lineDates.setPreciseDeliveryDate(dayAfterCutOff);
    lineDates.setMinDeliveryDate(dayAfterCutOff);
    lineDates.setMaxDeliveryDate(dayAfterCutOff);
  }

  private void setDynamicDataRandom(
      FMSRequestRandom fmsRequestRandom, FMSOrder orderPayload, String orderId) throws IOException {

    orderPayload.setEventId(UUID.randomUUID().toString());
    orderPayload.setEventTime(OffsetDateTime.now());
    orderPayload.getEventPayload().setOrderPlacedTimestamp(OffsetDateTime.now());

    setOrderDetailsRandom(orderPayload, orderId);
    setFulfillmentLinesItemRandom(fmsRequestRandom, orderPayload);
  }

  private void setOrderDetailsRandom(FMSOrder orderPayload, String orderId) {

    FMSOrder.EventPayload eventPayload = orderPayload.getEventPayload();
    eventPayload.setSourceOrderId(orderId + "OI");
    eventPayload.setCustomerOrderId(orderId + "OI");
    eventPayload.setPurchaseOrderNo(orderId + FAT_ORDER_SUFFIX);
  }

  private void setFulfillmentLinesItemRandom(
      FMSRequestRandom fmsRequestRandom, FMSOrder orderPayload) throws IOException {

    List<FMSOrder.FulfillmentLinesItem> fulfillmentLineList = new ArrayList<>();
    String linesItemString = commonUtils.readJson("/jsonFiles/fulfillmentLineItem.json");

    List<String> availableGtinList =
        new ArrayList<>(fmsRequestRandom.getAvailableGtinMap().keySet());
    int randomLineCount =
        RandomUtils.nextInt(fmsRequestRandom.getMinLines(), fmsRequestRandom.getMaxLines()+1);
    for (int i = 0; i < randomLineCount && availableGtinList.size() > 0; i++) {

      FMSOrder.FulfillmentLinesItem linesItem =
          objectMapper.readValue(linesItemString, FMSOrder.FulfillmentLinesItem.class);

      linesItem.setFulfillmentLineId(String.valueOf(i + 1));
      linesItem.getItemDetails().setSellerId(fmsRequestRandom.getSellerID());

      int randomGtinIndex = RandomUtils.nextInt(0, availableGtinList.size());
      String gtin = availableGtinList.remove(randomGtinIndex);
      linesItem.getItemDetails().setGtin(gtin);

      int randomPickCount =
          RandomUtils.nextInt(fmsRequestRandom.getMinPicks(), fmsRequestRandom.getMaxPicks()+1);
      Integer availablePickCount = fmsRequestRandom.getAvailableGtinMap().get(gtin);
      if (availablePickCount > randomPickCount) {
        linesItem.getQuantity().setMeasurementValue(randomPickCount);
        fmsRequestRandom.getAvailableGtinMap().put(gtin, (availablePickCount - randomPickCount));
      } else {
        linesItem.getQuantity().setMeasurementValue(availablePickCount);
        fmsRequestRandom.getAvailableGtinMap().remove(gtin);
      }

      fulfillmentLineList.add(linesItem);
    }
    orderPayload.getEventPayload().setFulfillmentLines(fulfillmentLineList);
  }
}
