package org.learn_java.bot.event.listeners.spam;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.learn_java.bot.data.repositories.SpamRepository;
import org.springframework.stereotype.Component;

@Component
public class SpamListener extends ListenerAdapter {

  private SpamRepository repository;
  private static int REMOVE_HISTORY_DAYS = 1;
  private static String REASON = "Matched spam filter";

  public SpamListener(SpamRepository repository) {
    this.repository = repository;
  }

  @Override
  public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
    if (event.isWebhookMessage()) return;
    String message = event.getMessage().getContentRaw().trim();
    String author = event.getAuthor().getName();
    if (repository.findByMessage(message) != null) {
      Member member = event.getMember();
      if (member != null) {
        try {
          member
              .ban(REMOVE_HISTORY_DAYS, REASON)
              .flatMap(
                  rest ->
                      event.getChannel().sendMessage(author + " has hit the spam filter, goodbye"))
              .queue();
        } catch (HierarchyException ex) {
          event
              .getChannel()
              .sendMessage("You hit the spam filter, but I cannot harm you master")
              .queue();
        }
      }
    }
  }
}
