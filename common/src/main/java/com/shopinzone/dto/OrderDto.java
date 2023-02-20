package com.shopinzone.dto;

import com.shopinzone.enums.PricingType;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
public class OrderDto {
    Long orderId;
    Long userId;
    OffsetDateTime orderDate;
    Double totalAmount;
    List<OrderItemDto> orderItemList;
    AddressDto orderAddress;
    PaymentOptionDto orderPaymentOption;

    @Data
    @Builder
    public static class OrderItemDto {
        Long itemId;
        String itemName;
        String description;
        int orderQuantity;
        Double price;
        PricingType pricingType;
        List<String> itemImageUrlList;
    }
}
