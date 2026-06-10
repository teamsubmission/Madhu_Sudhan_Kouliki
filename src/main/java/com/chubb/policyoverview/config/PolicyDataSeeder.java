package com.chubb.policyoverview.config;

import com.chubb.policyoverview.infrastructure.persistence.entity.PolicyEntity;
import com.chubb.policyoverview.infrastructure.persistence.repository.PolicyRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class PolicyDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(PolicyDataSeeder.class);
    private static final int RECORD_COUNT = 250;

    private final PolicyRepository policyRepository;
    private final PolicySampleDataFactory sampleDataFactory;

    public PolicyDataSeeder(PolicyRepository policyRepository, PolicySampleDataFactory sampleDataFactory) {
        this.policyRepository = policyRepository;
        this.sampleDataFactory = sampleDataFactory;
    }

    @Override
    public void run(String... args) {
        long existing = policyRepository.count();
        if (existing > 0) {
            log.info("Policy table already populated with {} records; skipping seeding", existing);
            return;
        }
        List<PolicyEntity> policies = sampleDataFactory.createPolicies(RECORD_COUNT);
        policyRepository.saveAll(policies);
        log.info("Seeded policy table with {} records (page size {})", policies.size(), RECORD_COUNT);
    }
}
