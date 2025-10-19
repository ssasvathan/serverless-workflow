package com.example;

import jakarta.ws.rs.*;
import java.util.Map;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;

@Path("/fraud")
@RegisterRestClient(configKey = "fraud")
@RegisterClientHeaders   // lets MP propagate headers configured above
@Consumes("application/json")
@Produces("application/json")
public interface FraudClient {
  @POST @Path("/check")
  Boolean check(Map<String, Object> order);
}

