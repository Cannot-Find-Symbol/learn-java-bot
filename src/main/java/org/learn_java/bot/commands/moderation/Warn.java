package org.learn_java.bot.commands.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import org.learn_java.bot.data.repositories.WarnRepository;
import org.springframework.stereotype.Component;

@Component
public class Warn extends Command {

  final WarnRepository repository;

  public Warn(WarnRepository repository) {
    this.repository = repository;
    this.name = "warn";
    this.requiredRole = "moderator";
  }

  @Override
  protected void execute(CommandEvent event) {

    if (event.getArgs().isBlank()) {
      return;
    }

    List<User> users = event.getMessage().getMentionedUsers();
    String command = event.getArgs().split("\\s+")[0];

    if (command.equals("show") && !users.isEmpty()) {
      User user = users.get(0);
      List<org.learn_java.bot.data.entities.Warn> warns = repository.findByUserID(user.getIdLong());
      EmbedBuilder eb = new EmbedBuilder();
      eb.setTitle("Warns for @" + user.getAsTag());
      warns.forEach(warn -> eb.addField("Warn on " + warn.getDate(), warn.getReason(), false));
      event.reply(eb.build());
      return;
    }

    List<String> args = List.of(event.getArgs().split("\\s+", users.size() + 1));

    List<org.learn_java.bot.data.entities.Warn> warns = new ArrayList<>();
    args.stream()
        .filter(m -> !m.startsWith("<@"))
        .findFirst()
        .ifPresentOrElse(
            reason ->
                users.forEach(
                    user ->
                        warns.add(
                            new org.learn_java.bot.data.entities.Warn(
                                user.getIdLong(), reason, LocalDateTime.now()))),
            () -> event.reply("A reason is mandatory"));
    repository.saveAll(warns);
  }
}
