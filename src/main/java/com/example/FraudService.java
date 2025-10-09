package com.example;

import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Simple fraud check:
 * - Approve when amount <= 10000
 * - Reject otherwise
 */
@ApplicationScoped
public class FraudService {

    @SuppressWarnings("unchecked")
    public boolean check(Map<String, Object> order) {
        if (order == null) return false;
        Object amountObj = order.get("amount");
        if (amountObj == null) return false;

        double amount;
        if (amountObj instanceof Number) {
            amount = ((Number) amountObj).doubleValue();
        } else {
            amount = Double.parseDouble(String.valueOf(amountObj));
        }
        return amount <= 10000;
    }
}

