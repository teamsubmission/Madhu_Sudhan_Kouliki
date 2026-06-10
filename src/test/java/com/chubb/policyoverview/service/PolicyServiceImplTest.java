package com.chubb.policyoverview.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chubb.policyoverview.api.dto.response.FlagPoliciesResponseDto;
import com.chubb.policyoverview.api.dto.response.PolicyDetailResponseDto;
import com.chubb.policyoverview.api.dto.response.PolicyResponseDto;
import com.chubb.policyoverview.api.dto.response.PolicySummaryResponseDto;
import com.chubb.policyoverview.api.mapper.DomainToResponseDto;
import com.chubb.policyoverview.domain.exception.PolicyNotFoundException;
import com.chubb.policyoverview.domain.models.LineOfBusiness;
import com.chubb.policyoverview.domain.models.Policy;
import com.chubb.policyoverview.domain.models.PolicySearchCriteria;
import com.chubb.policyoverview.domain.models.Region;
import com.chubb.policyoverview.domain.models.Status;
import com.chubb.policyoverview.infrastructure.persistence.entity.PolicyEntity;
import com.chubb.policyoverview.infrastructure.persistence.mapper.EntityToDomain;
import com.chubb.policyoverview.infrastructure.persistence.repository.PolicyRepository;
import com.chubb.policyoverview.infrastructure.persistence.repository.PolicyRepository.PremiumByLineOfBusiness;
import com.chubb.policyoverview.infrastructure.persistence.repository.PolicyRepository.StatusCount;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class PolicyServiceImplTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private EntityToDomain entityToDomain;

    @Mock
    private DomainToResponseDto domainToResponseDto;

    @InjectMocks
    private PolicyServiceImpl policyService;

    @Test
    void getPolicies_whenPoliciesExist_returnsMappedPagePreservingPagination() {
        PolicyEntity entity = mock(PolicyEntity.class);
        Policy domain = sampleDomain(UUID.randomUUID());
        PolicyResponseDto dto = PolicyResponseDto.builder().policyNumber("POL-1").build();
        Pageable pageable = PageRequest.of(0, 20);
        Page<PolicyEntity> entityPage = new PageImpl<>(List.of(entity), pageable, 1);
        when(policyRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(entityPage);
        when(entityToDomain.toDomain(entity)).thenReturn(domain);
        when(domainToResponseDto.toResponse(domain)).thenReturn(dto);

        Page<PolicyResponseDto> result =
                policyService.getPolicies(PolicySearchCriteria.builder().build(), pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(dto, result.getContent().get(0));
        verify(policyRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getPolicyById_whenPolicyExists_returnsDetailDto() {
        UUID id = UUID.randomUUID();
        PolicyEntity entity = mock(PolicyEntity.class);
        Policy domain = sampleDomain(id);
        PolicyDetailResponseDto dto = PolicyDetailResponseDto.builder().id(id).build();
        when(policyRepository.findById(id)).thenReturn(Optional.of(entity));
        when(entityToDomain.toDomain(entity)).thenReturn(domain);
        when(domainToResponseDto.toDetailResponse(domain)).thenReturn(dto);

        PolicyDetailResponseDto result = policyService.getPolicyById(id);

        assertSame(dto, result);
    }

    @Test
    void getPolicyById_whenPolicyMissing_throwsPolicyNotFoundException() {
        UUID id = UUID.randomUUID();
        when(policyRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(PolicyNotFoundException.class, () -> policyService.getPolicyById(id));
    }

    @Test
    void flagPolicies_whenPoliciesFound_flagsAllAndReturnsCount() {
        PolicyEntity first = sampleEntity("POL-1");
        PolicyEntity second = sampleEntity("POL-2");
        List<UUID> ids = List.of(UUID.randomUUID(), UUID.randomUUID());
        when(policyRepository.findAllById(ids)).thenReturn(List.of(first, second));

        FlagPoliciesResponseDto result = policyService.flagPolicies(ids);

        assertEquals(2, result.getFlaggedCount());
        assertEquals("2 policies flagged for review.", result.getMessage());
        assertTrue(first.isFlaggedForReview());
        assertTrue(second.isFlaggedForReview());
        verify(policyRepository).saveAll(List.of(first, second));
    }

    @Test
    void flagPolicies_whenNoPoliciesFound_returnsZeroCount() {
        List<UUID> ids = List.of(UUID.randomUUID());
        when(policyRepository.findAllById(ids)).thenReturn(List.of());

        FlagPoliciesResponseDto result = policyService.flagPolicies(ids);

        assertEquals(0, result.getFlaggedCount());
        assertEquals("0 policies flagged for review.", result.getMessage());
    }

    @Test
    void getPolicySummary_whenInvoked_buildsCompleteSummaryWithDefaults() {
        StatusCount activeCount = mock(StatusCount.class);
        when(activeCount.getStatus()).thenReturn(Status.ACTIVE);
        when(activeCount.getCount()).thenReturn(5L);
        PremiumByLineOfBusiness propertyPremium = mock(PremiumByLineOfBusiness.class);
        when(propertyPremium.getLineOfBusiness()).thenReturn(LineOfBusiness.PROPERTY);
        when(propertyPremium.getTotalPremium()).thenReturn(new BigDecimal("1000.00"));
        when(policyRepository.countGroupedByStatus()).thenReturn(List.of(activeCount));
        when(policyRepository.sumPremiumGroupedByLineOfBusiness()).thenReturn(List.of(propertyPremium));
        when(policyRepository.countByExpiryDateBetween(any(LocalDate.class), any(LocalDate.class))).thenReturn(3L);

        PolicySummaryResponseDto result = policyService.getPolicySummary();

        assertEquals(4, result.getStatusCounts().size());
        assertEquals(5L, result.getStatusCounts().get("Active").longValue());
        assertEquals(0L, result.getStatusCounts().get("Expired").longValue());
        assertEquals(new BigDecimal("1000.00"), result.getPremiumByLineOfBusiness().get("Property"));
        assertEquals(BigDecimal.ZERO, result.getPremiumByLineOfBusiness().get("A&H"));
        assertEquals(3, result.getExpiringSoonCount());
    }

    private Policy sampleDomain(UUID id) {
        return new Policy(id, "POL-1", "Acme", LineOfBusiness.PROPERTY, Status.ACTIVE,
                new BigDecimal("1000.00"), "SGD", LocalDate.now(), LocalDate.now().plusYears(1),
                Region.SINGAPORE, "Jane Lim", false, Instant.now(), Instant.now());
    }

    private PolicyEntity sampleEntity(String policyNumber) {
        return new PolicyEntity(policyNumber, "Acme", LineOfBusiness.PROPERTY, Status.ACTIVE,
                new BigDecimal("1000.00"), "SGD", LocalDate.now(), LocalDate.now().plusYears(1),
                Region.SINGAPORE, "Jane Lim", Instant.now(), Instant.now());
    }
}
