package com.walmart.thor.endgame.orderingestion.dto.allocation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemStatus {
    private int availableInventoryQty;
    private int requestedInventoryQty;
    private ItemKey itemKey;
}