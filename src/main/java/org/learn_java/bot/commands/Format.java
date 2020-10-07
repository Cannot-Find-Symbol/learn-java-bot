package org.learn_java.bot.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.math.BigInteger;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class Format extends Command {

  private static final BigInteger MAX_MESSAGE_ID = new BigInteger(String.valueOf(Long.MAX_VALUE));

  public Format() {
    this.name = "format";
    this.help = "Formats users non formatted message";
    this.cooldown = 20;
  }

  @Override
  protected void execute(CommandEvent event) {

    if (event.getArgs().isBlank()) {
      event.reply("!format needs at least 1 argument");
      return;
    }

    String[] args = event.getArgs().trim().split(" ");

    if (args.length > 2) {
      event.reply("Too many arguments for format command, try again");
      return;
    }

    String messageId = args[0];
    String language = args.length < 2 ? "java" : args[1];

    if (!StringUtils.isNumeric(messageId)) {
      event.reply("message id must be numeric, try again");
      return;
    }

    if (new BigInteger(messageId).compareTo(MAX_MESSAGE_ID) > 0) {
      event.reply("Message id out of range, try again");
      return;
    }

    event
        .getChannel()
        .retrieveMessageById(messageId)
        .queue(
            message -> {
              String wrappedMessage =
                  String.format("```%s\n%s\n```", language, message.getContentRaw());
              message.getChannel().sendMessage(wrappedMessage).queue();
            },
            error -> event.reply("Something bad happened, couldn't format"));
  }
}
