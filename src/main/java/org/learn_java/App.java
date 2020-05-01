package org.learn_java;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import net.dv8tion.jda.api.JDABuilder;
import org.learn_java.commands.Code;
import org.learn_java.event.event_handlers.CodeBlock;
import org.learn_java.listeners.ActionableEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;


public class App {
    final static Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        Config config = new Config();
        CommandClientBuilder builder = new CommandClientBuilder();
        builder.setOwnerId(config.getOwner());
        builder.setPrefix(config.getPrefix());
        builder.addCommand(new Code());
        CommandClient client = builder.build();

        ActionableEventListener eventListener = new ActionableEventListener();
        eventListener.registerEvent(new CodeBlock());

        try {
            JDABuilder.createDefault(config.getDiscordKey()).addEventListeners(client, eventListener).build();
        } catch (LoginException e) {
            logger.error("Invalid API key, check application.properties");
        }
    }

}
