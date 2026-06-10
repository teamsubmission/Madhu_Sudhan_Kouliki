package com.chubb.policyoverview.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FlagPoliciesResponseDto {

    private final int flaggedCount;
    private final String message;
}
