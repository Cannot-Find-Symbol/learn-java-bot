package org.learn_java.bot.commands.user;


import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.jetbrains.annotations.NotNull;
import org.learn_java.bot.commands.Command;
import org.learn_java.bot.commands.CommandType;
import org.learn_java.bot.data.entities.MemberInfo;
import org.learn_java.bot.service.MemberInfoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Component
@ConditionalOnProperty(value = "whois.enabled", havingValue = "true", matchIfMissing = true)
public class WhoisCommand extends Command {

	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private final SlashCommandData commandData;
	private final MemberInfoService memberInfoService;

	public WhoisCommand(@Value("${whois.cooldown:20}") int cooldown, MemberInfoService memberInfoService) {
		super("whois", CommandType.ANY);
		this.memberInfoService = memberInfoService;
		this.commandData = Commands.slash(getName(), "Show information about member")
				.addOption(OptionType.USER, "member", "member name", true)
				.addOption(OptionType.BOOLEAN, "visible", "only visible to you", false);
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
	public void executeSlash(SlashCommandInteractionEvent event) {
		Member member = Objects.requireNonNull(event.getOption("member")).getAsMember();
		boolean ephemeral = event.getOption("visible") == null || Objects.requireNonNull(event.getOption("visible")).getAsBoolean();
		event.deferReply(!ephemeral).queue();

		if (member == null) {
			event.getHook().sendMessage("Cannot find member by that name").queue();
			return;
		}

		if (member.getUser().isBot()) {
			event.getHook().sendMessage("Whois doesn't work on bots").queue();
			return;
		}
		event.getHook().sendMessageEmbeds(buildWhoisEmbed(member)).queue();
	}

	@Override
	public SlashCommandData getSlashCommandData() {
		return commandData;
	}
}
