package com.shopinzone.dto;

import com.shopinzone.enums.Gender;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileDto {
    String profileId;
    String userId;
    String email;
    String name;
    String mobile;
    Gender gender;
    String pinCode;
    String city;
    String password;
    AddressDto primaryAddress;
}
