package com.shopinzone.dto.requestDto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShopRequestDto {
    String shopName;
    String description;
    String rating;
    Long addressId;
    String imageUrl;
}
