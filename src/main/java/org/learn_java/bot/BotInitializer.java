package org.learn_java.bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.learn_java.bot.configuration.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class BotInitializer {

    static final Logger logger = LoggerFactory.getLogger(BotInitializer.class);

    private final Config config;

    public BotInitializer(Config config) {
        this.config = config;
    }


    @Bean(name = "jda")
    public JDA initiateJDA() {
        JDA jda = null;
        try {
            jda = JDABuilder.createDefault(config.getDiscordKey())
                    .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES, GatewayIntent.MESSAGE_CONTENT)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .build();
            jda.awaitReady();
        } catch (InvalidTokenException | InterruptedException e) {
            logger.error("Invalid API key, check properties file");
        }
        return jda;
    }
}
