package com.shopinzone.dto.requestDto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserRequestDto {
    String email;
    String mobile;
    String password;
}
