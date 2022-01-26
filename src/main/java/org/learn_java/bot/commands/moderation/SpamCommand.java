package org.learn_java.bot.commands.moderation;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.learn_java.bot.commands.Command;
import org.learn_java.bot.commands.CommandType;
import org.learn_java.bot.data.entities.Spam;
import org.learn_java.bot.service.SpamService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@ConditionalOnProperty(value = "spam.enabled", havingValue = "true", matchIfMissing = true)
public class SpamCommand extends Command {
    private final SpamService service;
    private final CommandData commandData;

    public SpamCommand(SpamService service) {
        super("spam", CommandType.MODERATOR);
        this.commandData = new CommandData(getName(), "adds message to spam list");
        this.commandData.addOption(OptionType.STRING, "message", "message to add", true);
        this.service = service;
    }

    @Override
    public void executeSlash(SlashCommandEvent event) {
        if (event.getUser().isBot()) {
            return;
        }

        event.deferReply(true).queue();

        String message = Objects.requireNonNull(event.getOption("message")).getAsString().trim();
        if (service.findMessage(message) == null) {
            service.save(new Spam(message));
            event.getHook().sendMessage("Message added").queue();
        } else {
            event.getHook().sendMessage("That message already exists").queue();
        }
    }

    @Override
    public CommandData getCommandData() {
        return commandData;
    }
}
