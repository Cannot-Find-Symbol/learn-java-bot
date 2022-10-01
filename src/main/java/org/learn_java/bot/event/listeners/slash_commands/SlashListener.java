package org.learn_java.bot.event.listeners.slash_commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.learn_java.bot.commands.SlashCommand;
import org.learn_java.bot.commands.delay.DelayHandler;
import org.learn_java.bot.data.dtos.InfoDTO;
import org.learn_java.bot.data.entities.Info;
import org.learn_java.bot.event.listeners.Startup;
import org.learn_java.bot.service.InfoService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SlashListener extends ListenerAdapter implements Startup {

    private final List<SlashCommand> commands;
    private final DelayHandler delayHandler;
    private final InfoService service;

    private final JDA jda;

    public SlashListener(List<SlashCommand> commands, DelayHandler delayHandler, InfoService service, JDA jda) {
        this.commands = commands;
        this.delayHandler = delayHandler;
        this.service = service;
        this.jda = jda;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String slashCommand = event.getName();
        commands.stream()
                .filter(c -> c.getName().equalsIgnoreCase(slashCommand))
                .findFirst()
                .ifPresentOrElse(command -> executeCommand(event, command), () -> this.handleInfoCommand(event));
    }

    private void handleInfoCommand(SlashCommandInteractionEvent event) {
        service.findById(event.getInteraction().getName()).ifPresent((info) -> sendInfoMessage(info, event));
    }

    private void sendInfoMessage(Info info, SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        event.getHook().sendMessage(info.getMessage()).queue();
    }


    private void executeCommand(SlashCommandInteractionEvent event, SlashCommand command) {
        if (event.getMember() == null) return;
        int delayUntilNextUse = delayHandler.secondsUntilNextUse(event.getMember().getIdLong(), command);
        if (delayUntilNextUse > 0) {
            event.deferReply(true).queue();
            event.getHook().sendMessage("That command is on cooldown for another " + delayUntilNextUse + " seconds").queue();
        } else {
            command.executeSlash(event);
            delayHandler.trackUse(event.getMember().getIdLong(), command.getName(), event.getTimeCreated());
        }
    }

    @Override
    public void startup() {
        List<SlashCommandData> commandData = service.findAll().stream().map(this::createCommandData).toList();
        jda.updateCommands().addCommands(commandData).queue();
    }

    private SlashCommandData createCommandData(InfoDTO infoDTO) {
        String description = infoDTO.getDescription() == null ? "info message" : infoDTO.getDescription();
        return Commands.slash(infoDTO.getTopic(), description);
    }
}
