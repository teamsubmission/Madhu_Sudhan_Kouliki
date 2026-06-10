package com.chubb.policyoverview.domain.models;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record Policy(
        UUID id,
        String policyNumber,
        String policyholderName,
        LineOfBusiness lineOfBusiness,
        Status status,
        BigDecimal premiumAmount,
        String currency,
        LocalDate effectiveDate,
        LocalDate expiryDate,
        Region region,
        String underwriter,
        boolean flaggedForReview,
        Instant createdAt,
        Instant updatedAt) {
}
