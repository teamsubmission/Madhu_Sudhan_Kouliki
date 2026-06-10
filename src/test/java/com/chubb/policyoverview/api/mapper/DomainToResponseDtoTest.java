package com.chubb.policyoverview.api.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.chubb.policyoverview.api.dto.response.PolicyDetailResponseDto;
import com.chubb.policyoverview.api.dto.response.PolicyResponseDto;
import com.chubb.policyoverview.domain.models.LineOfBusiness;
import com.chubb.policyoverview.domain.models.Policy;
import com.chubb.policyoverview.domain.models.Region;
import com.chubb.policyoverview.domain.models.Status;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DomainToResponseDtoTest {

    private final DomainToResponseDto mapper = new DomainToResponseDto();

    @Test
    void toResponse_whenGivenDomainPolicy_mapsEnumsToDisplayNames() {
        Policy policy = sampledomainPolicy();

        PolicyResponseDto dto = mapper.toResponse(policy);

        assertEquals(policy.id(), dto.getId());
        assertEquals("POL-1", dto.getPolicyNumber());
        assertEquals("Acme", dto.getPolicyholderName());
        assertEquals("A&H", dto.getLineOfBusiness());
        assertEquals("Active", dto.getStatus());
        assertEquals("Hong Kong", dto.getRegion());
        assertEquals(new BigDecimal("1234.56"), dto.getPremiumAmount());
        assertEquals("HKD", dto.getCurrency());
        assertTrue(dto.isFlaggedForReview());
    }

    @Test
    void toDetailResponse_whenGivenDomainPolicy_mapsAllFields() {
        Policy policy = sampledomainPolicy();

        PolicyDetailResponseDto dto = mapper.toDetailResponse(policy);

        assertEquals(policy.id(), dto.getId());
        assertEquals("A&H", dto.getLineOfBusiness());
        assertEquals("Active", dto.getStatus());
        assertEquals("Hong Kong", dto.getRegion());
        assertEquals(policy.effectiveDate(), dto.getEffectiveDate());
        assertEquals(policy.expiryDate(), dto.getExpiryDate());
        assertEquals(policy.createdAt(), dto.getCreatedAt());
        assertEquals(policy.updatedAt(), dto.getUpdatedAt());
    }

    private Policy sampledomainPolicy() {
        return new Policy(
                UUID.randomUUID(), "POL-1", "Acme",
                LineOfBusiness.ACCIDENT_AND_HEALTH, Status.ACTIVE,
                new BigDecimal("1234.56"), "HKD",
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31),
                Region.HONG_KONG, "Jane Lim", true,
                Instant.parse("2025-01-01T00:00:00Z"), Instant.parse("2025-06-01T00:00:00Z"));
    }
}
