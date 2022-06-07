package org.learn_java.bot.commands.moderation;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.learn_java.bot.commands.Command;
import org.learn_java.bot.commands.CommandType;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class SayCommand extends Command {
	private final SlashCommandData slashCommandData;
	public SayCommand() {
		super("say", CommandType.MODERATOR);
		slashCommandData = Commands.slash("say", "Say something")
				.addOption(OptionType.STRING, "message", "message", true)
				.setDefaultEnabled(false);
	}

	@Override
	public void executeSlash(SlashCommandInteractionEvent event) {
		event.deferReply(true).queue();
		event.getChannel().sendMessage(Objects.requireNonNull(event.getOption("message")).getAsString()).queue();
		event.getHook().sendMessage("done").queue();
	}

	@Override
	public SlashCommandData getSlashCommandData() {
		return this.slashCommandData;
	}
}
