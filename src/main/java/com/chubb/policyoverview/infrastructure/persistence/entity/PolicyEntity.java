package com.chubb.policyoverview.infrastructure.persistence.entity;

import com.chubb.policyoverview.domain.models.LineOfBusiness;
import com.chubb.policyoverview.domain.models.Region;
import com.chubb.policyoverview.domain.models.Status;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "policy")
public class PolicyEntity {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "policy_number", nullable = false, unique = true, length = 64)
    private String policyNumber;

    @Column(name = "policyholder_name", nullable = false, length = 255)
    private String policyholderName;

    @Enumerated(EnumType.STRING)
    @Column(name = "line_of_business", nullable = false, length = 32)
    private LineOfBusiness lineOfBusiness;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private Status status;

    @Column(name = "premium_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal premiumAmount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "region", nullable = false, length = 32)
    private Region region;

    @Column(name = "underwriter", nullable = false, length = 255)
    private String underwriter;

    @Column(name = "flagged_for_review", nullable = false)
    private boolean flaggedForReview;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PolicyEntity() {
    }

    public PolicyEntity(
            String policyNumber,
            String policyholderName,
            LineOfBusiness lineOfBusiness,
            Status status,
            BigDecimal premiumAmount,
            String currency,
            LocalDate effectiveDate,
            LocalDate expiryDate,
            Region region,
            String underwriter,
            Instant createdAt,
            Instant updatedAt) {
        this.policyNumber = policyNumber;
        this.policyholderName = policyholderName;
        this.lineOfBusiness = lineOfBusiness;
        this.status = status;
        this.premiumAmount = premiumAmount;
        this.currency = currency;
        this.effectiveDate = effectiveDate;
        this.expiryDate = expiryDate;
        this.region = region;
        this.underwriter = underwriter;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void flagForReview() {
        this.flaggedForReview = true;
    }

    public UUID getId() {
        return id;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public String getPolicyholderName() {
        return policyholderName;
    }

    public LineOfBusiness getLineOfBusiness() {
        return lineOfBusiness;
    }

    public Status getStatus() {
        return status;
    }

    public BigDecimal getPremiumAmount() {
        return premiumAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public Region getRegion() {
        return region;
    }

    public String getUnderwriter() {
        return underwriter;
    }

    public boolean isFlaggedForReview() {
        return flaggedForReview;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
