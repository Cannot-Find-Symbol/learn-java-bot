package org.learn_java.bot.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public interface SlashCommand {
     void executeSlash(SlashCommandInteractionEvent event);
     String getName();
     SlashCommandData getSlashCommandData();
     default int getDelay() {
          return 0;
     }

     default int getQuickLimit() {
          return 2;
     }
}
