package org.learn_java.bot.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

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

    @Value("${moderator.roleids}")
    private List<String> moderatorRoleIds;

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

    public List<String> getModeratorRoleIds() {
        return moderatorRoleIds;
    }
}
