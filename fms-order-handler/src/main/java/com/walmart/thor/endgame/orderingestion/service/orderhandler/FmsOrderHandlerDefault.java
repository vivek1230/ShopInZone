package com.walmart.thor.endgame.orderingestion.service.orderhandler;


import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.walmart.thor.endgame.orderingestion.common.constants.Constants;
import com.walmart.thor.endgame.orderingestion.common.dao.CustomerPurchaseOrderDAO;
import com.walmart.thor.endgame.orderingestion.common.dao.CustomerPurchaseOrderDAO.PickTicketDetails;
import com.walmart.thor.endgame.orderingestion.common.dto.domain.ChangeReason;
import com.walmart.thor.endgame.orderingestion.common.dto.domain.EventName;
import com.walmart.thor.endgame.orderingestion.common.dto.domain.LineStatus;
import com.walmart.thor.endgame.orderingestion.common.dto.fmsorder.FmsOrder;
import com.walmart.thor.endgame.orderingestion.common.dto.fmsorder.FmsOrder.FulfillmentLines;
import com.walmart.thor.endgame.orderingestion.common.dto.fmsupdates.CustomerPOStatusUpdate;
import com.walmart.thor.endgame.orderingestion.common.dto.fmsupdates.OrderStatusUpdatePayload;
import com.walmart.thor.endgame.orderingestion.common.repository.CustomerPurchaseOrderRepository;
import com.walmart.thor.endgame.orderingestion.config.FCDetailsConfig;
import com.walmart.thor.endgame.orderingestion.dto.FmsOrderEvent;
import com.walmart.thor.endgame.orderingestion.dto.InventoryResult;
import com.walmart.thor.endgame.orderingestion.dto.ItemKey;
import com.walmart.thor.endgame.orderingestion.exception.FMSOrderException;
import com.walmart.thor.endgame.orderingestion.service.FmsKafkaPublisher;
import com.walmart.thor.endgame.orderingestion.service.AllocationService;
import com.walmart.thor.endgame.orderingestion.service.MicrometerService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import java.time.OffsetDateTime;
import java.util.*;

import static com.walmart.thor.endgame.orderingestion.common.constants.Constants.CUSTOMER_ORDER;

@Service
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class FmsOrderHandlerDefault implements FmsOrderHandler {

  private CustomerPurchaseOrderRepository customerPurchaseOrderRepository;

  private FCDetailsConfig fcDetailsConfig;

  private FmsKafkaPublisher fmsKafkaPublisher;

  private ObjectMapper mapper;

  private MicrometerService micrometerService;

  private AllocationService allocationService;


  /*
  1. check inventory and update inventory
  2. save the order in cosmos, accepted and rejected
  3. publish the FMS message
  * */

  public void process(FmsOrderEvent fmsOrderEvent) throws FMSOrderException {
    log.info("Processing FMS order message {}", fmsOrderEvent.getEventId());
    FmsOrder fmsOrder = fmsOrderEvent.getEventPayload();
    String shipNode = fmsOrder.getShipNode().getId();

    if (fcDetailsConfig.getShipNodeToFcidMap().containsKey(shipNode)) {
      String fcid = fcDetailsConfig.getShipNodeToFcidMap().get(shipNode);
      String purchaseOrderNo = fmsOrder.getPurchaseOrderNo();

      log.info(
          "Found order with valid Ship node, fcid: {}, purchaseOrderNo : {}",
          fcid,
          purchaseOrderNo);

      String partitionKey = String.format("%s_%s", fcid, purchaseOrderNo);
      Optional<CustomerPurchaseOrderDAO> fmsOrderDao =
              customerPurchaseOrderRepository.findById(
                      CUSTOMER_ORDER, new PartitionKey(partitionKey));

      if(fmsOrderDao.isPresent()) {
        log.info("Order already {} for fcid: {}, purchaseOrderNo : {}, resending FMS update",
                fmsOrderDao.get().getStatus().toString(),
                fcid,
                purchaseOrderNo);
        if(fmsOrderDao.get().getStatus().equals(CustomerPurchaseOrderDAO.Status.Accepted)) {
          List<PickTicketDetails> pickTicketDetails = fmsOrderDao.get().getPickTicketDetails();
          sendOrderUpdate(LineStatus.LW, pickTicketDetails, fmsOrderEvent, new HashMap<>());
        } else {
          sendOrderUpdate(LineStatus.LB, new ArrayList<>(), fmsOrderEvent, new HashMap<>());
        }
        return;
      }

      InventoryResult inventoryResult =
              allocationService.checkInventoryAndAllocate(fmsOrderEvent, fcid);

      if (inventoryResult.getIsAvailable()) {
        log.info(
                "Inventory is available and saving order to cosmos for fcid: {}, purchaseOrderNo : {}",
                fcid, purchaseOrderNo);
        saveFmsOrder(
                CustomerPurchaseOrderDAO.Status.Accepted,
                fcid,
                purchaseOrderNo,
                inventoryResult.getPickTicketDetails(),
                fmsOrder);
        micrometerService.getAcceptedOrderMetric(fcid, LineStatus.LW.getCode()).increment();
        sendOrderUpdate(LineStatus.LW,
                inventoryResult.getPickTicketDetails(),
                fmsOrderEvent,
                inventoryResult.getItemStatus());
      } else {
        log.info(
            "Inventory is not available for fcid: {}, purchaseOrderNo : {}", fcid, purchaseOrderNo);
        List<PickTicketDetails> pickTicketDetails = new ArrayList<>();
        saveFmsOrder(
            CustomerPurchaseOrderDAO.Status.Rejected,
            fcid,
            purchaseOrderNo,
            pickTicketDetails,
            fmsOrder);
        micrometerService.getRejectedOrderMetric(fcid, LineStatus.LB.getCode()).increment();
        sendOrderUpdate(LineStatus.LB, pickTicketDetails, fmsOrderEvent, inventoryResult.getItemStatus());

      }
    }
  }

  private String getPickTicketNumber(List<PickTicketDetails> pickTicketDetails){
    var pickTicketDetail =
            pickTicketDetails.stream()
                    .filter(p -> StringUtils.isNotEmpty(p.getPickTicketId()))
                    .findAny();
    var pickTicketNumber =
            pickTicketDetail.isPresent()
                    && StringUtils.isNotEmpty(pickTicketDetail.get().getPickTicketId())
                    ? pickTicketDetail.get().getPickTicketId().replaceAll("[^\\d]", "")
                    : "";
    return pickTicketNumber;
  }

  public CustomerPOStatusUpdate createPOStatusUpdate(
      LineStatus lineStatus,
      String pickTicketNumber,
      FmsOrderEvent fmsOrderEvent,
      Map<ItemKey, ChangeReason> itemStatus) throws JsonProcessingException {
    String shipNode = fmsOrderEvent.getEventPayload().getShipNode().getId();
    String fcid = fcDetailsConfig.getShipNodeToFcidMap().get(shipNode);
    var fmsOrder = fmsOrderEvent.getEventPayload();
    var fulfillmentLinesList = fmsOrder.getFulfillmentLines();
    var purchaseOrderLines = new ArrayList<OrderStatusUpdatePayload.PurchaseOrderLine>();
    for (FulfillmentLines fulfillmentLines : fulfillmentLinesList) {
      ItemKey itemKey = getItemKey(fcid,
              fulfillmentLines.getItemDetails().getGtin(),
              fulfillmentLines.getItemDetails().getSellerId());

      String changeReason = ChangeReason.Unknown.getCode();
      if(lineStatus == LineStatus.LW) {
        changeReason = ChangeReason.Empty.getCode();
      } else if(lineStatus == LineStatus.LB
              && !ObjectUtils.isEmpty(itemStatus)
              && !ObjectUtils.isEmpty(itemStatus.get(itemKey))) {
        changeReason = itemStatus.get(itemKey).getCode();
      }
      OrderStatusUpdatePayload.PurchaseOrderLine purchaseOrderLine =
              OrderStatusUpdatePayload.PurchaseOrderLine.builder()
              .fulfillmentLineId(fulfillmentLines.getFulfillmentLineId())
              .changeReason(changeReason)
              .quantity(fulfillmentLines.getQuantity())
              .modifiedDate(OffsetDateTime.now())
              //.modifiedDate(DateTimeFormatter.ofPattern(Constants.FMS_STATUS_DATE_FORMAT).format(OffsetDateTime.now()).toUpperCase(Locale.ROOT))
              .status(lineStatus)
              .build();
      purchaseOrderLines.add(purchaseOrderLine);
    }
    //date issues
    var orderStatusUpdatePayload =
        OrderStatusUpdatePayload.builder()
            .purchaseOrderNo(fmsOrder.getPurchaseOrderNo())
            .buId(fmsOrder.getBuId())
            .martId(fmsOrder.getMartId())
            .pickTicketNumber(pickTicketNumber)
            .purchaseOrderLines(purchaseOrderLines)
            .build();
    return CustomerPOStatusUpdate.builder()
        .eventId(fmsOrder.getPurchaseOrderNo(), lineStatus)
        .eventName(EventName.ORDER_STATUS_UPDATE.name())
        .eventSource(Constants.FMS_ORDER_EVENT_SOURCE)
        .eventTime(fmsOrderEvent.getEventTime())
        .eventPayload(orderStatusUpdatePayload)
        .build();
  }

  public void saveFmsOrder(
      CustomerPurchaseOrderDAO.Status status,
      String fcid,
      String purchaseOrderNo,
      List<PickTicketDetails> pickTicketDetails,
      FmsOrder fmsOrder) {
    String partitionKey = String.format("%s_%s", fcid, purchaseOrderNo);
    log.info("Saving {} Order for partitionKey: {}", status.toString(), partitionKey);
    Optional<CustomerPurchaseOrderDAO> fmsOrderDao =
        customerPurchaseOrderRepository.findById(CUSTOMER_ORDER, new PartitionKey(partitionKey));
    if (fmsOrderDao.isEmpty()) {
      customerPurchaseOrderRepository.save(
          CustomerPurchaseOrderDAO.builder()
              .fmsOrder(fmsOrder)
              .purchaseOrderNo(purchaseOrderNo)
              .pickTicketDetails(pickTicketDetails)
              .fcid(fcid)
              .status(status)
              .partitionKey(partitionKey)
              .build());
      log.info("successfully saved order to cosmos for partition key - {}", partitionKey);
    } else {
      log.info("Order already exist in cosmos for partition key - {}", partitionKey);
    }
  }

  public void sendOrderUpdate(
      LineStatus lineStatus,
      List<PickTicketDetails> pickTicketDetails,
      FmsOrderEvent fmsOrderEvent,
      Map<ItemKey, ChangeReason> itemStatus) throws FMSOrderException {
    var fmsOrder = fmsOrderEvent.getEventPayload();
    String key = fmsOrder.getPurchaseOrderNo();
    try {
      String payload = "";
      String pickTicketNumber = getPickTicketNumber(pickTicketDetails);
      CustomerPOStatusUpdate customerPOStatusUpdate =
              createPOStatusUpdate(lineStatus, pickTicketNumber, fmsOrderEvent, itemStatus);
      payload = mapper.writeValueAsString(customerPOStatusUpdate);
      log.info("sending order update with status {} to FMS for PurchaseOrderNo: {}",lineStatus, key);
      fmsKafkaPublisher.send(key, payload);
    } catch (Exception e) {
      String errMsg =
              String.format(
                      "Exception occurred while processing FMSOrder event contract for purchaseOrderNo: %s and lineStatus: %s", key, lineStatus);
      throw new FMSOrderException(errMsg);
    }
  }

  private ItemKey getItemKey(String fcid, String gtin, String sellerId) {
    return ItemKey.builder().fcid(fcid).sellerId(sellerId).gtin(gtin).build();
  }
}