package org.learn_java.bot.commands.free;

import com.jagrosh.jdautilities.command.CommandEvent;
import org.springframework.stereotype.Component;

@Component
public class FreeAll extends Free {

  public FreeAll() {
    this.name = "free-all";
    this.cooldown = 5;
    this.requiredRole = "moderator";
  }

  @Override
  protected void execute(CommandEvent event) {
    event.getGuild().getChannels().stream()
        .filter(channel -> isValidForFree(channel.getName()))
        .forEach(channel -> setNameFree(channel.getManager()));
  }
  
}
