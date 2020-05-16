package org.learn_java.bot.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.stereotype.Component;

@Component
public class Code extends Command {

  private final Message message;

  public Code() {
    this.name = "code";
    message = buildMessage();
  }

  private Message buildMessage() {
    MessageBuilder builder = new MessageBuilder();
    builder.append("\nPlease format your code using the following format\n");
    builder.append("\\`\\`\\`java\n");
    builder.append("//your code");
    builder.append("\n\\`\\`\\`");
    builder.append("\n Which will result in a code block such as");
    builder.appendCodeBlock("int x = 3;\nSystem.out.println(x);", "java");
    return builder.build();
  }

  @Override
  protected void execute(CommandEvent commandEvent) {
    commandEvent.reply(message);
  }
}
