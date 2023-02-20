package com.shopinzone.dto.responseDto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CategoryResponseDto {
    Long categoryId;
    String categoryName;
    String description;
    List<String> categoryImageUrlList;
}
