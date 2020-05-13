package org.learn_java;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.util.sqlite.SQLiteDSL;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {

    private String discordKey;
    private String prefix;
    private String owner;
    private static DSLContext dslContext;

    public Config() {

        try (InputStream input = getClass().getResourceAsStream("/application.properties")) {
            Properties properties = new Properties();
            properties.load(input);
            discordKey = properties.getProperty("discord.key");
            prefix = properties.getProperty("bot.prefix");
            owner = properties.getProperty("bot.owner.id");
            dslContext = DSL.using(properties.getProperty("db.url"));

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

    public static DSLContext getDslContext() {
        return dslContext;
    }
}