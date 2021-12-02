package org.learn_java.bot.commands.user;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.jetbrains.annotations.NotNull;
import org.learn_java.bot.commands.SlashCommand;
import org.learn_java.bot.data.entities.MemberInfo;
import org.learn_java.bot.service.MemberInfoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@ConditionalOnProperty(value = "whois.enabled", havingValue = "true", matchIfMissing = true)
public class WhoisCommand extends Command implements SlashCommand {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final CommandData commandData;
    private final MemberInfoService memberInfoService;

    public WhoisCommand(@Value("${whois.cooldown:20}") int cooldown, MemberInfoService memberInfoService) {
        this.memberInfoService = memberInfoService;
        this.name = "whois";
        this.commandData = new CommandData(name, "Show information about member");
        commandData.addOption(OptionType.USER, "member", "member name", true);
        commandData.addOption(OptionType.BOOLEAN, "visible", "only visible to you", false);
        this.help = "shows information about member";
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

        event.reply(buildWhoisEmbed(member));
    }

    @NotNull
    private MessageEmbed buildWhoisEmbed(Member member) {
        Duration timeOnServer = Duration.between(member.getTimeJoined(), OffsetDateTime.now());

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(String.format("Whois for: %s", member.getEffectiveName()));
        builder.addField("Date Joined", member.getTimeJoined().format(formatter), false);
        builder.addField("Time On Server", DurationFormatUtils.formatDurationWords(timeOnServer.toMillis(), true, true),
                false);
        MemberInfo info = memberInfoService.findById(member.getIdLong());
        builder.addField("Current Month Thanks", String.valueOf(info.getMonthThankCount()), false);
        builder.addField("All Time Thanks", String.valueOf(info.getTotalThankCount()), false);
        return builder.build();
    }


    @Override
    public void executeSlash(SlashCommandEvent event) {
        Member member = event.getOption("member").getAsMember();
        boolean ephemeral = event.getOption("visible") == null || event.getOption("visible").getAsBoolean();
        event.deferReply(!ephemeral).queue();

        if (member == null) {
            event.getHook().sendMessage("Cannot find member by that name").queue();
            return;
        }

        if(member.getUser().isBot()) {
            event.getHook().sendMessage("Whois doesn't work on bots").queue();
            return;
        }
        event.getHook().sendMessageEmbeds(buildWhoisEmbed(member)).queue();
    }

    @Override
    public CommandData getCommandData() {
        return commandData;
    }
}
