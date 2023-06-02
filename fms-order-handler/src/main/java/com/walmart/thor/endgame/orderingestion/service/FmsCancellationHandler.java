package com.walmart.thor.endgame.orderingestion.service;

import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.walmart.thor.endgame.orderingestion.common.dao.CustomerPurchaseOrderDAO;
import com.walmart.thor.endgame.orderingestion.common.dao.CustomerPurchaseOrderDAO.Status;
import com.walmart.thor.endgame.orderingestion.common.dao.CustomerPurchaseOrderDAO.PickTicketDetails;
import com.walmart.thor.endgame.orderingestion.common.dto.customercancel.FmsCancellationEvent;
import com.walmart.thor.endgame.orderingestion.common.dto.fmsorder.BoxingDetails;
import com.walmart.thor.endgame.orderingestion.common.repository.CustomerPurchaseOrderRepository;
import com.walmart.thor.endgame.orderingestion.config.FCDetailsConfig;
import com.walmart.thor.endgame.orderingestion.common.dto.customercancel.PickticketCancellationEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.walmart.thor.endgame.orderingestion.common.constants.Constants.CUSTOMER_ORDER;

@Service
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class FmsCancellationHandler {

    private CustomerPurchaseOrderRepository customerPurchaseOrderRepository;
    private EndgameKafkaPublisher endgameKafkaPublisher;
    private FCDetailsConfig fcDetailsConfig;
    private MicrometerService micrometerService;
    private ObjectMapper mapper;

    private static final String CUSTOMER_CANCEL_REASON = "CUSTOMER_INITIATED_CANCEL";

    // AlgoForOptimizedResult cannot be run if pt count in the order is above 20
    private static final Integer OPTIMIZED_RESULT_ALGORITHM_PT_CUTOFF = 20;

    public void process(FmsCancellationEvent fmsCancellationEvent) {
        log.info("Processing FMS order cancellation message for PO {}",
                fmsCancellationEvent.getPurchaseOrderNo());
        String shipNode = fmsCancellationEvent.getShipNode().getId();

        if (fcDetailsConfig.getShipNodeToFcidMap().containsKey(shipNode)) {
            String fcId = fcDetailsConfig.getShipNodeToFcidMap().get(shipNode);
            String purchaseOrderNo = fmsCancellationEvent.getPurchaseOrderNo();
            String partitionKey = String.format("%s_%s", fcId, purchaseOrderNo);
            Optional<CustomerPurchaseOrderDAO> fmsOrderDao =
                    customerPurchaseOrderRepository.findById(
                            CUSTOMER_ORDER, new PartitionKey(partitionKey));

            if(fmsOrderDao.isEmpty()) {
                micrometerService.getIgnoredCancellationMetric(fcId).increment();
                log.info("Order is not found in cosmos, ignoring Cancellation request for FMS PurchaseOrderNo {}",
                        fmsCancellationEvent.getPurchaseOrderNo());
                return;
            }

            List<PickTicketDetails> pickTicketDetails =
                    fmsOrderDao.get().getPickTicketDetails();
            if(Status.Rejected.equals(fmsOrderDao.get().getStatus()) || ObjectUtils.isEmpty(pickTicketDetails)) {
                log.info("Order is already rejected, ignoring Cancellation request for FMS PurchaseOrderNo {}",
                        fmsCancellationEvent.getPurchaseOrderNo());
                return;
            }

            List<String> ptsToCancel =
                    getOptimalPtsToCancel(pickTicketDetails, fmsCancellationEvent.getFulfillmentLines(), purchaseOrderNo);
            log.info("Cancelling picktickets for PurchaseOrderNo {}, list -: {}",
                    purchaseOrderNo, ptsToCancel.toString());
            ptsToCancel.forEach(pickticketId -> {
                try {
                    String payload = mapper.writeValueAsString(
                            new PickticketCancellationEvent(purchaseOrderNo, pickticketId, fcId, CUSTOMER_CANCEL_REASON));
                    endgameKafkaPublisher.send(pickticketId, payload, new MessageHeaderAccessor());
                    log.info("successfully published cancellation request for PurchaseOrderNo {}, pickticket: {}",
                            purchaseOrderNo, pickticketId);
                    micrometerService.getPtCustomerCancelRequest(fcId).increment();
                } catch (JsonProcessingException e) {
                    log.error("error in serialization of customer cancellation event, pickticketId: {} ex: {}",
                            pickticketId,e.getMessage());
                }
            });
        }

    }

    /*
     * Details -: https://confluence.walmart.com/display/SCTEGT/Order+Ingestion+handling#OrderIngestionhandling-AlgorithmtofindoptimalsetofPicktickets
     */
    public List<String> getOptimalPtsToCancel(List<PickTicketDetails> pickTickets,
                                              List<FmsCancellationEvent.FulfillmentLines> cancellationLines,
                                              String purchaseOrderNo) {

        Map<String, List<BoxingDetails.LineDetails>> pickTicketMap = pickTickets.stream()
                .collect(Collectors.toMap(PickTicketDetails::getPickTicketId,
                        pickTicket -> pickTicket.getBoxingDetails().getLineDetails(), (a, b) -> b));

        Map<String, Integer> cancellationLineMap = cancellationLines.stream()
                .collect(Collectors.toMap(FmsCancellationEvent.FulfillmentLines::getLineId,
                        line -> line.getQuantity().getMeasurementValue(), (a, b) -> b));

        if(pickTickets.size() < OPTIMIZED_RESULT_ALGORITHM_PT_CUTOFF) {
            log.info("using Optimized Algorithm for PurchaseOrderNo {}", purchaseOrderNo);
            return runAlgoForOptimizedResult(pickTicketMap, cancellationLineMap);
        } else {
            log.info("using Greedy Algorithm for PurchaseOrderNo {}", purchaseOrderNo);
            return runGreedyAlgo(pickTicketMap, cancellationLineMap);
        }
    }

    public List<String> runAlgoForOptimizedResult(
            Map<String, List<BoxingDetails.LineDetails>> pickTicketMap,
            Map<String, Integer> cancellationLineMap) {
        int totalSets = (int)Math.pow(2, pickTicketMap.size());
        PtSet selectedSet = PtSet.builder()
                .ptList(new ArrayList<>())
                .build();
        int currSetValue = 0;
        while (currSetValue < totalSets) {
            PtSet currSet = PtSet.builder()
                    .value(currSetValue)
                    .itemCount(0)
                    .ptList(new ArrayList<>())
                    .build();
            Map<String, Integer> cancellationLineMapCopy = new HashMap<>(cancellationLineMap);
            int ptIndex = 0;
            for (Map.Entry<String, List<BoxingDetails.LineDetails>> entry : pickTicketMap.entrySet()) {
                if(cancellationLineMapCopy.isEmpty()) break;
                if ((currSet.getValue() & (1 << ptIndex)) != 0) {
                    String pickticketId = entry.getKey();
                    currSet.getPtList().add(pickticketId);
                    List<BoxingDetails.LineDetails> lineList = entry.getValue();
                    lineList.forEach(lineDetails -> {
                        String lineId = lineDetails.getFulfillmentLineId();
                        int lineQty = lineDetails.getQuantity().getMeasurementValue();
                        currSet.setItemCount(currSet.getItemCount() + lineQty);
                        int qtyAfterCancel = cancellationLineMapCopy.getOrDefault(lineId, 0) - lineQty;
                        if (qtyAfterCancel > 0)
                            cancellationLineMapCopy.put(lineId, qtyAfterCancel);
                        else
                            cancellationLineMapCopy.remove(lineId);
                    });
                }
                ptIndex++;
            }
            if(cancellationLineMapCopy.isEmpty() &&
                    (ObjectUtils.isEmpty(selectedSet.getItemCount())  ||
                            selectedSet.getItemCount() > currSet.getItemCount())) {
                selectedSet = currSet;
            }

            currSetValue++;
        }
        return selectedSet.getPtList();
    }

    public List<String> runGreedyAlgo(
            Map<String, List<BoxingDetails.LineDetails>> pickTicketMap,
            Map<String, Integer> cancellationLineMap) {
        Set<String> selectedSet = new HashSet<>();
        for (Map.Entry<String, List<BoxingDetails.LineDetails>> entry : pickTicketMap.entrySet()) {
            if(cancellationLineMap.isEmpty()) break;
            String pickticketId = entry.getKey();
            List<BoxingDetails.LineDetails> lineList = entry.getValue();
            lineList.forEach(lineDetails -> {
                String lineId = lineDetails.getFulfillmentLineId();
                int lineQty = lineDetails.getQuantity().getMeasurementValue();
                int toBeCancelledQty = cancellationLineMap.getOrDefault(lineId, 0);
                if (toBeCancelledQty > 0) selectedSet.add(pickticketId);
                if (toBeCancelledQty - lineQty > 0)
                    cancellationLineMap.put(lineId, toBeCancelledQty - lineQty);
                else
                    cancellationLineMap.remove(lineId);
            });
        }
        if(cancellationLineMap.isEmpty())
            return new ArrayList<>(selectedSet);
        else
            return new ArrayList<>();
    }

    @Getter
    @Setter
    @Builder
    public static class PtSet{
        Integer value;
        Integer itemCount;
        List<String> ptList;
    }
}
