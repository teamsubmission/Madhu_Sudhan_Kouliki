package com.chubb.policyoverview.domain.exception;

import java.util.UUID;

public class PolicyNotFoundException extends RuntimeException {

    public PolicyNotFoundException(UUID policyId) {
        super("Policy not found with id: " + policyId);
    }
}
