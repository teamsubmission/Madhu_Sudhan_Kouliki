package com.chubb.policyoverview.infrastructure.persistence.repository;

import com.chubb.policyoverview.domain.models.LineOfBusiness;
import com.chubb.policyoverview.domain.models.Status;
import com.chubb.policyoverview.infrastructure.persistence.entity.PolicyEntity;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyRepository
        extends JpaRepository<PolicyEntity, UUID>, JpaSpecificationExecutor<PolicyEntity> {

    long countByExpiryDateBetween(LocalDate startInclusive, LocalDate endInclusive);

    @Query("select p.status as status, count(p) as count from PolicyEntity p group by p.status")
    List<StatusCount> countGroupedByStatus();

    @Query("select p.lineOfBusiness as lineOfBusiness, sum(p.premiumAmount) as totalPremium "
            + "from PolicyEntity p group by p.lineOfBusiness")
    List<PremiumByLineOfBusiness> sumPremiumGroupedByLineOfBusiness();

    interface StatusCount {
        Status getStatus();

        long getCount();
    }

    interface PremiumByLineOfBusiness {
        LineOfBusiness getLineOfBusiness();

        BigDecimal getTotalPremium();
    }
}
