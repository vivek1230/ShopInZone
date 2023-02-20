package com.shopinzone.dto.requestDto;

import com.shopinzone.enums.Gender;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileRequestDto {
    String name;
    String mobile;
    Gender gender;
    String pinCode;
    String city;
}
