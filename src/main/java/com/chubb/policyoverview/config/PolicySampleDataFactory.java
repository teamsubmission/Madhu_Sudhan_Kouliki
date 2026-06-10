package com.chubb.policyoverview.config;

import com.chubb.policyoverview.domain.models.LineOfBusiness;
import com.chubb.policyoverview.domain.models.Region;
import com.chubb.policyoverview.domain.models.Status;
import com.chubb.policyoverview.infrastructure.persistence.entity.PolicyEntity;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

@Component
public class PolicySampleDataFactory {

    private static final double MIN_PREMIUM = 1000d;
    private static final double MAX_PREMIUM = 5_000_000d;
    private static final double FLAG_PROBABILITY = 0.15d;
    private static final int POLICY_TERM_YEARS = 1;
    private static final int MAX_START_OFFSET_DAYS = 730;
    private static final int POLICY_NUMBER_WIDTH = 6;

    private static final String[] GIVEN_NAMES = {
            "Wei", "Mei", "Hiroshi", "Yuki", "Arjun", "Priya", "Siti", "Budi",
            "Somchai", "Maria", "Jose", "Aisha", "Daniel", "Grace", "Ravi", "Lin"};
    private static final String[] FAMILY_NAMES = {
            "Tan", "Lim", "Sato", "Nair", "Wong", "Lee", "Suharto", "Rahman",
            "Cruz", "Reyes", "Chan", "Kumar", "Goh", "Ishikawa", "Wijaya"};
    private static final String[] UNDERWRITERS = {
            "Jane Lim", "Hiroshi Sato", "Priya Nair", "Daniel Wong",
            "Grace Chan", "Arjun Kumar", "Maria Cruz", "Budi Wijaya"};

    private static final Map<Region, String> REGION_CODES = regionCodes();
    private static final Map<Region, String> REGION_CURRENCIES = regionCurrencies();

    public List<PolicyEntity> createPolicies(int count) {
        List<PolicyEntity> policies = new ArrayList<>(count);
        for (int sequence = 1; sequence <= count; sequence++) {
            policies.add(createPolicy(sequence));
        }
        return policies;
    }

    private PolicyEntity createPolicy(int sequence) {
        Region region = randomElement(Region.values());
        LocalDate effectiveDate = randomEffectiveDate();
        Instant timestamp = effectiveDate.atStartOfDay(ZoneOffset.UTC).toInstant();
        PolicyEntity policy = new PolicyEntity(
                policyNumber(region, sequence),
                randomPolicyholderName(),
                randomElement(LineOfBusiness.values()),
                randomElement(Status.values()),
                randomPremium(),
                REGION_CURRENCIES.get(region),
                effectiveDate,
                effectiveDate.plusYears(POLICY_TERM_YEARS),
                region,
                randomElement(UNDERWRITERS),
                timestamp,
                timestamp);
        if (ThreadLocalRandom.current().nextDouble() < FLAG_PROBABILITY) {
            policy.flagForReview();
        }
        return policy;
    }

    private String policyNumber(Region region, int sequence) {
        return String.format("POL-%s-%0" + POLICY_NUMBER_WIDTH + "d", REGION_CODES.get(region), sequence);
    }

    private String randomPolicyholderName() {
        return randomElement(GIVEN_NAMES) + " " + randomElement(FAMILY_NAMES);
    }

    private BigDecimal randomPremium() {
        double amount = ThreadLocalRandom.current().nextDouble(MIN_PREMIUM, MAX_PREMIUM);
        return BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP);
    }

    private LocalDate randomEffectiveDate() {
        int offset = ThreadLocalRandom.current().nextInt(MAX_START_OFFSET_DAYS + 1);
        return LocalDate.now().minusDays(offset);
    }

    private <T> T randomElement(T[] values) {
        return values[ThreadLocalRandom.current().nextInt(values.length)];
    }

    private static Map<Region, String> regionCodes() {
        Map<Region, String> codes = new EnumMap<>(Region.class);
        codes.put(Region.SINGAPORE, "SG");
        codes.put(Region.HONG_KONG, "HK");
        codes.put(Region.AUSTRALIA, "AU");
        codes.put(Region.JAPAN, "JP");
        codes.put(Region.THAILAND, "TH");
        codes.put(Region.INDONESIA, "ID");
        codes.put(Region.MALAYSIA, "MY");
        codes.put(Region.PHILIPPINES, "PH");
        return codes;
    }

    private static Map<Region, String> regionCurrencies() {
        Map<Region, String> currencies = new EnumMap<>(Region.class);
        currencies.put(Region.SINGAPORE, "SGD");
        currencies.put(Region.HONG_KONG, "HKD");
        currencies.put(Region.AUSTRALIA, "AUD");
        currencies.put(Region.JAPAN, "JPY");
        currencies.put(Region.THAILAND, "THB");
        currencies.put(Region.INDONESIA, "IDR");
        currencies.put(Region.MALAYSIA, "MYR");
        currencies.put(Region.PHILIPPINES, "PHP");
        return currencies;
    }
}
