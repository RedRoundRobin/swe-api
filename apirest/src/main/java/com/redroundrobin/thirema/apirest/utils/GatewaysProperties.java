package com.redroundrobin.thirema.apirest.utils;

import org.springframework.beans.factory.annotation.Value;

public class GatewaysProperties {
  @Value(value = "${gateways.maxStoredPackets}")
  private static int maxStoredPackets;

  @Value(value = "${gateways.topic.telegram.prefix}")
  private static String gatewayCommandsPrefix;

  @Value(value = "${gateways.maxStoringTime}")
  private static int maxStoringTime;

  @Value(value = "${gateways.topic.config.prefix}")
  private static String configTopicPrefix;

  public static int getMaxStoredPackets() {
    return maxStoredPackets;
  }

  public static String getGatewayCommandsPrefix() {
    return gatewayCommandsPrefix;
  }

  public static int getMaxStoringTime() {
    return maxStoringTime;
  }

  public static String getConfigTopicPrefix() {
    return configTopicPrefix;
  }
}
