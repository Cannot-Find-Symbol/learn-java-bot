package org.learn_java.bot.commands.user;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@ConditionalOnProperty(value = "whois.enabled", havingValue = "true", matchIfMissing = true)
public class WhoisCommand extends Command {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public WhoisCommand(@Value("${whois.cooldown:20}") int cooldown) {
        this.name = "whois";
        this.help = "gives information about the member";
        this.cooldown = cooldown;
    }

    @Override
    protected void execute(CommandEvent event) {
        List<Member> members = event.getMessage().getMentionedMembers();
        Member member = members.size() > 0 ? members.get(0) : null;

        if (member == null) {
            event.reply("Cannot find member by that name");
            return;
        }

        Duration timeOnServer = Duration.between(member.getTimeJoined(), OffsetDateTime.now());

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(String.format("Whois for: %s", member.getEffectiveName()));
        builder.addField("Date Joined", member.getTimeJoined().format(formatter), false);
        builder.addField("Time On Server", DurationFormatUtils.formatDurationWords(timeOnServer.toMillis(), true, true),
                false);

        event.reply(builder.build());
    }
}
