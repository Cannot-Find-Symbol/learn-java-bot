package org.learn_java.bot.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class Config {

    @Value("${discord.key}")
    private String discordKey;

    @Value("${guild.id}")
    private String guildId;

    private final Map<String, String> roleCommands = new HashMap<>();


    public Config() {
    }

    public String getDiscordKey() {
        return discordKey;
    }

    public String getGuildId() {
        return guildId;
    }

    @ConfigurationProperties(prefix = "rolecommand")
    @Bean
    public Map<String, String> getRoleCommands() {
        return this.roleCommands;
    }
}
