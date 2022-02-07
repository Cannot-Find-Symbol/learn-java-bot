package org.learn_java.bot.event.listeners.slash_commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.learn_java.bot.commands.delay.DelayHandler;
import org.learn_java.bot.commands.SlashCommand;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SlashListener extends ListenerAdapter {

    private final List<SlashCommand> commands;
    private final DelayHandler delayHandler;

    public SlashListener(List<SlashCommand> commands, DelayHandler delayHandler) {
        this.commands = commands;
        this.delayHandler = delayHandler;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String slashCommand = event.getName();
        commands.stream()
                .filter(c -> c.getName().equalsIgnoreCase(slashCommand))
                .findFirst()
                .ifPresent(command -> executeCommand(event, command));
    }

    private void executeCommand(SlashCommandInteractionEvent event, SlashCommand value) {
        if(event.getMember() == null) return;
        int delayUntilNextUse = delayHandler.secondsUntilNextUse(value.getName(), event.getMember().getIdLong(), value.getDelay());
        if(delayUntilNextUse > 0) {
            event.deferReply(true).queue();
            event.getHook().sendMessage("That command is on cooldown for another " + delayUntilNextUse + " seconds").queue();
        } else {
            value.executeSlash(event);
            delayHandler.trackUse(event.getMember().getIdLong(), value.getName(), event.getTimeCreated());
        }
    }
}
