package com.redroundrobin.thirema.apirest.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.initialize.ExpectationInitializer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import static org.mockserver.model.HttpResponse.response;

public class TelegramServer implements ExpectationInitializer {

  @Override
  public void initializeExpectations(MockServerClient mockServerClient) {
    mockServerClient
        .when(
            HttpRequest.request()
                .withMethod("POST")
        ).callback(this::telegramTfaSimulator);
  }

  private HttpResponse telegramTfaSimulator(HttpRequest httpRequest) {
    String jsonString = (String)httpRequest.getBody().getValue();
    System.out.println(jsonString);

    JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();
    if (json.has("auth_code") && !json.get("auth_code").isJsonNull()
        && !json.get("auth_code").getAsString().isEmpty()
        && json.has("chat_id") && !json.get("chat_id").isJsonNull()
        && !json.get("chat_id").getAsString().isEmpty()) {
      return response().withStatusCode(200);
    } else {
      return response().withStatusCode(404);
    }
  }
}
