package com.redroundrobin.thirema.apirest.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
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
    data.put("reqType", "authentication");
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

  private CountDownLatch latch = new CountDownLatch(3);

  @KafkaListener(topics = "alerts", groupId = "alerts",
      containerFactory = "objectListKafkaListenerContainerFactory")
  public void sendAlerts(Object[] objectList) {
    for (Object obj : objectList) {
      System.out.println("Received Messasge in group 'alerts': \n\t" + obj);
      restTemplate.postForEntity(telegramUrl, obj, String.class);
    }
    latch.countDown();
  }
}
