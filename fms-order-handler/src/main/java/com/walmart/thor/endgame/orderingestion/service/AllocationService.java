package com.walmart.thor.endgame.orderingestion.service;

import com.azure.core.exception.AzureException;
import com.walmart.thor.endgame.orderingestion.common.dao.Allocation;
import com.walmart.thor.endgame.orderingestion.common.dao.CustomerPurchaseOrderDAO;
import com.walmart.thor.endgame.orderingestion.common.dao.GlobalGtinInventory;
import com.walmart.thor.endgame.orderingestion.common.dto.ItemIdentifier;
import com.walmart.thor.endgame.orderingestion.common.dto.domain.ChangeReason;
import com.walmart.thor.endgame.orderingestion.common.dto.fmsorder.BoxingDetails;
import com.walmart.thor.endgame.orderingestion.common.dto.fmsorder.FmsOrder;
import com.walmart.thor.endgame.orderingestion.common.repository.AllocationRepository;
import com.walmart.thor.endgame.orderingestion.common.repository.GlobalGtinInventoryRepository;
import com.walmart.thor.endgame.orderingestion.dto.FmsOrderEvent;
import com.walmart.thor.endgame.orderingestion.dto.InventoryResult;
import com.walmart.thor.endgame.orderingestion.dto.ItemKey;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = {AzureException.class, SQLException.class})
public class AllocationService {

    private AllocationRepository allocationRepository;

    private GlobalGtinInventoryRepository globalGtinInventoryRepository;

    private PickTicketIDGenerator pickTicketIDGenerator;

    public InventoryResult checkInventoryAndAllocate(FmsOrderEvent fmsOrderEvent, String fcid) {
        FmsOrder fmsOrder = fmsOrderEvent.getEventPayload();
        String purchaseOrderNo = fmsOrder.getPurchaseOrderNo();
        List<CustomerPurchaseOrderDAO.PickTicketDetails> pickTicketDetails = new ArrayList<>();
        InventoryResult inventoryResult =
                checkInventory(purchaseOrderNo, fcid, fmsOrder.getFulfillmentLines());
        if (inventoryResult.getIsAvailable() && !inventoryResult.getAlreadyUpdated()) {
            log.info(
                    "Inventory is available and allocations not made for fcid: {}, purchaseOrderNo : {}",
                    fcid,
                    purchaseOrderNo);
            pickTicketDetails =
                    generatePickticketDetails(fcid, fmsOrder.getBoxingDetails());
            Map<String, ItemIdentifier> lineToItemMap = fmsOrder.getLineToItemMap();

            List<GlobalGtinInventory> updatedGtinInventory =
                    getUpdatedInventory(fcid, purchaseOrderNo, inventoryResult);
            List<Allocation> updatedAllocations =
                    getUpdatedAllocation(fcid, purchaseOrderNo, pickTicketDetails, lineToItemMap);
            allocationRepository.saveAll(updatedAllocations);
            globalGtinInventoryRepository.saveAll(updatedGtinInventory);
            log.info(
                    "successfully updated inventory and allocation for purchaseOrderNo {}", purchaseOrderNo);
        }
        return inventoryResult.toBuilder()
                .pickTicketDetails(pickTicketDetails)
                .build();
    }

    public InventoryResult checkInventory(
            String purchaseOrderNo, String fcid, List<FmsOrder.FulfillmentLines> fulfillmentLines) {
        log.info("Checking inventory for fcid: {}, purchaseOrderNo : {}", fcid, purchaseOrderNo);
        List<Allocation> allocations = allocationRepository.findByOrderId(purchaseOrderNo);

    /* This is idempotency check to find if allocations have already been made.
    This may happen when kafka message is retried. */
        InventoryResult inventoryResult;
        if (allocations.isEmpty()) {
            Boolean isAvailable = true;
            List<GlobalGtinInventory.GlobalGtinInventoryID> requestedGtinInventoryIDs = new ArrayList<>();
            Map<ItemKey, Integer> requestedInventoryQty = new HashMap<>();
            Map<ItemKey, Integer> availableInventoryQty = new HashMap<>();
            Map<ItemKey, ChangeReason> itemStatus = new HashMap<>();

            fulfillmentLines.forEach(
                    line -> {
                        String gtin = line.getItemDetails().getGtin();
                        String sellerId = line.getItemDetails().getSellerId();
                        requestedGtinInventoryIDs.add(
                                new GlobalGtinInventory.GlobalGtinInventoryID().withGtin(gtin).withFcId(fcid).withSellerId(sellerId));
                        requestedInventoryQty.put(
                                getItemKey(fcid, gtin, sellerId), line.getQuantity().getMeasurementValue());
                    });

            List<GlobalGtinInventory> gtinInventory =
                    globalGtinInventoryRepository.findAllById(requestedGtinInventoryIDs);

            for (GlobalGtinInventory inv : gtinInventory) {
                String gtin = inv.getId().getGtin();
                String sellerId = inv.getId().getSellerId();
                int availableQuantity = inv.getSellableCount() - inv.getAllocatedCount();
                log.info("Got fcid={}|sellableCount={}|allocatedQty={}|inventoryAvailable={}|gtin={}|sellerId={}|fmsOrderId={}",
                        fcid,
                        inv.getSellableCount(),
                        inv.getAllocatedCount(),
                        availableQuantity,
                        gtin,
                        sellerId,
                        purchaseOrderNo);
                availableInventoryQty.put(getItemKey(fcid, gtin, sellerId), availableQuantity);
            }

            for (Map.Entry<ItemKey,Integer> entry : requestedInventoryQty.entrySet()) {
                ItemKey itemKey = entry.getKey(); // fcid_gtin_sellerId
                int availableQuantity = availableInventoryQty.getOrDefault(itemKey, 0);
                int requestedQuantity = entry.getValue();
                if (availableQuantity < requestedQuantity) {
                    log.info("Inventory not available for GTIN:{} | purchaseOrderNo:{} | fcid:{}",itemKey, purchaseOrderNo, fcid);
                    isAvailable = false;
                    //ItemStatus will be used if order is rejected
                    itemStatus.put(itemKey,
                            (availableQuantity==0)?ChangeReason.ZeroInventory:ChangeReason.NotEnoughInventory);
                } else {
                    log.info("Inventory available for GTIN:{} | purchaseOrderNo:{} | fcid:{}",itemKey, purchaseOrderNo, fcid);
                    // If order is rejected, changeReason for this item will be killOrFill
                    // because this item is available
                    itemStatus.put(itemKey, ChangeReason.KillOrFill);
                }
            }
            inventoryResult =
                    InventoryResult.builder()
                            .isAvailable(isAvailable)
                            .alreadyUpdated(false)
                            .gtinInventory(gtinInventory)
                            .requestedInventoryQty(requestedInventoryQty)
                            .itemStatus(itemStatus)
                            .build();
        } else {
            log.info("Order is already successfully allocated purchaseOrderNo - {}", purchaseOrderNo);
            inventoryResult = InventoryResult.builder().isAvailable(true).alreadyUpdated(true).build();
        }
        return inventoryResult;
    }

    public List<CustomerPurchaseOrderDAO.PickTicketDetails> generatePickticketDetails(
            String fcid, List<BoxingDetails> boxingDetails) {
        log.info("Generating pickticket details for fcid: {}", fcid);
        List<CustomerPurchaseOrderDAO.PickTicketDetails> pickTicketDetailsList = new ArrayList<>();
        List<String> pickTicketIds =
                pickTicketIDGenerator.getNextPickTicketIDs(fcid, boxingDetails.size());
        HashMap<Integer, BoxingDetails> boxingDetailsMap =
                boxingDetails.stream()
                        .collect(
                                HashMap<Integer, BoxingDetails>::new,
                                (map, streamValue) -> map.put(map.size(), streamValue),
                                (map, map2) -> {});
        boxingDetailsMap.forEach(
                (index, box) -> {
                    pickTicketDetailsList.add(
                            CustomerPurchaseOrderDAO.PickTicketDetails.builder()
                                    .boxingDetails(box)
                                    .pickTicketId(pickTicketIds.size() > 0 ? pickTicketIds.get(index) : "")
                                    .build());
                });
        return pickTicketDetailsList;
    }

    public List<GlobalGtinInventory> getUpdatedInventory(String fcid,
                                                         String purchaseOrderNo,
                                                         InventoryResult inventoryResult) {
        log.info("Constructing inventory for fcid: {}, purchaseOrderNo: {} ", fcid, purchaseOrderNo);
        List<GlobalGtinInventory> gtinInventory = inventoryResult.getGtinInventory();
        Map<ItemKey, Integer> requestedInventoryQty = inventoryResult.getRequestedInventoryQty();

        List<GlobalGtinInventory> updatedGtinInventory = new ArrayList<>();

        for (GlobalGtinInventory inv : gtinInventory) {
            String gtin = inv.getId().getGtin();
            String sellerId = inv.getId().getSellerId();
            ItemKey itemKey = getItemKey(fcid, gtin, sellerId);
            Integer requestedQuantity = requestedInventoryQty.get(itemKey);
            int totalAllocatedCount = requestedQuantity + inv.getAllocatedCount();
            updatedGtinInventory.add(
                    new GlobalGtinInventory()
                            .withId(inv.getId())
                            .withAllocatedCount(totalAllocatedCount)
                            .withSellableCount(inv.getSellableCount()));
        }
        return updatedGtinInventory;
    }

    public List<Allocation> getUpdatedAllocation(
            String fcid,
            String purchaseOrderNo,
            List<CustomerPurchaseOrderDAO.PickTicketDetails> pickTicketDetails,
            Map<String, ItemIdentifier> lineToItemMap) {
        log.info("Constructing Allocations for fcid: {}, purchaseOrderNo: {} ", fcid, purchaseOrderNo);

        List<Allocation> updatedAllocations = new ArrayList<>();
        for (CustomerPurchaseOrderDAO.PickTicketDetails ptDetails: pickTicketDetails){
            List<BoxingDetails.LineDetails> lineDetails = ptDetails.getBoxingDetails().getLineDetails();
            for (BoxingDetails.LineDetails line: lineDetails){
                String gtin = lineToItemMap.get(line.getFulfillmentLineId()).getGtin();
                String sellerId = lineToItemMap.get(line.getFulfillmentLineId()).getSellerId();
                updatedAllocations.add(
                        new Allocation()
                                .withId(
                                        new Allocation.AllocationID()
                                                .withFcId(fcid)
                                                .withOrderId(purchaseOrderNo)
                                                .withSellerId(sellerId)
                                                .withGtin(gtin)
                                                .withPickTicketId(ptDetails.getPickTicketId()))
                                .withQuantity(line.getQuantity().getMeasurementValue()));
            }
        }
        return updatedAllocations;
    }

    private ItemKey getItemKey(String fcid, String gtin, String sellerId) {
        return ItemKey.builder().fcid(fcid).sellerId(sellerId).gtin(gtin).build();
    }

}
