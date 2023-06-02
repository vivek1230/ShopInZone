package com.walmart.thor.endgame.orderingestion.service;

import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.walmart.thor.endgame.orderingestion.common.dao.FmsOrderDAO;
import com.walmart.thor.endgame.orderingestion.common.dao.FmsOrderDAO.FMSOrderStatus;
import com.walmart.thor.endgame.orderingestion.common.dao.ItemProjection;
import com.walmart.thor.endgame.orderingestion.common.dto.FmsOrderEvent;
import com.walmart.thor.endgame.orderingestion.common.dto.domain.ChangeReason;
import com.walmart.thor.endgame.orderingestion.common.dto.domain.EventName;
import com.walmart.thor.endgame.orderingestion.common.dto.domain.LineStatus;
import com.walmart.thor.endgame.orderingestion.common.dto.fmsListener.FOREJContract;
import com.walmart.thor.endgame.orderingestion.common.dto.fmsorder.BoxingDetails;
import com.walmart.thor.endgame.orderingestion.common.dto.fmsorder.FmsOrder;
import com.walmart.thor.endgame.orderingestion.common.dto.fmsorder.FmsOrder.FulfillmentLines;
import com.walmart.thor.endgame.orderingestion.common.dto.fmsorder.FmsOrder.ItemDetails;
import com.walmart.thor.endgame.orderingestion.common.dto.fmsorder.Quantity;
import com.walmart.thor.endgame.orderingestion.common.dto.fmsupdates.CustomerPOStatusUpdate;
import com.walmart.thor.endgame.orderingestion.common.dto.fmsupdates.OrderStatusUpdatePayload;
import com.walmart.thor.endgame.orderingestion.common.dto.fmsupdates.OrderStatusUpdatePayload.PurchaseOrderLine;
import com.walmart.thor.endgame.orderingestion.common.repository.FMSOrderRepository;
import com.walmart.thor.endgame.orderingestion.common.repository.ItemProjectionRepository;
import com.walmart.thor.endgame.orderingestion.config.FmsListenerConfig;
import com.walmart.thor.endgame.orderingestion.domain.OrderDomain;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.netty.util.internal.StringUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.Predicate;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Service;

import static com.walmart.thor.endgame.orderingestion.common.constants.Constants.FMS_ORDER_EVENT_SOURCE;
import static com.walmart.thor.endgame.orderingestion.common.constants.Constants.FMS_EVENT_NAME;
import static com.walmart.thor.endgame.orderingestion.common.constants.Constants.FMS_EVENT_KEY;
import static com.walmart.thor.endgame.orderingestion.common.constants.Constants.FACILITY_NUM;
import static com.walmart.thor.endgame.orderingestion.common.constants.Constants.FMS_ORDER;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class FmsListenerService {


  OrderDomain orderDomain;
  FMSOrderRepository fmsOrderRepository;
  ItemProjectionRepository itemProjectionRepository;
  ObjectMapper objectMapper;
  FMSKafkaPublisher fmsKafkaPublisher;
  EndgameKafkaPublisher endgameKafkaProducer;
  FmsListenerConfig fmsListenerConfig;
  PackmanInputService packmanInputService;
  MicrometerService micrometerService;

  private boolean hasValidBoxingDetails(
      final List<BoxingDetails> boxingDetails) {
    return CollectionUtils.isNotEmpty(boxingDetails);
  }

  public CustomerPOStatusUpdate createPOStatusUpdate(
      final LineStatus lineStatus,
      final FmsOrderEvent fmsOrderEvent,
      final List<PurchaseOrderLine> purchaseOrderLines) {
    var fmsOrder = fmsOrderEvent.getEventPayload();
    var orderStatusUpdatePayload =
        OrderStatusUpdatePayload.builder()
            .purchaseOrderNo(fmsOrder.getPurchaseOrderNo())
            .buId(fmsOrder.getBuId())
            .martId(fmsOrder.getMartId())
            .pickTicketNumber(null)
            .purchaseOrderLines(purchaseOrderLines)
            .build();
    return CustomerPOStatusUpdate.builder()
        .eventId(fmsOrder.getPurchaseOrderNo(), lineStatus)
        .eventName(EventName.ORDER_STATUS_UPDATE.name())
        .eventSource(FMS_ORDER_EVENT_SOURCE)
        .eventTime(fmsOrderEvent.getEventTime())
        .eventPayload(orderStatusUpdatePayload)
        .build();
  }

  public FmsOrderDAO saveFmsOrder(
      final String fcid,
      final FmsOrderEvent fmsOrderEvent) {
    FmsOrder fmsOrder = fmsOrderEvent.getEventPayload();
    String purchaseOrderNo = fmsOrder.getPurchaseOrderNo();
    String partitionKey = FmsOrderDAO.getPartitionKey(fcid, purchaseOrderNo);
    log.info("Saving validated FMS Order for partitionKey: {}", partitionKey);
    FmsOrderDAO fmsOrderDAO = fmsOrderRepository.save(
        FmsOrderDAO.builder()
            .fmsOrderEvent(fmsOrderEvent)
            .purchaseOrderNo(purchaseOrderNo)
            .fcId(fcid)
            .partitionKey(partitionKey)
            .build());
    log.info("successfully saved order to cosmos for partition key - {}", partitionKey);
    return fmsOrderDAO;
  }

  public void sendOrderUpdate(
      final LineStatus lineStatus,
      final FmsOrderEvent fmsOrderEvent,
      final List<PurchaseOrderLine> purchaseOrderLines) {
    var fmsOrder = fmsOrderEvent.getEventPayload();
    String key = fmsOrder.getPurchaseOrderNo();
    log.info("sending order update to FMS for PurchaseOrderNo: {}", key);
    CustomerPOStatusUpdate customerPOStatusUpdate =
        createPOStatusUpdate(lineStatus, fmsOrderEvent, purchaseOrderLines);
    String payload = "";
    try {
      payload = objectMapper.writeValueAsString(customerPOStatusUpdate);
    } catch (Exception e) {
      log.error("error in serialization of fmsorder status, key: {}", key);
    }
    fmsKafkaPublisher.send(key, payload);
  }

  public void publishOrderToEndgame(
      final FmsOrderEvent fmsOrderEvent, final String fcId) {
    var fmsOrder = fmsOrderEvent.getEventPayload();
    String key = fmsOrder.getPurchaseOrderNo();
    log.info("publishing fmsorder to topic : {}", key);
    String payload = "";
    try {
      payload = objectMapper.writeValueAsString(fmsOrderEvent);
    } catch (Exception e) {
      log.error("error in serialization of fmsorder status, key: {}", key);
    }
    MessageHeaderAccessor headers = new MessageHeaderAccessor();
    headers.setHeader(FMS_EVENT_KEY, FMS_EVENT_NAME.FULFILLMENT_ORDER_CREATE.name());
    headers.setHeader(FACILITY_NUM, fcId);
    endgameKafkaProducer.send(key, payload, headers);
  }

  List<PurchaseOrderLine> getPurchaseOrderLines(
      final FmsOrder fmsOrder,
      final List<String> gtins,
      final LineStatus ls,
      final ChangeReason errorReason) {

    return fmsOrder.getFulfillmentLines().stream().filter(
        fl -> CollectionUtils.isNotEmpty(gtins) && gtins.contains(fl.getItemDetails().getGtin()))
        .map(fl -> PurchaseOrderLine.builder()
            .fulfillmentLineId(fl.getFulfillmentLineId())
            .modifiedDate(OffsetDateTime.now())
            .status(ls)
            .changeReason(Objects.nonNull(errorReason) ? errorReason.getCode() : null)
            .quantity(Quantity.builder()
                .unitOfMeasure(fl.getQuantity().getUnitOfMeasure())
                .measurementValue(fl.getQuantity().getMeasurementValue())
                .build())
            .build()).collect(Collectors.toList());
  }

  private void processOrder(
      final List<String> allGtins,
      final List<ItemDetails> itemDetailsList,
      final FmsOrderDAO fmsOrderDAO) {
    List<ItemProjection> itemProjections = new ArrayList<>();
    allGtins.forEach(
        gtin -> itemProjectionRepository
            .findAll(new PartitionKey(ItemProjection.getPartitionKey(gtin)))
            .forEach(itemProjections::add)
    );

    FmsOrderEvent fmsOrderEvent = fmsOrderDAO.getFmsOrderEvent();

    if (CollectionUtils.isNotEmpty(itemProjections) && itemDetailsList.size() == itemProjections
        .size()) {
      if (hasFcMap(itemProjections, fmsOrderDAO.getFcId())) {
        boolean hasValidBoxingDetails = hasValidBoxingDetails(
            fmsOrderEvent.getEventPayload().getBoxingDetails());
        if (hasValidBoxingDetails) {
          updateFMSOrderDAO(fmsOrderDAO, FMSOrderStatus.PACKMAN_RECOMMENDATION_NOT_REQUIRED);
          publishOrderToEndgame(fmsOrderEvent, fmsOrderDAO.getFcId());
        } else {
          updateFMSOrderDAO(fmsOrderDAO, FMSOrderStatus.PACKMAN_RECOMMENDATION_REQUESTED);
          packmanInputService.handleInput(fmsOrderEvent, itemProjections, fmsOrderDAO.getFcId());
        }
      } else {
        updateFMSOrderDAO(fmsOrderDAO, FMSOrderStatus.FC_MAP_MISSING);
        sendUpdateToFMSForNoxExistentFcMap(itemProjections, fmsOrderEvent, fmsOrderDAO.getFcId());
      }
    } else {
      updateFMSOrderDAO(fmsOrderDAO, FMSOrderStatus.LINE_UNIDENTIFIED);
      sendUpdateToFMSForInvalidGtins(allGtins, itemProjections, fmsOrderEvent);
    }
  }

  private void updateFMSOrderDAO(final FmsOrderDAO fmsOrderDAO,
      final FMSOrderStatus fmsOrderStatus) {
    fmsOrderDAO.setStatus(fmsOrderStatus);
    fmsOrderRepository.save(fmsOrderDAO);
  }

  private void sendUpdateToFMSForNoxExistentFcMap(List<ItemProjection> itemProjections,
      FmsOrderEvent fmsOrderEvent, String fcId) {
    Predicate<ItemProjection> validateFcMap = validateFcMap(fcId);

    var partitionedItems = itemProjections.stream().collect(Collectors.
        partitioningBy(validateFcMap::evaluate));
    var validGtins = partitionedItems.get(true).stream().map(ItemProjection::getGtin)
        .collect(Collectors.toList());
    var inValidGtins = partitionedItems.get(false).stream().map(ItemProjection::getGtin)
        .collect(Collectors.toList());
    sendUpdateToFMS(validGtins, inValidGtins, fmsOrderEvent);
  }

  private void sendUpdateToFMSForInvalidGtins(List<String> allGtins,
      List<ItemProjection> itemProjections, FmsOrderEvent fmsOrderEvent) {

    var validGtins = itemProjections.stream().map(ItemProjection::getGtin).collect(
        Collectors.toList());
    var inValidGtins = allGtins.stream().filter(gtin -> !validGtins.contains(gtin))
        .collect(Collectors.toList());
    sendUpdateToFMS(validGtins, inValidGtins, fmsOrderEvent);
  }

  public void sendUpdateToFMS(List<String> validGtins, List<String> invalidGtins,
      FmsOrderEvent fmsOrderEvent) {

    List<String> metricLineStatus = new ArrayList<>();
    if( CollectionUtils.isNotEmpty(invalidGtins)) {
      metricLineStatus.add(LineStatus.LU.getCode());
      sendOrderUpdate(LineStatus.valueOf("LU"), fmsOrderEvent,
          getPurchaseOrderLines(fmsOrderEvent.getEventPayload(),
              invalidGtins, LineStatus.LU, ChangeReason.LineUnidentified));
    }
    if (CollectionUtils.isNotEmpty(validGtins)) {
      metricLineStatus.add(LineStatus.LB.getCode());
      sendOrderUpdate(LineStatus.valueOf("LB"), fmsOrderEvent,
          getPurchaseOrderLines(fmsOrderEvent.getEventPayload(), validGtins,
              LineStatus.LB, ChangeReason.KillOrFill));
    }
    Optional<String> fcIdOpt = getFcid(fmsOrderEvent);
    micrometerService.getInvalidGtinMetric(fcIdOpt.orElse(""),
            String.join(", ", metricLineStatus)).increment();
  }


  private boolean hasFcMap(List<ItemProjection> itemProjections, String fcId) {
    Predicate<ItemProjection> validateFcMap = validateFcMap(fcId);
    return itemProjections.stream()
        .allMatch(validateFcMap::evaluate);
  }

  private Predicate<ItemProjection> validateFcMap(String fcId) {
    return itemProjection -> MapUtils.isNotEmpty(itemProjection.getFcMap())
        && (itemProjection.getFcMap()).containsKey(fcId);
  }

  private Optional<String> getFcid(FmsOrderEvent fmsOrderEvent) {

    Optional<String> fcIdOpt;
    var fmsOrder = fmsOrderEvent.getEventPayload();
    var shipNode = fmsOrder.getShipNode().getId();
    Map<String, String> shipNodeToFcid = fmsListenerConfig.getShipNodeToFcid();
    if (MapUtils.isNotEmpty(shipNodeToFcid) && shipNodeToFcid.containsKey(shipNode)) {
      fcIdOpt = Optional.of(shipNodeToFcid.get(shipNode));
    } else {
      fcIdOpt = Optional.empty();
    }
    return fcIdOpt;
  }

  public void handle(
      final FmsOrderEvent fmsOrderEvent) {
    log.info("Processing FMS order message {}", fmsOrderEvent.getEventId());
    var fmsOrder = fmsOrderEvent.getEventPayload();
    Optional<String> fcIdOpt = getFcid(fmsOrderEvent);
    if (fcIdOpt.isPresent()) {
      String fcId = fcIdOpt.get();
      micrometerService.getOrdersWithValidFCMetric(fcId).increment();
      var itemDetails = fmsOrder.getFulfillmentLines()
          .stream()
          .map(FulfillmentLines::getItemDetails).collect(Collectors.toList());

      List<String> allGtins = itemDetails.stream().map(ItemDetails::getGtin).collect(
          Collectors.toList());

      if (orderDomain.isValidOrder(fmsOrderEvent)) {
        Optional<FmsOrderDAO> optFmsOrderDAO = fmsOrderRepository.findById(FMS_ORDER,
            new PartitionKey(FmsOrderDAO.getPartitionKey(fcId, fmsOrder.getPurchaseOrderNo())));

        // If order already exists re-process of same FMSOrder message is not allowed.
        if (optFmsOrderDAO.isPresent()) {
          log.info("The FMSOrder for fcID:{} and PurchaseOrderNo: {} already exists.", fcId,
              fmsOrder.getPurchaseOrderNo());
          return;
        }
        micrometerService.getValidOrdersMetric(fcId, LineStatus.LI.getCode()).increment();
        sendOrderUpdate(LineStatus.LI, fmsOrderEvent,
            getPurchaseOrderLines(fmsOrder, allGtins, LineStatus.LI, null));
        FmsOrderDAO fmsOrderDAO = saveFmsOrder(fcId, fmsOrderEvent);
        processOrder(allGtins, itemDetails, fmsOrderDAO);
      } else {
        micrometerService.getInvalidOrdersMetric(fcId, LineStatus.LB.getCode()).increment();
        sendOrderUpdate(LineStatus.LB, fmsOrderEvent,
            getPurchaseOrderLines(fmsOrder, allGtins, LineStatus.LB, ChangeReason.KillOrFill));
      }
    }
  }

  public void sendFOREJ(String key, String payload) throws JsonProcessingException {
    FOREJContract forejContract = new FOREJContract(key, payload);
    fmsKafkaPublisher.send(forejContract.getEventId(), objectMapper.writeValueAsString(forejContract));
    micrometerService.getOrdersUnparsableMetric().increment();
  }
}
