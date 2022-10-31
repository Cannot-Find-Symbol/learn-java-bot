package org.learn_java.bot.configuration;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.learn_java.bot.commands.Command;
import org.learn_java.bot.commands.ContextCommand;
import org.learn_java.bot.commands.SlashCommand;
import org.learn_java.bot.event.listeners.Startup;
import org.learn_java.bot.service.InfoService;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;

@Configuration
public class BotConfiguration {
    private final JDA jda;
    private final List<Command> commands;
    private final List<ContextCommand> contextCommands;
    private final ListenerAdapter[] listeners;
    private final List<Startup> startups;
    private final Config config;

    private final InfoService service;


    public BotConfiguration(JDA jda,
                            List<Command> commands,
                            List<ContextCommand> contextCommands, ListenerAdapter[] listeners,
                            List<Startup> startups,
                            Config config, InfoService service) {
        this.jda = jda;
        this.commands = commands;
        this.contextCommands = contextCommands;
        this.listeners = listeners;
        this.startups = startups;
        this.config = config;
        this.service = service;
    }

    @PostConstruct
    public void configure() {
        SlashCommandData[] slash = commands.stream().map(SlashCommand::getSlashCommandData).toArray(SlashCommandData[]::new);
        CommandData[] context = contextCommands.stream().map(ContextCommand::getContextCommandData).toArray(CommandData[]::new);
        SlashCommandData[] infoSlash = service.findAll().stream().map(info -> Commands.slash(info.getTopic(), info.getDescription())).toArray(SlashCommandData[]::new);

        jda.addEventListener((Object[]) listeners);
        startups.forEach(Startup::startup);

        Objects.requireNonNull(jda.getGuildById(config.getGuildId()))
                .updateCommands()
                .addCommands(slash)
                .addCommands(context)
                .addCommands(infoSlash)
                .queue();
    }
}
