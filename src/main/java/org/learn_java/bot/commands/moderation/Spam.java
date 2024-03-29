package org.learn_java.bot.commands.moderation;


import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.learn_java.bot.commands.Command;
import org.learn_java.bot.commands.CommandType;
import org.learn_java.bot.service.SpamService;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class Spam extends Command {
    private final SpamService service;
    private final SlashCommandData commandData;

    public Spam(SpamService service) {
        super("spam", CommandType.MODERATOR);
        this.commandData = Commands.slash("spam", "adds message to spam list")
                .addOption(OptionType.STRING, "message", "message to add")
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS));
        this.service = service;
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event) {
        if (event.getUser().isBot()) {
            return;
        }

        event.deferReply(true).queue();

        String message = Objects.requireNonNull(event.getOption("message")).getAsString().trim();
        if (service.findMessage(message) == null) {
            service.save(new org.learn_java.bot.data.entities.Spam(message));
            event.getHook().sendMessage("Message added").queue();
        } else {
            event.getHook().sendMessage("That message already exists").queue();
        }
    }

    @Override
    public SlashCommandData getSlashCommandData() {
        return commandData;
    }
}
