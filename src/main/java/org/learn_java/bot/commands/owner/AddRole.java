package org.learn_java.bot.commands.owner;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.learn_java.bot.commands.SlashCommand;
import org.learn_java.bot.data.entities.MemberRole;
import org.learn_java.bot.data.entities.RoleGroup;
import org.learn_java.bot.event.listeners.RoleListener;
import org.learn_java.bot.service.RoleGroupService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AddRole implements SlashCommand {
    private String name;
    private CommandData commandData;
    private RoleGroupService service;
    private RoleListener listener;

    public AddRole(RoleGroupService service, RoleListener listener) {
        this.listener = listener;
        this.name = "manage-roles";
        commandData = new CommandData("manage-roles", "manage role groups");
        SubcommandData view = new SubcommandData("view", "view current roles");
        SubcommandData add  = new SubcommandData("add-role-to-group", "add role to group");
        add.addOption(OptionType.ROLE, "role", "role", true);
        add.addOption(OptionType.STRING, "description", "description", true);
        add.addOption(OptionType.INTEGER, "order", "ordinal value", true);
        add.addOption(OptionType.INTEGER, "group-id", "group id", true);

        SubcommandData createGroup = new SubcommandData("create-group", "Create new role group");
        createGroup.addOption(OptionType.CHANNEL, "channel", "channel id", true);
        createGroup.addOption(OptionType.BOOLEAN, "unique", "unique", false);
        createGroup.addOption(OptionType.STRING, "name", "group name", false);
        createGroup.addOption(OptionType.STRING, "message", "Message to be sent", false);

        commandData.addSubcommands(view, add, createGroup);
        commandData.setDefaultEnabled(false);
        this.service = service;
    }

    @Override
    public void executeSlash(SlashCommandEvent event) {
        if(event.getSubcommandName().equals("view")) {
            event.deferReply(true).queue();
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("Current groups/roles");
            service.findAll().forEach(group -> {
                List<Role> roles = event.getGuild().getRoles();
               group.getRoles().forEach(role -> {
                   builder.addField(group.getName() + " " + group.getId(), findRoleNameById(roles, role.getId()), false);
               });
            });
            event.getHook().sendMessageEmbeds(builder.build()).queue();
        } else if(event.getSubcommandName().equals("add-role-to-group")) {
            event.deferReply(true).queue();
            long groupId = event.getOption("group-id").getAsLong();
            RoleGroup group = service.findById(groupId);
            if(group == null) {
                event.getHook().sendMessage("Group id does not exist").queue();
                return;
            }
            Role role = event.getOptionsByType(OptionType.ROLE).get(0).getAsRole();
            String description = event.getOption("description").getAsString();
            int ordinal = (int) event.getOption("order").getAsLong();
            MemberRole memberRole = new MemberRole();
            memberRole.setId(role.getIdLong());
            memberRole.setDescription(description);
            memberRole.setOrdinal(ordinal);
            memberRole.setGroup(group);
            group.getRoles().add(memberRole);
            service.save(group);
            // move method into shared context
            listener.startup();
            event.getHook().sendMessage("Role added").queue();

        } else if(event.getSubcommandName().equals("create-group")) {
            event.deferReply(true).queue();
            RoleGroup group = new RoleGroup();
            MessageChannel channel = event.getOptionsByType(OptionType.CHANNEL).get(0).getAsMessageChannel();
            group.setGuildId(event.getGuild().getIdLong());
            OptionMapping uniqueMapping = event.getOption("unique");
            group.setUnique(uniqueMapping != null && uniqueMapping.getAsBoolean());
            group.setChannelId(channel.getIdLong());
            OptionMapping name = event.getOption("name");
            group.setName(name == null ? "" : name.getAsString());
            OptionMapping message = event.getOption("message");
            group.setMessage(message == null ? "" : message.getAsString());
            RoleGroup saved = service.save(group);
            event.getHook().sendMessage("Guild created, id is " + saved.getId()).queue();
        }
    }

    private String findRoleNameById(List<Role> roles, long roleId) {
        for(Role role : roles) {
            if(role.getIdLong() == roleId) {
                return role.getName();
            }
        }
        return "ERROR";
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
