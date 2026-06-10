package com.chubb.policyoverview.infrastructure.persistence.specification;

import com.chubb.policyoverview.domain.models.LineOfBusiness;
import com.chubb.policyoverview.domain.models.PolicySearchCriteria;
import com.chubb.policyoverview.domain.models.Region;
import com.chubb.policyoverview.domain.models.Status;
import com.chubb.policyoverview.infrastructure.persistence.entity.PolicyEntity;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;

public final class PolicySpecification {

    private static final String FIELD_STATUS = "status";
    private static final String FIELD_LINE_OF_BUSINESS = "lineOfBusiness";
    private static final String FIELD_REGION = "region";
    private static final String FIELD_EFFECTIVE_DATE = "effectiveDate";
    private static final String FIELD_POLICY_NUMBER = "policyNumber";
    private static final String FIELD_POLICYHOLDER_NAME = "policyholderName";
    private static final String FIELD_UNDERWRITER = "underwriter";
    private static final String WILDCARD = "%";

    private PolicySpecification() {
    }

    public static Specification<PolicyEntity> fromCriteria(PolicySearchCriteria criteria) {
        return Specification.where(hasStatus(criteria.getStatus()))
                .and(hasLineOfBusiness(criteria.getLineOfBusiness()))
                .and(hasRegion(criteria.getRegion()))
                .and(effectiveDateFrom(criteria.getEffectiveDateFrom()))
                .and(effectiveDateTo(criteria.getEffectiveDateTo()))
                .and(matchesSearch(criteria.getSearch()));
    }

    public static Specification<PolicyEntity> hasStatus(Optional<Status> status) {
        return status.map(PolicySpecification::statusEquals).orElseGet(PolicySpecification::noFilter);
    }

    public static Specification<PolicyEntity> hasLineOfBusiness(Optional<LineOfBusiness> lineOfBusiness) {
        return lineOfBusiness.map(PolicySpecification::lineOfBusinessEquals).orElseGet(PolicySpecification::noFilter);
    }

    public static Specification<PolicyEntity> hasRegion(Optional<Region> region) {
        return region.map(PolicySpecification::regionEquals).orElseGet(PolicySpecification::noFilter);
    }

    public static Specification<PolicyEntity> effectiveDateFrom(Optional<LocalDate> from) {
        return from.map(PolicySpecification::effectiveOnOrAfter).orElseGet(PolicySpecification::noFilter);
    }

    public static Specification<PolicyEntity> effectiveDateTo(Optional<LocalDate> to) {
        return to.map(PolicySpecification::effectiveOnOrBefore).orElseGet(PolicySpecification::noFilter);
    }

    public static Specification<PolicyEntity> matchesSearch(Optional<String> search) {
        return search.filter(value -> !value.isBlank())
                .map(PolicySpecification::textMatches)
                .orElseGet(PolicySpecification::noFilter);
    }

    private static Specification<PolicyEntity> statusEquals(Status status) {
        return (root, query, cb) -> cb.equal(root.get(FIELD_STATUS), status);
    }

    private static Specification<PolicyEntity> lineOfBusinessEquals(LineOfBusiness lineOfBusiness) {
        return (root, query, cb) -> cb.equal(root.get(FIELD_LINE_OF_BUSINESS), lineOfBusiness);
    }

    private static Specification<PolicyEntity> regionEquals(Region region) {
        return (root, query, cb) -> cb.equal(root.get(FIELD_REGION), region);
    }

    private static Specification<PolicyEntity> effectiveOnOrAfter(LocalDate from) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.<LocalDate>get(FIELD_EFFECTIVE_DATE), from);
    }

    private static Specification<PolicyEntity> effectiveOnOrBefore(LocalDate to) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.<LocalDate>get(FIELD_EFFECTIVE_DATE), to);
    }

    private static Specification<PolicyEntity> textMatches(String search) {
        return (root, query, cb) -> {
            String pattern = WILDCARD + search.toLowerCase(Locale.ROOT) + WILDCARD;
            return cb.or(
                    cb.like(cb.lower(root.<String>get(FIELD_POLICY_NUMBER)), pattern),
                    cb.like(cb.lower(root.<String>get(FIELD_POLICYHOLDER_NAME)), pattern),
                    cb.like(cb.lower(root.<String>get(FIELD_UNDERWRITER)), pattern));
        };
    }

    private static Specification<PolicyEntity> noFilter() {
        return (root, query, cb) -> cb.conjunction();
    }
}
