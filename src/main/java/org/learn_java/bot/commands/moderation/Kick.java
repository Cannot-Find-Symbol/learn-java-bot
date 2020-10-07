package org.learn_java.bot.commands.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.Arrays;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import org.springframework.stereotype.Component;

@Component
public class Kick extends Command {

  public Kick() {
    this.name = "kick";
    this.requiredRole = "moderator";
  }

  @Override
  protected void execute(CommandEvent event) {
    String[] args = event.getArgs().split("\\s");
    if (args.length > 0) event.reply("Please specify : @users ... <reason>");
    else {
      String reason =
          Arrays.stream(args).filter(arg -> !arg.startsWith("<@")).collect(Collectors.joining(" "));
      event
          .getMessage()
          .getMentionedMembers()
          .forEach(
              member -> {
                try {
                  member.kick(reason).queue();
                } catch (HierarchyException ex) {
                  event.reply("Could not kick " + member.getAsMention());
                }
              });
    }
  }
}
