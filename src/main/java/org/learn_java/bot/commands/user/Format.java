package org.learn_java.bot.commands.user;

import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.RestAction;
import org.learn_java.bot.commands.ContextCommand;
import org.springframework.stereotype.Component;

@Component
public class Format implements ContextCommand {
    private final CommandData contextCommandData;

    public Format() {
        contextCommandData = Commands.context(Command.Type.MESSAGE, "Format");
    }

    @Override
    public void executeContextCommand(MessageContextInteractionEvent event) {
        event.deferReply().queue();
        if (!event.getName().equals("Format")) return;
        String wrappedMessage = String.format("```%s\n%s\n```", "java", event.getTarget().getContentRaw());
        event.getHook().sendMessage(wrappedMessage).queue(RestAction.getDefaultSuccess(), error -> event.getHook().sendMessage("Something bad happened, couldn't format").queue());
    }

    @Override
    public CommandData getContextCommandData() {
        return contextCommandData;
    }

    @Override
    public String getName() {
        return "Format";
    }
}
