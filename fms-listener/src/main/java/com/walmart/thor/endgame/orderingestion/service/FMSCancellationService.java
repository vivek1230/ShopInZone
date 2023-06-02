package com.walmart.thor.endgame.orderingestion.service;

import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.walmart.thor.endgame.orderingestion.common.dao.FmsOrderDAO;
import com.walmart.thor.endgame.orderingestion.common.dao.FmsOrderDAO.FMSOrderStatus;
import com.walmart.thor.endgame.orderingestion.common.dto.customercancel.FmsCancellationEvent;
import com.walmart.thor.endgame.orderingestion.common.dto.fmsorder.ShipNode;
import com.walmart.thor.endgame.orderingestion.common.repository.FMSOrderRepository;
import com.walmart.thor.endgame.orderingestion.config.FmsListenerConfig;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import com.walmart.thor.endgame.orderingestion.common.constants.Constants.FMS_EVENT_NAME;

import java.util.Map;
import java.util.Optional;

import static com.walmart.thor.endgame.orderingestion.common.constants.Constants.*;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class FMSCancellationService {

    ObjectMapper objectMapper;
    EndgameKafkaPublisher endgameKafkaProducer;
    FmsListenerConfig fmsListenerConfig;
    MicrometerService micrometerService;
    FMSOrderRepository fmsOrderRepository;

    public void handle(FmsCancellationEvent fmsCancellationEvent) {
        Optional<String> fcIdOpt = getFcId(fmsCancellationEvent.getShipNode());
        if(fcIdOpt.isPresent()) {
            micrometerService.getCustomerCancelMetric(fcIdOpt.get()).increment();
            String purchaseOrderNo = fmsCancellationEvent.getPurchaseOrderNo();
            Optional<FmsOrderDAO> optFmsOrderDAO = fmsOrderRepository.findById(FMS_ORDER,
                    new PartitionKey(FmsOrderDAO.getPartitionKey(fcIdOpt.get(), purchaseOrderNo)));
            if(optFmsOrderDAO.isPresent() &&
                    FMSOrderStatus.PACKMAN_RECOMMENDATION_UPDATED.equals(optFmsOrderDAO.get().getStatus())) {
                log.info("publishing customer cancellation for : {}", purchaseOrderNo);
                String payload = "";
                MessageHeaderAccessor headers = new MessageHeaderAccessor();
                headers.setHeader(FMS_EVENT_KEY, FMS_EVENT_NAME.FULFILLMENT_ORDER_CANCEL.name());
                headers.setHeader(FACILITY_NUM, fcIdOpt.get());
                try {
                    payload = objectMapper.writeValueAsString(fmsCancellationEvent);

                } catch (Exception e) {
                    log.error("error in serialization of customer cancellation event, key: {}", purchaseOrderNo);
                }
                endgameKafkaProducer.send(purchaseOrderNo, payload, headers);
            } else {
                micrometerService.getIgnoredCancellationMetric(fcIdOpt.get()).increment();
                log.info("Order cannot be cancelled, Ignoring customer cancellation request for PO : {}", purchaseOrderNo);
            }
        }
    }

    private Optional<String> getFcId(ShipNode shipNode) {
        Optional<String> fcIdOpt = Optional.empty();
        if(!ObjectUtils.isEmpty(shipNode)) {
            String shipNodeId = shipNode.getId();
            Map<String, String> shipNodeToFcid = fmsListenerConfig.getShipNodeToFcid();
            if (MapUtils.isNotEmpty(shipNodeToFcid) && shipNodeToFcid.containsKey(shipNodeId)) {
                fcIdOpt = Optional.of(shipNodeToFcid.get(shipNodeId));
            }
        }
        return fcIdOpt;
    }
}
