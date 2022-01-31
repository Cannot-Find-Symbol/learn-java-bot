package org.learn_java.bot.commands.moderation;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.learn_java.bot.commands.Command;
import org.learn_java.bot.commands.CommandType;
import org.learn_java.bot.data.dtos.WarnDTO;
import org.learn_java.bot.data.entities.Warn;
import org.learn_java.bot.service.WarnService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Component
@ConditionalOnProperty(value = "warn.enabled", havingValue = "true", matchIfMissing = true)
public class WarnCommand extends Command {

	private final WarnService service;
	private final SlashCommandData commandData;


	public WarnCommand(WarnService service) {
		super("warn", CommandType.MODERATOR);
		SubcommandData show = new SubcommandData("show", "show warns for user")
				.addOption(OptionType.MENTIONABLE, "member", "member", true);
		SubcommandData add = new SubcommandData("add", "add warn for user")
				.addOption(OptionType.MENTIONABLE, "member", "member", true)
				.addOption(OptionType.STRING, "reason", "reason for warn", true);

		this.commandData = Commands.slash(getName(), "warn utility")
				.addSubcommands(show, add);

		this.service = service;
	}

	public void handleShow(String userTag, TextChannel channel) {
		List<WarnDTO> warns = service.findByUsername(userTag);
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Warns for @" + userTag);
		warns.forEach(warn -> eb.addField("Warn on " + warn.getDate(), warn.getReason(), false));
		channel.sendMessageEmbeds(eb.build()).queue();
	}

	private Warn createWarn(User user, String reason) {
		return new Warn(user.getAsTag(), reason, LocalDate.now());
	}

	@Override
	public void executeSlash(SlashCommandInteractionEvent event) {
		event.deferReply(true).queue();
		String subcommandName = Objects.requireNonNull(event.getSubcommandName());
		User user = Objects.requireNonNull(event.getOption("member")).getAsUser();
		switch (subcommandName) {
			case "show" -> handleShow(user.getAsTag(), event.getTextChannel());
			case "add" -> handleAdd(event, user);
		}
	}

	private void handleAdd(SlashCommandInteractionEvent event, User user) {
		String reason = Objects.requireNonNull(event.getOption("reason")).getAsString();
		service.save(createWarn(user, reason));
		event.getHook().sendMessage("warn added").queue();
	}

	@Override
	public SlashCommandData getSlashCommandData() {
		return commandData;
	}
}
