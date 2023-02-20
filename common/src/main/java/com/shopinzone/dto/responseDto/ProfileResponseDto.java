package com.shopinzone.dto.responseDto;

import com.shopinzone.enums.Gender;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileResponseDto {
    Long profileId;
    String email;
    String name;
    String mobile;
    Gender gender;
    String pinCode;
    String city;
}
