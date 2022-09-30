package org.learn_java.bot.event.listeners.context_commands;

import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.learn_java.bot.commands.ContextCommand;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ContextListener extends ListenerAdapter {

    private final List<ContextCommand> commands;

    public ContextListener(List<ContextCommand> commands) {
        this.commands = commands;
    }

    @Override
    public void onMessageContextInteraction(MessageContextInteractionEvent event) {
        String contextName = event.getName();
        commands.stream()
                .filter(c -> c.getName().equalsIgnoreCase(contextName))
                .findFirst()
                .ifPresent(value -> value.executeContextCommand(event));
    }
}
