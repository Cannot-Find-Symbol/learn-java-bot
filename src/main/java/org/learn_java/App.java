package org.learn_java;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import net.dv8tion.jda.api.JDABuilder;
import org.learn_java.commands.Code;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.util.logging.ErrorManager;


public class App {
    final static Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        Config config = new Config();
        CommandClientBuilder builder = new CommandClientBuilder();
        builder.setOwnerId(config.getOwner());
        builder.setPrefix(config.getPrefix());
        builder.addCommand(new Code());
        CommandClient client = builder.build();


        try {
            JDABuilder.createDefault(config.getDiscordKey()).addEventListeners(client).build();
        } catch (LoginException e) {
            logger.error("Invalid API key, check application.properties");
        }
    }

}
