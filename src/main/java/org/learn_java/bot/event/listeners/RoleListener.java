package org.learn_java.bot.event.listeners;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
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
import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class RoleListener extends ListenerAdapter implements Startup {
    private RoleGroupService service;
    private JDA jda;

    public RoleListener(RoleGroupService service, JDA jda) {
        this.service = service;
        this.jda = jda;
    }

    private Message createRoleMessage(List<Role> roles, RoleGroup group) {
        MessageBuilder builder = new MessageBuilder();
        SelectionMenu.Builder menuBuilder = SelectionMenu.create("rolegroup" + ":" + group.getId());
        Map<Long, Role> discordRoles = findGroupRoles(group, roles);

        List<MemberRole> memberRoles = group.getRoles().stream()
                .sorted(Comparator.comparing(MemberRole::getOrdinal))
                .collect(Collectors.toList());

        memberRoles.forEach(role -> {
            Role discordRole = discordRoles.get(role.getId());
            menuBuilder.addOption(discordRole.getName(), discordRole.getId(), role.getDescription());
        });

        builder.setContent(group.getMessage() == null ? "test" : group.getMessage());
        builder.setActionRows(ActionRow.of(menuBuilder.build()));

        return builder.build();
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

    @Override
    @Transactional
    public void startup() {
        List<RoleGroup> groups = service.findAll();
        groups.forEach(group -> {
            List<Role> roles = jda.getGuildById(group.getGuildId()).getRoles();
            TextChannel roleChannel = jda.getGuildById(group.getGuildId()).getTextChannelById(group.getChannelId());
            Message message = createRoleMessage(roles, group);
            if (group.getMessageId() == null) {
                roleChannel.sendMessage(message).queue((success) -> updateMessageInformation(success.getIdLong(), group));
            } else {
                roleChannel.retrieveMessageById(group.getMessageId())
                        .queue((succuess) -> succuess.editMessage(message).queue()
                                ,(fail) -> roleChannel.sendMessage(message).queue(succ -> {
                                    System.out.println("here");
                                    group.setMessageId(succ.getIdLong());
                                    service.save(group);
                                }));
            }
        });
    }

    public void updateMessageInformation(long messageId, RoleGroup group) {
        group.setMessageId(messageId);
        service.save(group);
    }
}