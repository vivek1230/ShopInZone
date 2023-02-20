package com.shopinzone.dto;

import com.shopinzone.enums.UserRoleType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    String userId;
    UserRoleType userRoleType;
    String profileId;
}
