package com.shopinzone.dto;

import com.shopinzone.enums.AddressType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressDto {
    String addressId;
    String name;
    String mobile;
    AddressType addressType;
    String city;
    String pinCode;
    String landMark;
    String state;
    String addressText;
}
