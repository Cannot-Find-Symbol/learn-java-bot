package org.learn_java.bot.commands.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import org.learn_java.bot.data.entities.Spam;
import org.learn_java.bot.service.SpamService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "spam.enabled", havingValue = "true", matchIfMissing = true)
public class SpamCommand extends Command {
    private final SpamService service;

    public SpamCommand(SpamService service) {
        this.service = service;
        this.name = "spam";
        this.requiredRole = "moderator";
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getAuthor().isBot())
            return;
        String message = event.getArgs().trim();
        if (service.findMessage(message) == null) {
            service.save(new Spam(message));
        } else {
            event.reply("That message already exists");
        }
    }
}
