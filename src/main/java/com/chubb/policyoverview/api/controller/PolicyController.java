package com.chubb.policyoverview.api.controller;

import com.chubb.policyoverview.api.dto.request.FlagPoliciesRequestDto;
import com.chubb.policyoverview.api.dto.response.FlagPoliciesResponseDto;
import com.chubb.policyoverview.api.dto.response.PagedResponseDto;
import com.chubb.policyoverview.api.dto.response.PolicyDetailResponseDto;
import com.chubb.policyoverview.api.dto.response.PolicyResponseDto;
import com.chubb.policyoverview.api.dto.response.PolicySummaryResponseDto;
import com.chubb.policyoverview.domain.models.LineOfBusiness;
import com.chubb.policyoverview.domain.models.PolicySearchCriteria;
import com.chubb.policyoverview.domain.models.Region;
import com.chubb.policyoverview.domain.models.Status;
import com.chubb.policyoverview.service.PolicyService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/policies")
public class PolicyController {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final PolicyService policyService;

    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    @GetMapping
    public PagedResponseDto<PolicyResponseDto> getPolicies(
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) LineOfBusiness lineOfBusiness,
            @RequestParam(required = false) Region region,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveDateTo,
            @RequestParam(required = false) String search,
            @PageableDefault(size = DEFAULT_PAGE_SIZE) Pageable pageable) {
        PolicySearchCriteria criteria = PolicySearchCriteria.builder()
                .status(status)
                .lineOfBusiness(lineOfBusiness)
                .region(region)
                .effectiveDateFrom(effectiveDateFrom)
                .effectiveDateTo(effectiveDateTo)
                .search(search)
                .build();
        Page<PolicyResponseDto> policies = policyService.getPolicies(criteria, pageable);
        return PagedResponseDto.from(policies);
    }

    @GetMapping("/{id}")
    public PolicyDetailResponseDto getPolicyById(@PathVariable UUID id) {
        return policyService.getPolicyById(id);
    }

    @PatchMapping("/flag")
    public FlagPoliciesResponseDto flagPolicies(@Valid @RequestBody FlagPoliciesRequestDto request) {
        return policyService.flagPolicies(request.getPolicyIds());
    }

    @GetMapping("/summary")
    public PolicySummaryResponseDto getPolicySummary() {
        return policyService.getPolicySummary();
    }
}
