package com.shopinzone.dto.requestDto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ItemRequestDto {
    String itemId;
    String itemName;
    String description;
    List<String> itemImageUrlList;
}
