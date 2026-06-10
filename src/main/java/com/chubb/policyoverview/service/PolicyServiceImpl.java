package com.chubb.policyoverview.service;

import com.chubb.policyoverview.api.dto.response.FlagPoliciesResponseDto;
import com.chubb.policyoverview.api.dto.response.PolicyDetailResponseDto;
import com.chubb.policyoverview.api.dto.response.PolicyResponseDto;
import com.chubb.policyoverview.api.dto.response.PolicySummaryResponseDto;
import com.chubb.policyoverview.api.mapper.DomainToResponseDto;
import com.chubb.policyoverview.domain.exception.PolicyNotFoundException;
import com.chubb.policyoverview.domain.models.LineOfBusiness;
import com.chubb.policyoverview.domain.models.PolicySearchCriteria;
import com.chubb.policyoverview.domain.models.Status;
import com.chubb.policyoverview.infrastructure.persistence.entity.PolicyEntity;
import com.chubb.policyoverview.infrastructure.persistence.mapper.EntityToDomain;
import com.chubb.policyoverview.infrastructure.persistence.repository.PolicyRepository;
import com.chubb.policyoverview.infrastructure.persistence.specification.PolicySpecification;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PolicyServiceImpl implements PolicyService {

    private static final int EXPIRY_WINDOW_DAYS = 30;
    private static final String FLAG_MESSAGE_TEMPLATE = "%d policies flagged for review.";

    private final PolicyRepository policyRepository;
    private final EntityToDomain entityToDomain;
    private final DomainToResponseDto domainToResponseDto;

    public PolicyServiceImpl(
            PolicyRepository policyRepository,
            EntityToDomain entityToDomain,
            DomainToResponseDto domainToResponseDto) {
        this.policyRepository = policyRepository;
        this.entityToDomain = entityToDomain;
        this.domainToResponseDto = domainToResponseDto;
    }

    @Override
    public Page<PolicyResponseDto> getPolicies(PolicySearchCriteria criteria, Pageable pageable) {
        Specification<PolicyEntity> specification = PolicySpecification.fromCriteria(criteria);
        return policyRepository.findAll(specification, pageable)
                .map(entity -> domainToResponseDto.toResponse(entityToDomain.toDomain(entity)));
    }

    @Override
    public PolicyDetailResponseDto getPolicyById(UUID id) {
        PolicyEntity entity = policyRepository.findById(id)
                .orElseThrow(() -> new PolicyNotFoundException(id));
        return domainToResponseDto.toDetailResponse(entityToDomain.toDomain(entity));
    }

    @Override
    @Transactional
    public FlagPoliciesResponseDto flagPolicies(List<UUID> policyIds) {
        List<PolicyEntity> policies = policyRepository.findAllById(policyIds);
        policies.forEach(PolicyEntity::flagForReview);
        policyRepository.saveAll(policies);
        int flaggedCount = policies.size();
        return FlagPoliciesResponseDto.builder()
                .flaggedCount(flaggedCount)
                .message(String.format(FLAG_MESSAGE_TEMPLATE, flaggedCount))
                .build();
    }

    @Override
    public PolicySummaryResponseDto getPolicySummary() {
        LocalDate today = LocalDate.now();
        long expiringSoonCount =
                policyRepository.countByExpiryDateBetween(today, today.plusDays(EXPIRY_WINDOW_DAYS));
        return PolicySummaryResponseDto.builder()
                .statusCounts(statusCounts())
                .premiumByLineOfBusiness(premiumTotalsByLineOfBusiness())
                .expiringSoonCount((int) expiringSoonCount)
                .build();
    }

    private Map<String, Long> statusCounts() {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (Status status : Status.values()) {
            counts.put(status.getDisplayName(), 0L);
        }
        policyRepository.countGroupedByStatus()
                .forEach(row -> counts.put(row.getStatus().getDisplayName(), row.getCount()));
        return counts;
    }

    private Map<String, BigDecimal> premiumTotalsByLineOfBusiness() {
        Map<String, BigDecimal> totals = new LinkedHashMap<>();
        for (LineOfBusiness lineOfBusiness : LineOfBusiness.values()) {
            totals.put(lineOfBusiness.getDisplayName(), BigDecimal.ZERO);
        }
        policyRepository.sumPremiumGroupedByLineOfBusiness()
                .forEach(row -> totals.put(row.getLineOfBusiness().getDisplayName(), row.getTotalPremium()));
        return totals;
    }
}
