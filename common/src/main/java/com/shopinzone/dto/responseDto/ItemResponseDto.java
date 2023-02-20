package com.shopinzone.dto.responseDto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ItemResponseDto {
    String itemId;
    String itemName;
    String description;
    List<String> itemImageUrlList;
}
