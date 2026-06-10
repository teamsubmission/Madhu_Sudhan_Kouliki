package com.chubb.policyoverview.infrastructure.persistence.specification;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chubb.policyoverview.domain.models.LineOfBusiness;
import com.chubb.policyoverview.domain.models.PolicySearchCriteria;
import com.chubb.policyoverview.domain.models.Region;
import com.chubb.policyoverview.domain.models.Status;
import com.chubb.policyoverview.infrastructure.persistence.entity.PolicyEntity;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PolicySpecificationTest {

    @Mock
    private Root<PolicyEntity> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder cb;

    @Mock
    private Predicate predicate;

    @Mock
    private Path<String> stringPath;

    @Mock
    private Path<LocalDate> datePath;

    @Mock
    private Expression<String> loweredExpression;

    @BeforeEach
    void setUp() {
        doReturn(stringPath).when(root).get(anyString());
        doReturn(datePath).when(root).get("effectiveDate");
        when(cb.conjunction()).thenReturn(predicate);
    }

    @Test
    void hasStatus_whenPresent_buildsEqualityPredicate() {
        when(cb.equal(any(), eq(Status.ACTIVE))).thenReturn(predicate);

        Predicate result = PolicySpecification.hasStatus(Optional.of(Status.ACTIVE))
                .toPredicate(root, query, cb);

        assertSame(predicate, result);
        verify(cb).equal(stringPath, Status.ACTIVE);
    }

    @Test
    void hasStatus_whenEmpty_buildsConjunctionNoFilter() {
        Predicate result = PolicySpecification.hasStatus(Optional.empty())
                .toPredicate(root, query, cb);

        assertSame(predicate, result);
        verify(cb).conjunction();
    }

    @Test
    void hasLineOfBusiness_whenPresent_buildsEqualityPredicate() {
        when(cb.equal(any(), eq(LineOfBusiness.PROPERTY))).thenReturn(predicate);

        Predicate result = PolicySpecification.hasLineOfBusiness(Optional.of(LineOfBusiness.PROPERTY))
                .toPredicate(root, query, cb);

        assertSame(predicate, result);
        verify(cb).equal(stringPath, LineOfBusiness.PROPERTY);
    }

    @Test
    void hasRegion_whenPresent_buildsEqualityPredicate() {
        when(cb.equal(any(), eq(Region.SINGAPORE))).thenReturn(predicate);

        Predicate result = PolicySpecification.hasRegion(Optional.of(Region.SINGAPORE))
                .toPredicate(root, query, cb);

        assertSame(predicate, result);
        verify(cb).equal(stringPath, Region.SINGAPORE);
    }

    @Test
    void effectiveDateFrom_whenPresent_buildsGreaterThanOrEqualPredicate() {
        LocalDate from = LocalDate.of(2025, 1, 1);
        when(cb.greaterThanOrEqualTo(datePath, from)).thenReturn(predicate);

        Predicate result = PolicySpecification.effectiveDateFrom(Optional.of(from))
                .toPredicate(root, query, cb);

        assertSame(predicate, result);
        verify(cb).greaterThanOrEqualTo(datePath, from);
    }

    @Test
    void effectiveDateTo_whenPresent_buildsLessThanOrEqualPredicate() {
        LocalDate to = LocalDate.of(2025, 12, 31);
        when(cb.lessThanOrEqualTo(datePath, to)).thenReturn(predicate);

        Predicate result = PolicySpecification.effectiveDateTo(Optional.of(to))
                .toPredicate(root, query, cb);

        assertSame(predicate, result);
        verify(cb).lessThanOrEqualTo(datePath, to);
    }

    @Test
    void matchesSearch_whenPresent_buildsCaseInsensitiveLikeAcrossThreeFields() {
        when(cb.lower(any())).thenReturn(loweredExpression);
        when(cb.like(any(Expression.class), anyString())).thenReturn(predicate);
        when(cb.or(any(Predicate.class), any(Predicate.class), any(Predicate.class))).thenReturn(predicate);

        Predicate result = PolicySpecification.matchesSearch(Optional.of("Acme"))
                .toPredicate(root, query, cb);

        assertSame(predicate, result);
        verify(cb, times(3)).lower(stringPath);
        verify(cb, times(3)).like(loweredExpression, "%acme%");
        verify(cb).or(predicate, predicate, predicate);
    }

    @Test
    void matchesSearch_whenBlank_buildsConjunctionNoFilter() {
        Predicate result = PolicySpecification.matchesSearch(Optional.of("   "))
                .toPredicate(root, query, cb);

        assertSame(predicate, result);
        verify(cb).conjunction();
        verify(cb, never()).like(any(Expression.class), anyString());
    }

    @Test
    void fromCriteria_whenAllFiltersPresent_composesNonNullPredicate() {
        when(cb.equal(any(), any())).thenReturn(predicate);
        when(cb.greaterThanOrEqualTo(any(Expression.class), any(LocalDate.class))).thenReturn(predicate);
        when(cb.lessThanOrEqualTo(any(Expression.class), any(LocalDate.class))).thenReturn(predicate);
        when(cb.lower(any())).thenReturn(loweredExpression);
        when(cb.like(any(Expression.class), anyString())).thenReturn(predicate);
        when(cb.or(any(Predicate.class), any(Predicate.class), any(Predicate.class))).thenReturn(predicate);
        when(cb.and(any(Predicate.class), any(Predicate.class))).thenReturn(predicate);
        PolicySearchCriteria criteria = PolicySearchCriteria.builder()
                .status(Status.ACTIVE)
                .lineOfBusiness(LineOfBusiness.PROPERTY)
                .region(Region.SINGAPORE)
                .effectiveDateFrom(LocalDate.of(2025, 1, 1))
                .effectiveDateTo(LocalDate.of(2025, 12, 31))
                .search("Acme")
                .build();

        Predicate result = PolicySpecification.fromCriteria(criteria).toPredicate(root, query, cb);

        assertNotNull(result);
    }

    @Test
    void fromCriteria_whenNoFiltersPresent_composesConjunctionsOnly() {
        when(cb.and(any(Predicate.class), any(Predicate.class))).thenReturn(predicate);

        Predicate result = PolicySpecification.fromCriteria(PolicySearchCriteria.builder().build())
                .toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb, atLeastOnce()).conjunction();
    }
}
