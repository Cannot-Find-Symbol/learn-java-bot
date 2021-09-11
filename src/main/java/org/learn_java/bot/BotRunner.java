package org.learn_java.bot;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.learn_java.bot.commands.SlashCommand;
import org.learn_java.bot.configuration.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.security.auth.login.LoginException;
import java.util.Arrays;
import java.util.Objects;

@Component
public class BotRunner implements CommandLineRunner {

    static final Logger logger = LoggerFactory.getLogger(BotRunner.class);

    private final Command[] commands;
    private final ListenerAdapter[] listeners;
    private final Config config;
    private final SlashCommand[] slashCommands;

    public BotRunner(Command[] commands, ListenerAdapter[] listeners, SlashCommand[] slashCommands, Config config) {
        this.commands = commands;
        this.listeners = listeners;
        this.slashCommands = slashCommands;
        this.config = config;
    }

    @Override
    public void run(String... args) {
        CommandClientBuilder builder = new CommandClientBuilder();
        builder.setOwnerId(config.getOwner());
        builder.setPrefix(config.getPrefix());
        builder.addCommands(commands);
        CommandClient client = builder.build();
        try {
           JDA jda =  JDABuilder.createDefault(config.getDiscordKey())
                    .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .addEventListeners(client)
                    .addEventListeners((Object[]) listeners)
                    .build();
           jda.awaitReady();
           CommandData[] commands = Arrays.stream(slashCommands).map(SlashCommand::getCommandData).toArray(CommandData[]::new);
           Objects.requireNonNull(jda.getGuildById(config.getGuildId())).updateCommands().addCommands(commands).queue();
        } catch (LoginException | InterruptedException e) {
            logger.error("Invalid API key, check application.properties");
        }
    }
}
