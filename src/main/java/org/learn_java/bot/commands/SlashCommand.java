package org.learn_java.bot.commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public interface SlashCommand {
     void executeSlash(SlashCommandEvent event);
     String getName();
     CommandData getCommandData();
}
