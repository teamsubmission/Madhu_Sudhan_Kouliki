package com.chubb.policyoverview.infrastructure.persistence.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.chubb.policyoverview.domain.models.LineOfBusiness;
import com.chubb.policyoverview.domain.models.Policy;
import com.chubb.policyoverview.domain.models.Region;
import com.chubb.policyoverview.domain.models.Status;
import com.chubb.policyoverview.infrastructure.persistence.entity.PolicyEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class EntityToDomainTest {

    private final EntityToDomain mapper = new EntityToDomain();

    @Test
    void toDomain_whenGivenEntity_mapsAllFieldsPreservingEnums() {
        UUID id = UUID.randomUUID();
        Instant created = Instant.parse("2025-01-01T00:00:00Z");
        Instant updated = Instant.parse("2025-06-01T00:00:00Z");
        PolicyEntity entity = new PolicyEntity(
                "POL-9", "Sato Holdings", LineOfBusiness.MARINE, Status.PENDING,
                new BigDecimal("8900.50"), "JPY",
                LocalDate.of(2025, 3, 15), LocalDate.of(2026, 3, 14),
                Region.JAPAN, "Hiroshi Sato", created, updated);
        entity.flagForReview();
        ReflectionTestUtils.setField(entity, "id", id);

        Policy domain = mapper.toDomain(entity);

        assertEquals(id, domain.id());
        assertEquals("POL-9", domain.policyNumber());
        assertEquals("Sato Holdings", domain.policyholderName());
        assertEquals(LineOfBusiness.MARINE, domain.lineOfBusiness());
        assertEquals(Status.PENDING, domain.status());
        assertEquals(new BigDecimal("8900.50"), domain.premiumAmount());
        assertEquals("JPY", domain.currency());
        assertEquals(Region.JAPAN, domain.region());
        assertEquals("Hiroshi Sato", domain.underwriter());
        assertTrue(domain.flaggedForReview());
        assertEquals(created, domain.createdAt());
        assertEquals(updated, domain.updatedAt());
    }
}
