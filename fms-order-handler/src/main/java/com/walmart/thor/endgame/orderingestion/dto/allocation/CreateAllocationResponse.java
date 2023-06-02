package com.walmart.thor.endgame.orderingestion.dto.allocation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAllocationResponse {
    private Success success;
    private Error error;
}