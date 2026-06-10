package com.chubb.policyoverview.api.mapper;

import com.chubb.policyoverview.api.dto.response.PolicyDetailResponseDto;
import com.chubb.policyoverview.api.dto.response.PolicyResponseDto;
import com.chubb.policyoverview.domain.models.Policy;
import org.springframework.stereotype.Component;

@Component
public class DomainToResponseDto {

    public PolicyResponseDto toResponse(Policy policy) {
        return PolicyResponseDto.builder()
                .id(policy.id())
                .policyNumber(policy.policyNumber())
                .policyholderName(policy.policyholderName())
                .lineOfBusiness(policy.lineOfBusiness().getDisplayName())
                .status(policy.status().getDisplayName())
                .premiumAmount(policy.premiumAmount())
                .currency(policy.currency())
                .effectiveDate(policy.effectiveDate())
                .expiryDate(policy.expiryDate())
                .region(policy.region().getDisplayName())
                .underwriter(policy.underwriter())
                .flaggedForReview(policy.flaggedForReview())
                .createdAt(policy.createdAt())
                .updatedAt(policy.updatedAt())
                .build();
    }

    public PolicyDetailResponseDto toDetailResponse(Policy policy) {
        return PolicyDetailResponseDto.builder()
                .id(policy.id())
                .policyNumber(policy.policyNumber())
                .policyholderName(policy.policyholderName())
                .lineOfBusiness(policy.lineOfBusiness().getDisplayName())
                .status(policy.status().getDisplayName())
                .premiumAmount(policy.premiumAmount())
                .currency(policy.currency())
                .effectiveDate(policy.effectiveDate())
                .expiryDate(policy.expiryDate())
                .region(policy.region().getDisplayName())
                .underwriter(policy.underwriter())
                .flaggedForReview(policy.flaggedForReview())
                .createdAt(policy.createdAt())
                .updatedAt(policy.updatedAt())
                .build();
    }
}
