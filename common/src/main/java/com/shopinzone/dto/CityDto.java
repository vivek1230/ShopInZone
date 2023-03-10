package com.shopinzone.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CityDto {
    String cityId;
    String cityName;
    String description;
    String cityImageUrl;
}
