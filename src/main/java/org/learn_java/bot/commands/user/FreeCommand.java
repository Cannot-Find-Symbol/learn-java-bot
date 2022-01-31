package org.learn_java.bot.commands.user;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.learn_java.bot.commands.Command;
import org.learn_java.bot.commands.CommandType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
@ConditionalOnProperty(value = "free.enabled", havingValue = "true", matchIfMissing = true)
public class FreeCommand extends Command {

    private final SlashCommandData commandData;
    private final String availableCategoryId;
    private final Set<String> helpChannelIds;

    public FreeCommand(@Value("${free.cooldown:5}") int cooldown,
                       @Value("${help.channelids}") String helpChannelIds,
                       @Value("${available.categoryid}") String availableCategoryId) {
        super("free", CommandType.ANY);
        this.commandData = Commands.slash(getName(), "frees current channel");
        this.helpChannelIds = new HashSet<>(Arrays.asList(helpChannelIds.split(",")));
        this.availableCategoryId = availableCategoryId;
    }

    private boolean run(TextChannel channel) {
        net.dv8tion.jda.api.entities.Category parent = channel.getParentCategory();
        if (!helpChannelIds.contains(channel.getId()) || (parent != null && parent.getId().equals(availableCategoryId))) {
            return false;
        }
        net.dv8tion.jda.api.entities.Category availableCategory = channel.getJDA().getCategoryById(availableCategoryId);
        channel.getManager().setParent(availableCategory).queue();
        return true;
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event) {
        event.deferReply(true).queue();
        String response = run(event.getTextChannel()) ? "Success" : "Unable to free";
        event.getHook().sendMessage(response).queue();
    }

    @Override
    public SlashCommandData getSlashCommandData() {
        return commandData;
    }
}
