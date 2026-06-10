package com.chubb.policyoverview.infrastructure.persistence.specification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.chubb.policyoverview.domain.models.LineOfBusiness;
import com.chubb.policyoverview.domain.models.PolicySearchCriteria;
import com.chubb.policyoverview.domain.models.Region;
import com.chubb.policyoverview.domain.models.Status;
import com.chubb.policyoverview.infrastructure.persistence.entity.PolicyEntity;
import com.chubb.policyoverview.infrastructure.persistence.repository.PolicyRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PolicySpecificationIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private PolicyRepository policyRepository;

    @BeforeEach
    void seedPolicies() {
        policyRepository.saveAll(List.of(
                policy("POL-SG-000001", "Wei Tan", LineOfBusiness.PROPERTY, Status.ACTIVE,
                        Region.SINGAPORE, "Jane Lim", LocalDate.of(2025, 1, 15)),
                policy("POL-JP-000002", "Hiroshi Sato", LineOfBusiness.MARINE, Status.PENDING,
                        Region.JAPAN, "Hiroshi Sato", LocalDate.of(2025, 6, 1)),
                policy("POL-AU-000003", "Grace Chan", LineOfBusiness.CASUALTY, Status.ACTIVE,
                        Region.AUSTRALIA, "Daniel Wong", LocalDate.of(2024, 12, 20))));
    }

    @Test
    void findAll_filterByStatusAndRegion_returnsOnlyMatching() {
        PolicySearchCriteria criteria = PolicySearchCriteria.builder()
                .status(Status.ACTIVE)
                .region(Region.SINGAPORE)
                .build();

        List<PolicyEntity> result = policyRepository.findAll(PolicySpecification.fromCriteria(criteria));

        assertEquals(1, result.size());
        assertEquals("POL-SG-000001", result.get(0).getPolicyNumber());
    }

    @Test
    void findAll_freeTextSearchIsCaseInsensitive_matchesUnderwriterAndHolder() {
        PolicySearchCriteria criteria = PolicySearchCriteria.builder().search("SATO").build();

        List<PolicyEntity> result = policyRepository.findAll(PolicySpecification.fromCriteria(criteria));

        assertEquals(1, result.size());
        assertEquals("POL-JP-000002", result.get(0).getPolicyNumber());
    }

    @Test
    void findAll_filterByEffectiveDateRange_returnsPoliciesWithinRange() {
        PolicySearchCriteria criteria = PolicySearchCriteria.builder()
                .effectiveDateFrom(LocalDate.of(2025, 1, 1))
                .effectiveDateTo(LocalDate.of(2025, 12, 31))
                .build();

        List<PolicyEntity> result = policyRepository.findAll(PolicySpecification.fromCriteria(criteria));

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(policy -> policy.getEffectiveDate().getYear() == 2025));
    }

    @Test
    void findAll_emptyCriteriaWithPagination_returnsAllPaged() {
        PolicySearchCriteria criteria = PolicySearchCriteria.builder().build();

        Page<PolicyEntity> page = policyRepository.findAll(
                PolicySpecification.fromCriteria(criteria), PageRequest.of(0, 2));

        assertEquals(3, page.getTotalElements());
        assertEquals(2, page.getContent().size());
    }

    @Test
    void shouldFilterByStatus() {

        PolicySearchCriteria criteria =
                PolicySearchCriteria.builder()
                        .status(Status.ACTIVE)
                        .build();

        Page<PolicyEntity> result =
                policyRepository.findAll(
                        PolicySpecification.fromCriteria(criteria),
                        PageRequest.of(0, 10));

        assertFalse(result.isEmpty());
    }

    private PolicyEntity policy(String policyNumber, String policyholderName, LineOfBusiness lineOfBusiness,
            Status status, Region region, String underwriter, LocalDate effectiveDate) {
        Instant timestamp = Instant.now();
        return new PolicyEntity(policyNumber, policyholderName, lineOfBusiness, status,
                new BigDecimal("1500.00"), "SGD", effectiveDate, effectiveDate.plusYears(1),
                region, underwriter, timestamp, timestamp);
    }
}
