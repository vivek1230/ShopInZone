package com.walmart.thor.endgame.orderingestion.service.orderhandler;

import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.walmart.thor.endgame.orderingestion.common.constants.Constants;
import com.walmart.thor.endgame.orderingestion.common.dao.CustomerPurchaseOrderDAO;
import com.walmart.thor.endgame.orderingestion.common.dao.CustomerPurchaseOrderDAO.PickTicketDetails;
import com.walmart.thor.endgame.orderingestion.common.dto.ItemIdentifier;
import com.walmart.thor.endgame.orderingestion.common.dto.domain.ChangeReason;
import com.walmart.thor.endgame.orderingestion.common.dto.domain.EventName;
import com.walmart.thor.endgame.orderingestion.common.dto.domain.LineStatus;
import com.walmart.thor.endgame.orderingestion.common.dto.fmsorder.BoxingDetails;
import com.walmart.thor.endgame.orderingestion.common.dto.fmsorder.FmsOrder;
import com.walmart.thor.endgame.orderingestion.common.dto.fmsorder.FmsOrder.FulfillmentLines;
import com.walmart.thor.endgame.orderingestion.common.dto.fmsupdates.CustomerPOStatusUpdate;
import com.walmart.thor.endgame.orderingestion.common.dto.fmsupdates.OrderStatusUpdatePayload;
import com.walmart.thor.endgame.orderingestion.common.repository.CustomerPurchaseOrderRepository;
import com.walmart.thor.endgame.orderingestion.config.FCDetailsConfig;
import com.walmart.thor.endgame.orderingestion.dto.FmsOrderEvent;
import com.walmart.thor.endgame.orderingestion.dto.allocation.*;
import com.walmart.thor.endgame.orderingestion.exception.FMSOrderRuntimeException;
import com.walmart.thor.endgame.orderingestion.service.FmsKafkaPublisher;
import com.walmart.thor.endgame.orderingestion.service.MicrometerService;
import com.walmart.thor.endgame.orderingestion.service.PickTicketIDGenerator;
import com.walmart.thor.endgame.orderingestion.service.CreateAllocationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.time.OffsetDateTime;
import java.util.*;

import static com.walmart.thor.endgame.orderingestion.common.constants.Constants.CUSTOMER_ORDER;

@Service
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class FmsOrderHandlerInventoryProjection implements FmsOrderHandler{

    private PickTicketIDGenerator pickTicketIDGenerator;

    private CustomerPurchaseOrderRepository customerPurchaseOrderRepository;

    private FCDetailsConfig fcDetailsConfig;

    private FmsKafkaPublisher fmsKafkaPublisher;

    private ObjectMapper mapper;

    private MicrometerService micrometerService;

    private CreateAllocationService createAllocationService;

    private static Map<ItemKey, ChangeReason> getItemKeyChangeReasonMap(CreateAllocationResponse createAllocationResponse) {
        Map<ItemKey, ChangeReason> itemStatus = new HashMap<>();
        createAllocationResponse.getError().getItemStatusList().forEach(item -> {
            ItemKey itemKey1 = ItemKey.builder()
                    .fcid(item.getItemKey().getFcid())
                    .sellerId(item.getItemKey().getSellerId())
                    .gtin(item.getItemKey().getGtin()).build();
            if (item.getAvailableInventoryQty() == 0) {
                itemStatus.put(itemKey1, ChangeReason.ZeroInventory);
            } else if (item.getRequestedInventoryQty() > item.getAvailableInventoryQty()) {
                itemStatus.put(itemKey1, ChangeReason.NotEnoughInventory);
            } else {
                itemStatus.put(itemKey1, ChangeReason.KillOrFill);
            }
        });
        return itemStatus;
    }

    private void repeated(FmsOrderEvent fmsOrderEvent, CustomerPurchaseOrderDAO fmsOrderDao) {
        log.info(
                "Order is already processed and saved in cosmos fcid: {}, purchaseOrderNo : {}, status: {}",
                fmsOrderDao.getFcid(), fmsOrderDao.getPurchaseOrderNo(), fmsOrderDao.getStatus());
        List<PickTicketDetails> pickTicketDetails = fmsOrderDao.getPickTicketDetails();
        String pickTicketNumber = getPickTicketNumber(pickTicketDetails);
        // sending update to FMS
        if (fmsOrderDao.getStatus().equals(CustomerPurchaseOrderDAO.Status.Accepted)) {
            sendOrderUpdate(LineStatus.LW, pickTicketNumber, fmsOrderEvent, null);
        } else if (fmsOrderDao.getStatus().equals(CustomerPurchaseOrderDAO.Status.Rejected)) {
            sendOrderUpdate(LineStatus.LB, pickTicketNumber, fmsOrderEvent, null);
        }
    }

    private void failure(FmsOrderEvent fmsOrderEvent, FmsOrder fmsOrder, String fcid, String purchaseOrderNo, CreateAllocationResponse createAllocationResponse) {
        log.info(
                "Failure: Inventory is not available for fcid: {}, purchaseOrderNo : {}", fcid, purchaseOrderNo);
        Map<ItemKey, ChangeReason> itemStatus = getItemKeyChangeReasonMap(createAllocationResponse);

        saveFmsOrder(
                CustomerPurchaseOrderDAO.Status.Rejected,
                fcid,
                purchaseOrderNo,
                new ArrayList<>(),
                fmsOrder);
        micrometerService.getRejectedOrderMetric(fcid, LineStatus.LB.getCode()).increment();
        sendOrderUpdate(LineStatus.LB, "", fmsOrderEvent, itemStatus);

    }

    private void success(FmsOrderEvent fmsOrderEvent, FmsOrder fmsOrder, String fcid, String purchaseOrderNo, List<PickTicketDetails> pickTicketDetails) {
        log.info(
                "Success: Inventory is available for fcid: {}, purchaseOrderNo : {}", fcid, purchaseOrderNo);
        saveFmsOrder(
                CustomerPurchaseOrderDAO.Status.Accepted,
                fcid,
                purchaseOrderNo,
                pickTicketDetails,
                fmsOrder);
        String pickTicketNumber = getPickTicketNumber(pickTicketDetails);
        // sending update to FMS for accepted order
        micrometerService.getAcceptedOrderMetric(fcid, LineStatus.LW.getCode()).increment();
        sendOrderUpdate(LineStatus.LW,
                pickTicketNumber,
                fmsOrderEvent,
                null);

    }

    public void process(FmsOrderEvent fmsOrderEvent) {

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

            fmsOrderDao.ifPresentOrElse(fo ->
                            repeated(fmsOrderEvent, fo),
                    () -> {
                        List<PickTicketDetails> pickTicketDetails =
                                generatePickticketDetails(fcid, fmsOrder.getBoxingDetails());
                        CreateAllocationResponse createAllocationResponse = getCreateAllocationResponse(fmsOrder, fcid, purchaseOrderNo, pickTicketDetails);
                        if (!ObjectUtils.isEmpty(createAllocationResponse.getSuccess())
                                && !CollectionUtils.isEmpty(createAllocationResponse.getSuccess().getAllocationList())) {
                            success(fmsOrderEvent, fmsOrder, fcid, purchaseOrderNo, pickTicketDetails);
                        } else if (!ObjectUtils.isEmpty(createAllocationResponse.getError())
                                && !CollectionUtils.isEmpty(createAllocationResponse.getError().getItemStatusList())) {
                            failure(fmsOrderEvent, fmsOrder, fcid, purchaseOrderNo, createAllocationResponse);
                        } else if (!ObjectUtils.isEmpty(createAllocationResponse.getError())
                                && !ObjectUtils.isEmpty(createAllocationResponse.getError().getErrorCode())){
                            throw new FMSOrderRuntimeException(createAllocationResponse.getError().getErrorDesc());
                        }

                    });

        }
    }

    private CreateAllocationResponse getCreateAllocationResponse(FmsOrder fmsOrder, String fcId, String purchaseOrderNo, List<PickTicketDetails> pickTicketDetailsList) {
        Map<String, ItemIdentifier> lineToItemMap = fmsOrder.getLineToItemMap();
        List<PickTicket> pickTicketList = new ArrayList<>();
        pickTicketDetailsList.forEach(pickTicketDetails -> {
            List<Item> itemList = new ArrayList<>();
            pickTicketDetails.getBoxingDetails().getLineDetails().forEach(line -> {
                ItemIdentifier itemIdentifier = lineToItemMap.get(line.getFulfillmentLineId());
                ItemKey itemKey = ItemKey.builder()
                        .fcid(fcId)
                        .gtin(itemIdentifier.getGtin())
                        .sellerId(itemIdentifier.getSellerId())
                        .build();
                itemList.add(Item.builder()
                        .quantity(line.getQuantity().getMeasurementValue())
                        .itemKey(itemKey)
                        .build());
            });
            pickTicketList.add(PickTicket.builder()
                    .itemList(itemList)
                    .pickTicketId(pickTicketDetails.getPickTicketId())
                    .build());
        });

        CreateAllocationRequest createAllocationRequest = CreateAllocationRequest.builder()
                .pickTicketList(pickTicketList)
                .purchaseOrderNumber(purchaseOrderNo)
                .build();
        return createAllocationService.callCreateAllocation(fcId, createAllocationRequest);
    }



    private String getPickTicketNumber(List<PickTicketDetails> pickTicketDetails){
        var pickTicketDetail =
                pickTicketDetails.stream()
                        .filter(p -> StringUtils.isNotEmpty(p.getPickTicketId()))
                        .findAny();

        return pickTicketDetail.isPresent()
                && StringUtils.isNotEmpty(pickTicketDetail.get().getPickTicketId())
                ? pickTicketDetail.get().getPickTicketId().replaceAll("[^\\d]", "")
                : "";
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
            String pickTicketNumber,
            FmsOrderEvent fmsOrderEvent,
            Map<ItemKey, ChangeReason> itemStatus)  {
        var fmsOrder = fmsOrderEvent.getEventPayload();
        String key = fmsOrder.getPurchaseOrderNo();
        try {
            String payload;
            CustomerPOStatusUpdate customerPOStatusUpdate =
                    createPOStatusUpdate(lineStatus, pickTicketNumber, fmsOrderEvent, itemStatus);
            payload = mapper.writeValueAsString(customerPOStatusUpdate);
            log.info("sending order update with status {} to FMS for PurchaseOrderNo: {}",lineStatus, key);
            fmsKafkaPublisher.send(key, payload);
        } catch (Exception e) {
            String errMsg =
                    String.format(
                            "Exception occurred while processing FMSOrder event contract for purchaseOrderNo: %s and lineStatus: %s", key, lineStatus);
            throw new FMSOrderRuntimeException(errMsg);
        }
    }

    private List<PickTicketDetails> generatePickticketDetails(
            String fcid, List<BoxingDetails> boxingDetails) {
        log.info("Generating pickticket details for fcid: {}", fcid);
        List<PickTicketDetails> pickTicketDetailsList = new ArrayList<>();
        List<String> pickTicketIds =
                pickTicketIDGenerator.getNextPickTicketIDs(fcid, boxingDetails.size());
        HashMap<Integer, BoxingDetails> boxingDetailsMap =
                boxingDetails.stream()
                        .collect(
                                HashMap::new,
                                (map, streamValue) -> map.put(map.size(), streamValue),
                                (map, map2) -> {});
        boxingDetailsMap.forEach(
                (index, box) ->
                        pickTicketDetailsList.add(
                                PickTicketDetails.builder()
                                        .boxingDetails(box)
                                        .pickTicketId(pickTicketIds.isEmpty() ? StringUtils.EMPTY : pickTicketIds.get(index) )
                                        .build())
        );
        return pickTicketDetailsList;
    }

    private ItemKey getItemKey(String fcid, String gtin, String sellerId) {
        return ItemKey.builder().fcid(fcid).sellerId(sellerId).gtin(gtin).build();
    }
}