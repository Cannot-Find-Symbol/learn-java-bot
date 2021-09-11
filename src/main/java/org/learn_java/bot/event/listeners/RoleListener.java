package org.learn_java.bot.event.listeners;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

import org.learn_java.bot.data.entities.MemberRole;
import org.learn_java.bot.data.entities.RoleGroup;
import org.learn_java.bot.service.RoleGroupService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class RoleListener extends ListenerAdapter {
    private String roleChannelId;
    private String guildId;
    private RoleGroupService service;

    public RoleListener(@Value("${role.channelid}") String roleChannelId, @Value("${guild.id}") String guildId, RoleGroupService service) {
        this.roleChannelId = roleChannelId;
        this.guildId = guildId;
        this.service = service;
    }

    public void onReady(@Nonnull ReadyEvent event) {
        TextChannel roleChannel = Objects.requireNonNull(event.getJDA().getGuildById(guildId)).getTextChannelById(roleChannelId);
        // TODO this is really bad, fix it
        roleChannel.getIterableHistory().forEach(message -> message.delete().queue());
        List<Role> roles = event.getJDA().getGuildById(guildId).getRoles();
        List<RoleGroup> groups = service.findAll();
        groups.forEach(group -> {
            SelectionMenu.Builder menuBuilder = SelectionMenu.create("rolegroup" + ":" + group.getId());
            Map<Long, Role> discordRoles = findGroupRoles(group, roles);

            List<MemberRole> memberRoles = group.getRoles().stream().sorted(Comparator.comparing(MemberRole::getOrdinal)).collect(Collectors.toList());
            memberRoles.forEach(role -> {
                Role discordRole = discordRoles.get(role.getId());
                menuBuilder.addOption(discordRole.getName(), discordRole.getId(), role.getDescription());
            });
            roleChannel.sendMessage("Select a role").setActionRows(ActionRow.of(menuBuilder.build())).queue();
        });
    }

    private Map<Long, Role> findGroupRoles(RoleGroup group, List<Role> roles) {
        Map<Long, Role> groupRoles = new HashMap<>();
        group.getRoles().forEach(role -> {
            for (Role discordRole : roles) {
                if (discordRole.getIdLong() == role.getId()) {
                    groupRoles.put(discordRole.getIdLong(), discordRole);
                }
            }
        });
        return groupRoles;
    }

    public void onSelectionMenu(@Nonnull SelectionMenuEvent event) {
        String eventId = event.getComponentId();
        if (!eventId.startsWith("rolegroup")) return;
        event.deferReply(true).queue();
        String groupId = eventId.split(":")[1];
        Set<Long> roleIds = service.findById(Long.parseLong(groupId)).getRoles().stream()
                .map(MemberRole::getId)
                .collect(Collectors.toSet());

        Set<Role> roles = event.getGuild().getRoles()
                .stream()
                .filter(role -> roleIds.contains(role.getIdLong()))
                .collect(Collectors.toSet());

        Role role = event.getGuild().getRoleById(event.getValues().get(0));
        roles.remove(role);
        event.getGuild().modifyMemberRoles(event.getInteraction().getMember(), Collections.singletonList(role), roles).queue(succuess -> {
            event.getHook().sendMessage("Your role has been set").queue();
        });
    }
}