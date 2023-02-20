package com.shopinzone.dto.requestDto;

import com.shopinzone.enums.PaymentOptionType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentOptionRequestDto {
    String paymentOptionId;
    String userId;
    String paymentOptionName;
    String description;
    PaymentOptionType paymentOptionType;
}
