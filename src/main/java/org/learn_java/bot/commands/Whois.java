package org.learn_java.bot.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.springframework.stereotype.Component;

@Component
public class Whois extends Command {

  private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  public Whois() {
    this.name = "whois";
    this.help = "gives information about the member";
    this.cooldown = 20;
  }

  @Override
  protected void execute(CommandEvent event) {
    String[] args = event.getArgs().split(" ");
    String memberName = args.length > 0 ? args[0] : "";

    List<Member> members = event.getGuild().getMembersByEffectiveName(memberName, true);
    Member member = members.size() > 0 ? members.get(0) : null;

    if (member == null) {
      event.reply("Cannot find member by that name");
      return;
    }

    Duration timeOnServer = Duration.between(member.getTimeJoined(), OffsetDateTime.now());

    EmbedBuilder builder = new EmbedBuilder();
    builder.setTitle(String.format("Whois for: %s", member.getEffectiveName()));
    builder.addField("Date Joined", member.getTimeJoined().format(formatter), false);
    builder.addField(
        "Time On Server",
        DurationFormatUtils.formatDurationWords(timeOnServer.toMillis(), true, true),
        false);

    event.reply(builder.build());
  }
}
