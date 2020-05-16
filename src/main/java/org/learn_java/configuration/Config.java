package org.learn_java.configuration;

import org.springframework.beans.factory.annotation.Value;

public class Config {

  @Value("${discord.key}")
  private String discordKey;

  @Value("${bot.prefix}")
  private String prefix;

  @Value("${bot.ownerid}")
  private String owner;

  public Config() {}

  public String getDiscordKey() {
    return discordKey;
  }

  public String getPrefix() {
    return prefix;
  }

  public String getOwner() {
    return owner;
  }
}
