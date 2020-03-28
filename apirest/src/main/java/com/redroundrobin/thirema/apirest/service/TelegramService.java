package com.redroundrobin.thirema.apirest.service;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@Service
public class TelegramService {

  @Value("${telegram.url}")
  private String telegramUrl;

  private RestTemplate restTemplate = new RestTemplate();

  public boolean sendTfa(Map<String, Object> data) {
    HttpEntity<Map<String,Object>> request = new HttpEntity<>(data);

    try {
      ResponseEntity<String> telegramResponse =
          restTemplate.postForEntity(telegramUrl, request, String.class);

      if (telegramResponse.getStatusCode().value() != 200) {
        throw new ResourceAccessException("");
      }
    } catch (RestClientResponseException | ResourceAccessException rae) {
      return false;
    }
    return true;
  }
}
