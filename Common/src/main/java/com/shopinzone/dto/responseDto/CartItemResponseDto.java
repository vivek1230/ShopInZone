package com.shopinzone.dto.responseDto;

import com.shopinzone.enums.PricingType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CartItemResponseDto {
    String cartId;
    String userId;
    String shopId;
    String itemId;
    String itemName;
    String description;
    int orderQuantity;
    Double price;
    PricingType pricingType;
    List<String> itemImageUrlList;
}
