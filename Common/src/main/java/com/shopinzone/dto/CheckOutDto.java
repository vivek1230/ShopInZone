package com.shopinzone.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CheckOutDto {
    String checkOutId;
    String userId;
    String shopId;
    String cartId;
    List<AddressDto> addressList;
    List<PaymentOptionDto> paymentOptionList;
}
