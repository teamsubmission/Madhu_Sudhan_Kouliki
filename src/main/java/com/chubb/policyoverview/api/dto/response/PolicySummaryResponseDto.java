package com.chubb.policyoverview.api.dto.response;

import java.math.BigDecimal;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PolicySummaryResponseDto {

    private final Map<String, Long> statusCounts;
    private final Map<String, BigDecimal> premiumByLineOfBusiness;
    private final int expiringSoonCount;
}
