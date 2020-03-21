package com.redroundrobin.thirema.apirest.models;

public class AuthenticationRequestTelegram {

  private String telegramName;
  private String telegramChat;

  public AuthenticationRequestTelegram() {
  }

  public AuthenticationRequestTelegram(String telegramName, String telegramChat) {
    this.telegramName = telegramName;
    this.telegramChat = telegramChat;
  }

  public String getTelegramName() {
    return telegramName;
  }

  public void setTelegramName(String telegramName) {
    this.telegramName = telegramName;
  }

  public String getTelegramChat() {
    return telegramChat;
  }

  public void setTelegramChat(String telegramChat) {
    this.telegramChat = telegramChat;
  }
}
