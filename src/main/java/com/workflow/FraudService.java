package com.workflow;

import java.util.Map;

public class FraudService {
  // Simple rule: decline if amount > 10000
  public Map<String, Object> check(Map<String, Object> input) {
    Number amount = (Number) input.getOrDefault("amount", 0);
    boolean approved = amount.doubleValue() <= 10_000.0;
    String reason = approved ? "ok" : "limit-exceeded";
    return Map.of("approved", approved, "reason", reason);
  }
}