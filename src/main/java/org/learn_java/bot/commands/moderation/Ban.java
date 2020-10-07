package org.learn_java.bot.commands.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.EmbedBuilder;
import org.learn_java.bot.data.repositories.BanRepository;
import org.springframework.stereotype.Component;

@Component
public class Ban extends Command {

  final BanRepository repository;

  public Ban(BanRepository repository) {
    this.repository = repository;
    this.name = "ban";
    this.requiredRole = "moderator";
  }

  @Override
  protected void execute(CommandEvent event) {

    if (event.getArgs().isBlank()) {
      return;
    }

    String[] args = event.getArgs().split("\\s+");

    if (args[0].equals("show")) {
      event
          .getMessage()
          .getMentionedUsers()
          .forEach(
              user -> {
                List<org.learn_java.bot.data.entities.Ban> bans =
                    repository.findByUserID(user.getIdLong());
                if (bans.isEmpty()) {
                  event.reply("This user is not banned");
                  return;
                }
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle("ban for @" + user.getAsTag());
                bans.forEach( // only one ID per user
                    ban ->
                        eb.addField(
                            ban.isPermanent()
                                ? "Permanently banned"
                                : "Banned until " + ban.getDate(),
                            ban.getReason(),
                            false));
                event.reply(eb.build());
              });
      return;
    }

    List<org.learn_java.bot.data.entities.Ban> bans = new ArrayList<>();

    String reason =
        Arrays.stream(args).filter(m -> !m.startsWith("<@")).collect(Collectors.joining(" "));

    repository.saveAll(bans);
  }
}
