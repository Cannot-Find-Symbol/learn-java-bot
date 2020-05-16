package org.learn_java;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import net.dv8tion.jda.api.JDABuilder;
import org.learn_java.configuration.Config;
import org.learn_java.event.listeners.code_block.CodeBlockListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.security.auth.login.LoginException;

@Component
public class BotRunner implements CommandLineRunner {

    final static Logger logger = LoggerFactory.getLogger(BotRunner.class);

    private final Command[] commands;
    private final Config config;

    public BotRunner(Command[] commands, Config config){
        this.commands = commands;
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
            JDABuilder.createDefault(config.getDiscordKey()).addEventListeners(client, new CodeBlockListener()).build();
        } catch (LoginException e) {
            logger.error("Invalid API key, check application.properties");
        }
    }
}
