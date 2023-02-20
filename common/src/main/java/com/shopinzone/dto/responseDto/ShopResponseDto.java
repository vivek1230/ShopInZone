package com.shopinzone.dto.responseDto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShopResponseDto {
    Long shopId;
    String shopName;
    String description;
    Long cityId;
    String rating;
    Long addressId;
    String imageUrl;
}
