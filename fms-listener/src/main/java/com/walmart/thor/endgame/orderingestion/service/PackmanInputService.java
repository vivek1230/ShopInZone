package com.walmart.thor.endgame.orderingestion.service;

import com.azure.cosmos.implementation.guava25.collect.Streams;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.walmart.thor.endgame.orderingestion.common.dao.Bag;
import com.walmart.thor.endgame.orderingestion.common.dao.ContainerKind;
import com.walmart.thor.endgame.orderingestion.common.dao.ItemProjection;
import com.walmart.thor.endgame.orderingestion.common.dao.ItemProjection.FcAttributes;
import com.walmart.thor.endgame.orderingestion.common.dao.ItemProjection.Shipping;
import com.walmart.thor.endgame.orderingestion.common.dao.ItemProjection.Storage;
import com.walmart.thor.endgame.orderingestion.common.dao.StockBox;
import com.walmart.thor.endgame.orderingestion.common.dto.FmsOrderEvent;
import com.walmart.thor.endgame.orderingestion.common.dto.domain.PackagingType;
import com.walmart.thor.endgame.orderingestion.common.dto.domain.UNClassification;
import com.walmart.thor.endgame.orderingestion.common.dto.fmsorder.FmsOrder;
import com.walmart.thor.endgame.orderingestion.common.repository.BagCatalogRepository;
import com.walmart.thor.endgame.orderingestion.common.repository.FMSOrderRepository;
import com.walmart.thor.endgame.orderingestion.common.repository.IStockBoxRepository;
import com.walmart.thor.endgame.orderingestion.common.repository.ItemProjectionRepository;
import com.walmart.thor.endgame.orderingestion.config.FmsListenerConfig;
import com.walmart.thor.endgame.orderingestion.common.dto.fmsListener.BagDto;
import com.walmart.thor.endgame.orderingestion.common.dto.fmsListener.PackmanRequest;
import com.walmart.thor.endgame.orderingestion.common.dto.fmsListener.PackmanRequest.ProtectionLevel;
import com.walmart.thor.endgame.orderingestion.common.dto.fmsListener.PackmanRequest.Sku;
import com.walmart.thor.endgame.orderingestion.common.dto.fmsListener.StockBoxDto;
import com.walmart.thor.endgame.orderingestion.common.dto.fmsListener.PackmanRequest.TemperatureZone;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.walmart.thor.endgame.orderingestion.common.config.ccm.ItemConfig;
import com.walmart.thor.endgame.orderingestion.config.ItemConfigWrapper;
import io.strati.ccm.utils.client.annotation.ManagedConfiguration;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.walmart.thor.endgame.orderingestion.common.constants.Constants.FAT_ORDER_SUFFIX;

@Service
@AllArgsConstructor
@Slf4j
public class PackmanInputService {

  ItemProjectionRepository itemProjectionRepository;
  IStockBoxRepository stockBoxRepository;
  BagCatalogRepository bagCatalogRepository;
  PackmanRequestPublisher packmanRequestPublisher;
  ObjectMapper objectMapper;
  FmsListenerConfig fmsListenerConfig;
  FMSOrderRepository fmsOrderRepository;
  ItemConfigWrapper itemConfigWrapper;

  private ProtectionLevel getProtectionlevel(
      final FcAttributes fcAttributes,
      final boolean isHazmat) {
    if (isHazmat) {
      return ProtectionLevel.BOX;
    }
    return getProtectionlevelForPackagingType(fcAttributes.getShipping().getPackagingType());
  }

  private ProtectionLevel getProtectionlevelForPackagingType(
      final PackagingType packagingType) {
    switch (packagingType) {
      case PolyBag: {
        return ProtectionLevel.BAG;
      }
      case BubbleMailer: {
        return ProtectionLevel.MAILER;
      }
      case Box: {
        return ProtectionLevel.BOX;
      }
      case ShipAsIs:
      default: {
        return ProtectionLevel.NONE;
      }
    }
  }

  private FcAttributes adjustFcAttributes(
      FcAttributes fcAttributes) {
    double adjustedHeight = fcAttributes.getStorage().getEachHeight() + itemConfigWrapper.getBufferDimensions();
    double adjustedWidth = fcAttributes.getStorage().getEachWidth() + itemConfigWrapper.getBufferDimensions();
    double adjustedLength = fcAttributes.getStorage().getEachLength() + itemConfigWrapper.getBufferDimensions();

    return fcAttributes.withStorage(
        fcAttributes.getStorage().withEachLength(adjustedLength).withEachHeight(adjustedHeight)
            .withEachWidth(adjustedWidth));
  }

  //TODO- get input from products on requireOutBoundPrep
  private boolean checkHazmat(
      final ItemProjection itemProjection) {

    return itemProjection.getTransportationModes().entrySet().stream()
        .anyMatch(t -> t.getValue().isORMD() || (!Objects.isNull(t.getValue().getUnClassification())
            && t.getValue().getUnClassification() != UNClassification.None));
  }

  private List<Sku> getSkus(
      final List<ItemProjection> itemProjections,
      final FmsOrderEvent fmsOrderEvent, String fcId) {
    List<Sku> skus = new ArrayList<>();
    for (FmsOrder.FulfillmentLines fl:fmsOrderEvent.getEventPayload().getFulfillmentLines()) {
      String gtin = fl.getItemDetails().getGtin();

      // the check for itemprojection existence happened earlier as part of fms listener so we can
      // directly use get() to get the correct value.
      ItemProjection itemProjection = itemProjections.stream()
          .filter(ip -> ip.getGtin().equals(gtin))
          .findFirst().get();

      FcAttributes fcAttributes = itemProjection.getFcMap().get(fcId);
      if (Objects.isNull(fcAttributes)) {
        log.error("Could not map orderItem to sku, fcAttributes are not present for item: {}  "
                + "purchaseOrderNo: {}"
                + "fcId: {}",
            gtin, fmsOrderEvent.getEventPayload().getPurchaseOrderNo(), fcId);
        // continue to the next item.
        continue;
      }
      boolean isHazmat = checkHazmat(itemProjection);
      if (isHazmat) {
        fcAttributes =  adjustFcAttributes(fcAttributes);
      }
      Storage storage = fcAttributes.getStorage();
      Shipping shipping = fcAttributes.getShipping();
      skus.add(Sku.builder().skuId(gtin)
          .length(storage.getEachLength())
          .weight(storage.getEachWeight())
          .height(storage.getEachHeight())
          .width(storage.getEachWidth())
          .temperatureZone(TemperatureZone.REGULAR)
          .protectionLevel(getProtectionlevel(fcAttributes, isHazmat))
          .shipsAlone(shipping.isShipAlone())
          .limitedQty(false)
          .qty(fl.getQuantity().getMeasurementValue())
          .foldable(shipping.isNonRigid())
          .alcohol(null).build());
    }
    return skus;
  }


  public PackmanRequest buildPackmanRequest(
      final String orderId,
      final Iterable<StockBox> stockBoxList, Iterable<Bag> bags, List<Sku> skus) {
    var bagDtos = getBagCatalogForRequest(bags);
    var boxDtos = getBoxCatalogForRequest(stockBoxList, orderId);
    return PackmanRequest.builder().orderId(orderId).skus(skus)
        .bags(bagDtos).boxes(boxDtos).build();
  }

  public void sendPackmanRequest(
      PackmanRequest packmanRequest, String purchaseOrderNo, String fcId) {
    log.info("sending packman request for PurchaseOrderNo: {}", purchaseOrderNo);
    String payload = "";
    try {
      payload = objectMapper.writeValueAsString(packmanRequest);
    } catch (Exception e) {
      log.error("error in serialization of packmanRequest status, key: {}", purchaseOrderNo);
    }
    packmanRequestPublisher.send(purchaseOrderNo, payload, fcId);
  }

  public void handleInput(FmsOrderEvent fmsOrderEvent, List<ItemProjection> itemProjections,
      String fcId) {
    FmsOrder fmsOrder = fmsOrderEvent.getEventPayload();
    var bags = bagCatalogRepository.findAll(new PartitionKey(fcId));
    var stockBoxes = stockBoxRepository.findAll(new PartitionKey(fcId));
    List<Sku> skus = getSkus(itemProjections, fmsOrderEvent, fcId);
    var packmanRequest = buildPackmanRequest(fmsOrder.getPurchaseOrderNo(),
        stockBoxes, bags, skus);
    sendPackmanRequest(packmanRequest, fmsOrder.getPurchaseOrderNo(), fcId);
  }

  private List<BagDto> getBagCatalogForRequest(Iterable<Bag> bags) {

    return Streams.stream(bags)
        .map(bag -> BagDto.builder().containerId(bag.getId())
            .containerKind(bag.getContainerKind().getValue())
            .length(bag.getLength()).width(bag.getWidth()).height(bag.getHeight())
            .maxHeight(bag.getMaxHeight()).maxAllowableWeight(bag.getMaxAllowableWeight())
            .angle(bag.getAngle())
            .build()).collect(Collectors.toList());
  }

  private List<StockBoxDto> getBoxCatalogForRequest(Iterable<StockBox> boxes, String orderId) {

    return Streams.stream(boxes)
            .filter(box -> !box.isTestOnly() || orderId.contains(FAT_ORDER_SUFFIX)) //Include everything For FAT and Exclude testOnly Boxes For Non-FAT orders.
            .map(box -> StockBoxDto.builder().containerId(box.getId())
        .containerKind(ContainerKind.BOX.getValue()).length(box.getLength()).width(box.getWidth())
        .height(box.getHeight()).weight(box.getWeight())
        .maxAllowableWeight(box.getMaxAllowableWeight()).build())
        .collect(Collectors.toList());
  }


}
