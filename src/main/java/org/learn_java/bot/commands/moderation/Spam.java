package org.learn_java.bot.commands.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import org.learn_java.bot.data.repositories.SpamRepository;
import org.springframework.stereotype.Component;

@Component
public class Spam extends Command {
  private SpamRepository repository;

  public Spam(SpamRepository repository) {
    this.repository = repository;
    this.name = "spam";
    this.requiredRole = "moderator";
  }

  @Override
  protected void execute(CommandEvent event) {
    if (event.getAuthor().isBot()) return;
    String message = event.getArgs().trim();
    if (repository.findByMessage(message) == null) {
      repository.save(new org.learn_java.bot.data.entities.Spam(message));
    } else {
      event.reply("That message already exists");
    }
  }
}
