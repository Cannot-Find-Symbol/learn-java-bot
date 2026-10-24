package org.learn_java.bot.commands.user;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.learn_java.bot.commands.Command;
import org.learn_java.bot.commands.CommandType;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.stereotype.Component;

@Component
public class Version extends Command {
    private final BuildProperties buildProperties;
    private final GitProperties gitProperties;
    private final SlashCommandData commandData = Commands.slash("version", "Show the bot version and commit");

    public Version(BuildProperties buildProperties, GitProperties gitProperties) {
        super("version", CommandType.ANY);
        this.buildProperties = buildProperties;
        this.gitProperties = gitProperties;
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event) {
        String version = buildProperties.getVersion();
        String label = version.endsWith("-SNAPSHOT") ? version : "v" + version;
        event.reply("Version: `" + label + "`\nCommit: `" + gitProperties.getShortCommitId() + "`").queue();
    }

    @Override
    public SlashCommandData getSlashCommandData() {
        return commandData;
    }
}
