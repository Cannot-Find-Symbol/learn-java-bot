package org.learn_java.bot.commands;

import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public interface ContextCommand {
	void executeContextCommand(MessageContextInteractionEvent event);
	CommandData getContextCommandData();
	String getName();
}
