package com.chubb.policyoverview.api.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PolicyDetailResponseDto {

    private final UUID id;
    private final String policyNumber;
    private final String policyholderName;
    private final String lineOfBusiness;
    private final String status;
    private final BigDecimal premiumAmount;
    private final String currency;
    private final LocalDate effectiveDate;
    private final LocalDate expiryDate;
    private final String region;
    private final String underwriter;
    private final boolean flaggedForReview;
    private final Instant createdAt;
    private final Instant updatedAt;
}
