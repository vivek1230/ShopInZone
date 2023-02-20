package com.shopinzone.dto.requestDto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CategoryRequestDto {
    String categoryName;
    String description;
    List<String> categoryImageUrlList;
}
