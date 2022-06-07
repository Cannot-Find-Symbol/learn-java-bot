package org.learn_java.bot.commands.user;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.learn_java.bot.commands.Command;
import org.learn_java.bot.commands.CommandType;
import org.learn_java.bot.manager.QuestionManager;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class Edit extends Command {
	private final SlashCommandData commandData;
	private final QuestionManager questionManager;
	public Edit(QuestionManager questionManager) {
		super("edit", CommandType.ANY);
		this.questionManager = questionManager;
		this.commandData = Commands.slash(getName(), "Edit thread name")
				.addOption(OptionType.STRING, "name", "Thread name", true);
	}
	@Override
	public void executeSlash(SlashCommandInteractionEvent event) {
		event.deferReply(true).queue();
		if(ChannelType.GUILD_PUBLIC_THREAD != event.getChannel().getType()){
			event.getHook().sendMessage("Sorry, can't use this here").queue();
			return;
		}

		if(!questionManager.doesUserOwnThread(event.getUser().getIdLong(), event.getThreadChannel().getIdLong())){
			event.getHook().sendMessage("Sorry, you don't own this thread").queue();
			return;
		}

		String name = Objects.requireNonNull(event.getOption("name")).getAsString();
		event.getThreadChannel().getManager().setName(name).queue();
		event.getHook().sendMessage("Thread name changed").queue();
	}

	@Override
	public SlashCommandData getSlashCommandData() {
		return commandData;
	}
}
