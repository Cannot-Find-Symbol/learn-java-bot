package org.learn_java.bot.commands.owner;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.learn_java.bot.commands.SlashCommand;

public class AddRole implements SlashCommand {
    private String name;
    private CommandData commandData;


    public AddRole() {
        this.name = "addRole";
        commandData = new CommandData("manageRoles", "manage role groups");
        commandData.addOption(OptionType.STRING, "group", "create new role group", true);
        commandData.setDefaultEnabled(false);
    }

    @Override
    public void executeSlash(SlashCommandEvent event) {

    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public CommandData getCommandData() {
        return commandData;
    }
}
