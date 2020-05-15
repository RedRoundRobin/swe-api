package com.redroundrobin.thirema.apirest.service;

import com.redroundrobin.thirema.apirest.controller.AlertController;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  Logger logger = LoggerFactory.getLogger(AlertController.class);

  @Value("${telegram.url}")
  private String telegramUrl;

  private final RestTemplate restTemplate;

  public TelegramService() {
    this.restTemplate = new RestTemplate();
  }

  public TelegramService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

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

  @KafkaListener(topics = "alerts", groupId = "alerts",
      containerFactory = "objectListKafkaListenerContainerFactory")
  public void sendAlerts(Object[] objectList) {
    for (Object obj : objectList) {
      logger.debug("Received Messasge in group 'alerts': \n\t" + obj);
      try {
        restTemplate.postForEntity(telegramUrl, obj, String.class);
      } catch (RestClientResponseException | ResourceAccessException rae) {
        logger.trace(rae.toString());
      }
    }
    new CountDownLatch(3).countDown();
  }
}
