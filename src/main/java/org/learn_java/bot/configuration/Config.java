package org.learn_java.bot.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Config {

    @Value("${discord.key}")
    private String discordKey;

    @Value("${bot.prefix}")
    private String prefix;

    @Value("${bot.ownerid}")
    private String owner;

    @Value("${guild.id}")
    private String guildId;

    public Config() {
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

    public String getGuildId() {
        return guildId;
    }
}
