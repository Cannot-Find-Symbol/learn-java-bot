package org.learn_java;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {

    private String discordKey;
    private String prefix;
    private String owner;

    public Config() {

        try (InputStream input = getClass().getResourceAsStream("/application.properties")) {
            Properties properties = new Properties();
            properties.load(input);
            discordKey = properties.getProperty("discord.key");
            prefix = properties.getProperty("bot.prefix");
            owner = properties.getProperty("bot.owner.id");

        } catch (IOException e) {
            System.out.println("Cannot read settings file");
        }

    }

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