package com.chubb.policyoverview.service;

import com.chubb.policyoverview.api.dto.response.FlagPoliciesResponseDto;
import com.chubb.policyoverview.api.dto.response.PolicyDetailResponseDto;
import com.chubb.policyoverview.api.dto.response.PolicyResponseDto;
import com.chubb.policyoverview.api.dto.response.PolicySummaryResponseDto;
import com.chubb.policyoverview.domain.models.PolicySearchCriteria;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PolicyService {

    Page<PolicyResponseDto> getPolicies(PolicySearchCriteria criteria, Pageable pageable);

    PolicyDetailResponseDto getPolicyById(UUID id);

    FlagPoliciesResponseDto flagPolicies(List<UUID> policyIds);

    PolicySummaryResponseDto getPolicySummary();
}
