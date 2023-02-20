package com.shopinzone.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ShopDto {
    Long shopId;
    String shopName;
    String description;
    String city;
    String rating;
    Long addressId;
    List<String> imageUrlList;
}
