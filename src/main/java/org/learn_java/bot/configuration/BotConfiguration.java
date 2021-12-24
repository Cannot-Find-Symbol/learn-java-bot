package org.learn_java.bot.configuration;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import org.jetbrains.annotations.NotNull;
import org.learn_java.bot.commands.CommandType;
import org.learn_java.bot.commands.SlashCommand;
import org.learn_java.bot.event.listeners.Startup;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
        enablePrivilegedSlashCommands();
    }

    private void enablePrivilegedSlashCommands() {
        Guild guild = jda.getGuildById(config.getGuildId());
        if(guild != null) {
            guild.retrieveCommands().queue((s) ->
                    processComands(guild, s));
        }
    }

    private void processComands(Guild guild, List<net.dv8tion.jda.api.interactions.commands.Command> s) {
        Set<String> ownerCommands = getCommandNamesByType(CommandType.OWNER);
        s.stream().filter(c -> ownerCommands.contains(c.getName())).forEach(command -> enableForOwner(config, command, guild));
        Set<String> moderatorCommands = getCommandNamesByType(CommandType.MODERATOR);
        s.stream().filter(c -> moderatorCommands.contains(c.getName())).forEach(command -> enableForModerators(config, command, guild));

    }

    private void enableForOwner(Config config, net.dv8tion.jda.api.interactions.commands.Command command, Guild guild) {
        CommandPrivilege privilege = CommandPrivilege.enableUser(config.getOwner());
        command.updatePrivileges(guild, privilege).queue();
    }

    private void enableForModerators(Config config, net.dv8tion.jda.api.interactions.commands.Command command, Guild guild) {
        config.getModeratorRoleIds().forEach(roleId -> {
            CommandPrivilege privilege = CommandPrivilege.enableRole(roleId);
            command.updatePrivileges(guild, privilege).queue();
        });
    }

    @NotNull
    private Set<String> getCommandNamesByType(CommandType type) {
        return slashCommands.stream()
                .filter(command -> command.getType() == type).map(SlashCommand::getName).collect(Collectors.toSet());
    }
}
