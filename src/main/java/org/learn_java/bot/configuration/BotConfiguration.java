package org.learn_java.bot.configuration;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import org.learn_java.bot.commands.SlashCommand;
import org.learn_java.bot.event.listeners.Startup;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;

@Configuration
public class BotConfiguration {
    private final JDA jda;
    private final List<SlashCommand> slashCommands;
    private final List<Command> commands;
    private final ListenerAdapter[] listeners;
    private final List<Startup> startups;
    private final Config config;

    public BotConfiguration(JDA jda, List<SlashCommand> slashCommands, List<Command> commands, ListenerAdapter[] listeners, List<Startup> startups, Config config) {
        this.jda = jda;
        this.slashCommands = slashCommands;
        this.commands = commands;
        this.listeners = listeners;
        this.startups = startups;
        this.config = config;
    }

    @PostConstruct
    public void configure() {
        CommandClientBuilder builder = new CommandClientBuilder();
        builder.setOwnerId(config.getOwner());
        builder.setPrefix(config.getPrefix());
        commands.forEach(builder::addCommand);
        CommandClient client = builder.build();
        CommandData[] slash = slashCommands.stream().map(SlashCommand::getCommandData).toArray(CommandData[]::new);
        jda.addEventListener((Object[]) listeners);
        jda.addEventListener(client);
        startups.forEach(Startup::startup);
        Objects.requireNonNull(jda.getGuildById(config.getGuildId())).updateCommands().addCommands(slash).queue();
        enableOwnerSlashCommands();
    }

    private void enableOwnerSlashCommands() {
        Guild guild = jda.getGuildById(config.getGuildId());
        if(guild != null) {
            guild.retrieveCommands().queue((s) -> {
                s.stream().filter(command -> command.getName().equals("manage-roles")).forEach(command -> {
                    CommandPrivilege privilege = CommandPrivilege.enableUser(config.getOwner());
                    command.updatePrivileges(guild, privilege).queue();
                });
            });
        }
    }
}
