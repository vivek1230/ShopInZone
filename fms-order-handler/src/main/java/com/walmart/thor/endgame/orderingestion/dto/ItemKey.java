package com.walmart.thor.endgame.orderingestion.dto;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
@Data
@Builder
public class ItemKey {

    String fcid;
    String gtin;
    String sellerId;
}
