package com.shopinzone.dto.requestDto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CityRequestDto {
    String cityName;
    String description;
    String cityImageUrl;
}
