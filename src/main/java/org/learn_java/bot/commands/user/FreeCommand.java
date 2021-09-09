package org.learn_java.bot.commands.user;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(value = "free.enabled", havingValue = "true", matchIfMissing = true)
public class FreeCommand extends Command implements SlashCommand {

    private final CommandData commandData;
    private final String availableCategoryId;
    private final Set<String> helpChannelIds;

    public FreeCommand(@Value("${free.cooldown:5}") int cooldown,
                       @Value("${help.channelids}") String helpChannelIds,
                       @Value("${available.categoryid}") String availableCategoryId) {
        this.name = "free";
        this.cooldown = cooldown;
        this.commandData = new CommandData(name, "frees current channel");
        this.helpChannelIds = new HashSet<>(Arrays.asList(helpChannelIds.split(",")));
        this.availableCategoryId = availableCategoryId;
    }

    @Override
    protected void execute(CommandEvent event) {
        run(event.getTextChannel());
    }

    private boolean run(TextChannel channel) {
        net.dv8tion.jda.api.entities.Category parent = channel.getParent();
        if (!helpChannelIds.contains(channel.getId()) || (parent != null && parent.getId().equals(availableCategoryId))) {
            return false;
        }
        net.dv8tion.jda.api.entities.Category availableCategory = channel.getJDA().getCategoryById(availableCategoryId);
        channel.getManager().setParent(availableCategory).queue();
        return true;
    }

    @Override
    public void executeSlash(SlashCommandEvent event) {
        boolean success = run(event.getTextChannel());
        String msg = success ? "Success" : "Unable to free";
        event.reply(msg)
                .queue((s) -> s.deleteOriginal().queueAfter(5, TimeUnit.SECONDS));
    }

    @Override
    public CommandData getCommandData() {
        return commandData;
    }
}
