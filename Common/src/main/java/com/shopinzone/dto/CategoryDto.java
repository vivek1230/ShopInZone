package com.shopinzone.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CategoryDto {
    Long categoryId;
    String categoryName;
    String description;
    List<String> categoryImageUrlList;
}
