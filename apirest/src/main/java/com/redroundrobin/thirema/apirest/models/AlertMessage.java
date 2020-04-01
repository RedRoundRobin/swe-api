package com.redroundrobin.thirema.apirest.models;

import java.util.List;

public class AlertMessage {
  private String reqType = "alert";
  private transient int alertId;
  private transient int entityId;
  private String sensorType;
  private int realDeviceId;
  private int realSensorId;
  private int currentThreshold;
  private int currentThresholdType;
  private List<String> telegramChatId;

  public String getReqType() {
    return reqType;
  }

  public void setReqType(String reqType) {
    this.reqType = reqType;
  }

  public int getAlertId() {
    return alertId;
  }

  public void setAlertId(int alertId) {
    this.alertId = alertId;
  }

  public int getRealDeviceId() {
    return realDeviceId;
  }

  public void setRealDeviceId(int realDeviceId) {
    this.realDeviceId = realDeviceId;
  }

  public int getRealSensorId() {
    return realSensorId;
  }

  public void setRealSensorId(int realSensorId) {
    this.realSensorId = realSensorId;
  }

  public int getCurrentThreshold() {
    return currentThreshold;
  }

  public void setCurrentThreshold(int currentThreshold) {
    this.currentThreshold = currentThreshold;
  }

  public int getCurrentThresholdType() {
    return currentThresholdType;
  }

  public void setCurrentThresholdType(int currentThresholdType) {
    this.currentThresholdType = currentThresholdType;
  }

  public List<String> getTelegramChatId() {
    return telegramChatId;
  }

  public void setTelegramChatId(List<String> telegramChatId) {
    this.telegramChatId = telegramChatId;
  }

  public int getEntityId() {
    return entityId;
  }

  public void setEntityId(int entityId) {
    this.entityId = entityId;
  }

  public String getSensorType() {
    return sensorType;
  }

  public void setSensorType(String sensorType) {
    this.sensorType = sensorType;
  }
}

