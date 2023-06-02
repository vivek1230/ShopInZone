package com.walmart.thor.endgame.orderingestion.domain;

import static java.util.stream.Collectors.groupingBy;

import com.azure.cosmos.implementation.apachecommons.lang.tuple.ImmutablePair;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.walmart.thor.endgame.orderingestion.common.dto.FmsOrderEvent;
import com.walmart.thor.endgame.orderingestion.common.dto.fmsorder.FmsOrder;
import com.walmart.thor.endgame.orderingestion.common.dto.fmsorder.FmsOrder.Address;
import com.walmart.thor.endgame.orderingestion.common.dto.fmsorder.FmsOrder.Contact;
import com.walmart.thor.endgame.orderingestion.common.dto.fmsorder.FmsOrder.FulfillmentLines;
import com.walmart.thor.endgame.orderingestion.common.dto.fmsorder.FmsOrder.LineDates;
import com.walmart.thor.endgame.orderingestion.common.dto.fmsorder.FmsOrder.Name;
import com.walmart.thor.endgame.orderingestion.common.dto.fmsorder.FmsOrder.ShippingTo;
import com.walmart.thor.endgame.orderingestion.config.FmsListenerConfig;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class OrderDomain {

  FmsListenerConfig fmsListenerConfig;

  public boolean isValidOrder(FmsOrderEvent fmsOrderEvent) {
    FmsOrder fmsOrder = fmsOrderEvent.getEventPayload();
    return isNotEmpty(fmsOrder.getBuId()) && isNotEmpty(fmsOrder.getMartId())
        && isNotEmpty(fmsOrder.getOrderChannelId()) && isNotEmpty(fmsOrder.getPurchaseOrderNo())
        && isNotEmpty(fmsOrder.getSourceOrderId()) && isNotEmpty(fmsOrder.getCustomerOrderId())
        && isNotEmpty(fmsOrder.getFulfillmentType()) && isNotEmpty(fmsOrder.getShippingMethod())
        && isNotEmpty(fmsOrder.getCarrierMethodId()) && isValid(fmsOrder.getLineDates())
        && isValid(fmsOrder.getShippingTo()) && hasValidFulfillmentLines(
        fmsOrder.getFulfillmentLines());
  }

  private boolean hasValidFulfillmentLines(List<FulfillmentLines> fulfillmentLines) {

    //identify violating lines, more than one fulfillment line for each gtin & sellerId pair
    Map<Pair<String, String>, List<FulfillmentLines>> fulfilmentMap = fulfillmentLines.stream()
        .collect(groupingBy(fl->new ImmutablePair<>(fl.getItemDetails().getGtin(),
            fl.getItemDetails().getSellerId())));

    return fulfilmentMap.entrySet().stream()
        .allMatch(entry->CollectionUtils.isNotEmpty(entry.getValue())
            && entry.getValue().size() ==1
            && isNotEmpty(entry.getValue().get(0).getFulfillmentLineId()));

  }

  private boolean isValid(Contact contact) {
    Name name = contact.getName();
    return !Objects.isNull(name) && isNotEmpty(name.getFirstName()) && isNotEmpty(
        name.getLastName());
  }

  private boolean isValid(Address address) {
    return !Objects.isNull(address) && isNotEmpty(address.getLine1()) && isNotEmpty(
        address.getCity()) && isNotEmpty(address.getState()) && isNotEmpty(address.getZip());
  }


  private boolean isValid(ShippingTo shippingTo) {
    Contact contact = shippingTo.getContact();
    Address address = shippingTo.getAddress();

    return isValid(contact) && isValid(address);
  }

  private boolean isValid(LineDates lineDates) {
    return Objects.nonNull(lineDates.getExpectedShipDate()) && Objects.nonNull(
        lineDates.getOrderProcessingDate());
  }

  private boolean isNotEmpty(String str) {
    return StringUtils.isNotEmpty(str);
  }

}
