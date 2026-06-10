package com.chubb.policyoverview.infrastructure.persistence.mapper;

import com.chubb.policyoverview.domain.models.Policy;
import com.chubb.policyoverview.infrastructure.persistence.entity.PolicyEntity;
import org.springframework.stereotype.Component;

@Component
public class EntityToDomain {

    public Policy toDomain(PolicyEntity entity) {
        return new Policy(
                entity.getId(),
                entity.getPolicyNumber(),
                entity.getPolicyholderName(),
                entity.getLineOfBusiness(),
                entity.getStatus(),
                entity.getPremiumAmount(),
                entity.getCurrency(),
                entity.getEffectiveDate(),
                entity.getExpiryDate(),
                entity.getRegion(),
                entity.getUnderwriter(),
                entity.isFlaggedForReview(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
