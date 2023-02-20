package com.shopinzone.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ItemDto {
    Long itemId;
    String itemName;
    String description;
    List<String> itemImageUrlList;
}
