package com.shopinzone.dto.requestDto;

import com.shopinzone.dto.AddressDto;
import com.shopinzone.dto.PaymentOptionDto;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CheckOutRequestDto {
    String checkOutId;
    String userId;
    String shopId;
    String cartId;
    List<AddressDto> addressList;
    List<PaymentOptionDto> paymentOptionList;
}
