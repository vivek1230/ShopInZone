package com.walmart.thor.endgame.orderingestion.dto.allocation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Allocation {

    private String area;
    private OffsetDateTime lastUpdated;
    private String gtin;
    private String sellerId;
    private String pickTicketId;
    private String fcid;
    private int qty;
    private long id;
    private String purchaseOrderNumber;
}