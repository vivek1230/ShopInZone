package com.shopinzone.dto.requestDto;

import com.shopinzone.enums.PricingType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CartItemRequestDto {
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
