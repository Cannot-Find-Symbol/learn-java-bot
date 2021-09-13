package org.learn_java.bot.commands;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.List;
import java.util.function.Function;

public interface SlashCommand {
     void executeSlash(SlashCommandEvent event);
     String getName();
     CommandData getCommandData();
}
