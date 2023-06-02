package com.walmart.thor.endgame.orderingestion.dto.allocation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    private int quantity;
    private ItemKey itemKey;
}