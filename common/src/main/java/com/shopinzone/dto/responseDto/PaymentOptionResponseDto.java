package com.shopinzone.dto.responseDto;

import com.shopinzone.enums.PaymentOptionType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentOptionResponseDto {
    String paymentOptionId;
    String userId;
    String paymentOptionName;
    String description;
    PaymentOptionType paymentOptionType;
}
