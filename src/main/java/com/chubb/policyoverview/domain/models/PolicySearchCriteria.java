package com.chubb.policyoverview.domain.models;

import java.time.LocalDate;
import java.util.Optional;

public final class PolicySearchCriteria {

    private final Status status;
    private final LineOfBusiness lineOfBusiness;
    private final Region region;
    private final LocalDate effectiveDateFrom;
    private final LocalDate effectiveDateTo;
    private final String search;

    private PolicySearchCriteria(Builder builder) {
        this.status = builder.status;
        this.lineOfBusiness = builder.lineOfBusiness;
        this.region = builder.region;
        this.effectiveDateFrom = builder.effectiveDateFrom;
        this.effectiveDateTo = builder.effectiveDateTo;
        this.search = builder.search;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Optional<Status> getStatus() {
        return Optional.ofNullable(status);
    }

    public Optional<LineOfBusiness> getLineOfBusiness() {
        return Optional.ofNullable(lineOfBusiness);
    }

    public Optional<Region> getRegion() {
        return Optional.ofNullable(region);
    }

    public Optional<LocalDate> getEffectiveDateFrom() {
        return Optional.ofNullable(effectiveDateFrom);
    }

    public Optional<LocalDate> getEffectiveDateTo() {
        return Optional.ofNullable(effectiveDateTo);
    }

    public Optional<String> getSearch() {
        return Optional.ofNullable(search).map(String::trim).filter(s -> !s.isEmpty());
    }

    public static final class Builder {

        private Status status;
        private LineOfBusiness lineOfBusiness;
        private Region region;
        private LocalDate effectiveDateFrom;
        private LocalDate effectiveDateTo;
        private String search;

        public Builder status(Status status) {
            this.status = status;
            return this;
        }

        public Builder lineOfBusiness(LineOfBusiness lineOfBusiness) {
            this.lineOfBusiness = lineOfBusiness;
            return this;
        }

        public Builder region(Region region) {
            this.region = region;
            return this;
        }

        public Builder effectiveDateFrom(LocalDate effectiveDateFrom) {
            this.effectiveDateFrom = effectiveDateFrom;
            return this;
        }

        public Builder effectiveDateTo(LocalDate effectiveDateTo) {
            this.effectiveDateTo = effectiveDateTo;
            return this;
        }

        public Builder search(String search) {
            this.search = search;
            return this;
        }

        public PolicySearchCriteria build() {
            return new PolicySearchCriteria(this);
        }
    }
}
