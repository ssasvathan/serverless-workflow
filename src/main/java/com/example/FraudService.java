package com.example;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.logging.Logger;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FraudService {

  private static final Logger LOG = Logger.getLogger(FraudService.class.getName());

  @ConfigProperty(name = "fraud.service.url", defaultValue = "http://localhost:8085/fraud/check")
  String fraudUrl;

  private final HttpClient http = HttpClient.newBuilder()
      .connectTimeout(Duration.ofSeconds(3))
      .build();

  @PostConstruct
  void onInit() {
    LOG.info(() -> "FraudService CDI bean initialized. fraud.service.url=" + fraudUrl);
  }

  /** Serverless Workflow custom function entrypoint. Must be public, non-static. */
  public boolean fraudCheck(Map<String, Object> order) {
    try {
      LOG.info(() -> "FraudService.check called with order=" + order);
      String body = com.fasterxml.jackson.databind.json.JsonMapper.builder()
          .findAndAddModules().build()
          .writeValueAsString(order == null ? Map.of() : order);

      LOG.info(() -> "FraudService.check → POST " + fraudUrl + " body=" + body);

      HttpRequest req = HttpRequest.newBuilder()
          .uri(URI.create(fraudUrl))
          .timeout(Duration.ofSeconds(7))
          .header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(body))
          .build();

      HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
      LOG.info(() -> "FraudService.check " + resp.statusCode() + " " + resp.body());

      if (resp.statusCode() / 100 != 2) {
        throw new RuntimeException("Fraud HTTP " + resp.statusCode() + ": " + resp.body());
      }

      String s = resp.body().trim();
      if ("true".equalsIgnoreCase(s) || "false".equalsIgnoreCase(s)) return Boolean.parseBoolean(s);

      var node = com.fasterxml.jackson.databind.json.JsonMapper.builder().findAndAddModules().build().readTree(s);
      if (node.has("fraudOk")) return node.get("fraudOk").asBoolean();
      if (node.has("ok"))      return node.get("ok").asBoolean();
      if (node.has("result"))  return node.get("result").asBoolean();

      throw new RuntimeException("Unexpected fraud response payload: " + s);
    } catch (Exception e) {
      LOG.severe("FraudService.check failed: " + e.getMessage());
      // Fail safe: reject on error so the flow is deterministic
      return false;
    }
  }

  public static boolean checkStatic(Map<String,Object> order) {
  System.out.println("### STATIC CHECK CALLED, order=" + order);
  return false;
}
}
