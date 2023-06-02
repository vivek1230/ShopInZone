package com.walmart.thor.endgame.orderingestion.dto.allocation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Error {
    private String purchaseOrderNumber;
    private List<ItemStatus> itemStatusList;
    private String errorCode;
    private String errorDesc;
}