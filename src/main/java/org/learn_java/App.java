package org.learn_java;

import net.dv8tion.jda.api.JDABuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.util.logging.ErrorManager;


public class App {
    final static Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        try {
            JDABuilder.createDefault(new Config().getDiscordKey()).build();
        } catch (LoginException e) {
            logger.error("Invalid API key, check application.properties");
        }
    }

}
