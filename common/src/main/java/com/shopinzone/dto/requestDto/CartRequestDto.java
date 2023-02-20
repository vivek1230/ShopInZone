package com.shopinzone.dto.requestDto;

import com.shopinzone.enums.PricingType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CartRequestDto {
    List<ItemRequestDto> itemRequestList;

    @Data
    @Builder
    public static class ItemRequestDto {
        String itemId;
        int orderQuantity;
        Double price;
        PricingType pricingType;
    }
}
