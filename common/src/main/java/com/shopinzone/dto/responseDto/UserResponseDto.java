package com.shopinzone.dto.responseDto;

import com.shopinzone.enums.UserRoleType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponseDto {
    Long userId;
    UserRoleType userRoleType;
    Long profileId;
}
