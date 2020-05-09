package org.learn_java.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;

public class Format extends Command {

    private static final BigInteger MAX_MESSAGE_ID = new BigInteger(String.valueOf(Long.MAX_VALUE));

    public Format() {
        this.name = "format";
        this.help = "Formats users non formatted message";
        this.cooldown = 120;
    }

    @Override
    protected void execute(CommandEvent event) {
        String messageId = event.getArgs();

        if (!StringUtils.isNumeric(messageId)) {
            event.getChannel().sendMessage("message id must be numeric, try again").queue();
            return;
        }

        if (new BigInteger(messageId).compareTo(MAX_MESSAGE_ID) > 0) {
            event.getChannel().sendMessage("Message id out of range, try again").queue();
            return;
        }

        event.getChannel().retrieveMessageById(messageId).queue(message -> {
            String wrappedContent = StringUtils.wrap(message.getContentRaw(), "```");
            message.getChannel().sendMessage(wrappedContent).queue();
        }, error -> {
            event.getChannel().sendMessage("Something bad happened, couldn't format").queue();
        });
    }
}
