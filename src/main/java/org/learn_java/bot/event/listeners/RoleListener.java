package org.learn_java.bot.event.listeners;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.jetbrains.annotations.NotNull;
import org.learn_java.bot.data.entities.MemberRole;
import org.learn_java.bot.data.entities.RoleGroup;
import org.learn_java.bot.service.RoleGroupService;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class RoleListener extends ListenerAdapter implements Startup {
    private final RoleGroupService service;
    private final JDA jda;

    public RoleListener(RoleGroupService service, JDA jda) {
        this.service = service;
        this.jda = jda;
    }

    private MessageCreateData createRoleMessage(List<Role> roles, RoleGroup group) {
        List<SelectOption> options = extractRolesOptions(roles, group);

        StringSelectMenu menu = StringSelectMenu.create("rolegroup" + ":" + group.getId())
                .addOptions(options)
                .build();

        MessageCreateBuilder builder = new MessageCreateBuilder();
        builder.setContent(group.getMessage() == null ? "test" : group.getMessage());
        builder.setActionRow(menu);

        return builder.build();
    }

    @NotNull
    private List<SelectOption> extractRolesOptions(List<Role> roles, RoleGroup group) {
        Map<Long, Role> discordRoles = findGroupRoles(group, roles);

        List<MemberRole> memberRoles = extractSortedRoles(group);

        return memberRoles.stream()
                .map(memberRole -> transformRoleOption(memberRole, discordRoles))
                .filter(Objects::nonNull)
                .toList();
    }

    private SelectOption transformRoleOption(MemberRole memberRole, Map<Long, Role> discordRoles) {
        Role discordRole = discordRoles == null
                ? null
                : discordRoles.get(memberRole.getId());

        return discordRoles == null
                ? null
                : SelectOption.of(discordRole.getName(), discordRole.getId())
                        .withDescription(memberRole.getDescription());
    }

    @NotNull
    private List<MemberRole> extractSortedRoles(RoleGroup group) {
        return group == null
                ? Collections.emptyList()
                : group.getRoles()
                        .stream()
                        .sorted(Comparator.comparing(MemberRole::getOrdinal))
                        .toList();
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

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {

        String eventId = event.getComponentId();
        if (!eventId.startsWith("rolegroup")) {
            return;
        }
        event.deferReply(true).queue();
        String groupId = eventId.split(":")[1];
        Set<Long> roleIds = extractRoleIdsFomGroupId(groupId);

        final Guild guild = event.getGuild();

        if (guild == null || event.getInteraction().getMember() == null) {
            return;
        }

        Set<Role> roles = guild.getRoles()
                .stream()
                .filter(role -> roleIds.contains(role.getIdLong()))
                .collect(Collectors.toSet());

        Role role = guild.getRoleById(event.getValues().get(0));
        roles.remove(role);

        guild.modifyMemberRoles(event.getInteraction().getMember(), Collections.singletonList(role), roles)
                .queue(success ->
                    event.getHook()
                            .sendMessage("Your role has been set")
                            .queue());
    }

    @NotNull
    private Set<Long> extractRoleIdsFomGroupId(String groupId) {
        return service.findById(Long.parseLong(groupId))
                .getRoles()
                .stream()
                .map(MemberRole::getId)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public void startup() {
        List<RoleGroup> groups = service.findAll();
        groups.forEach(this::computeGroupMessage);
    }

    private void computeGroupMessage(RoleGroup group) {
        final Guild guild = jda.getGuildById(group.getGuildId());

        if (guild == null) {
            return;
        }
        List<Role> roles = guild.getRoles();
        TextChannel roleChannel = guild.getTextChannelById(group.getChannelId());

        if (roleChannel == null) {
            return;
        }

        MessageCreateData message = createRoleMessage(roles, group);
        if (group.getMessageId() == null) {
            roleChannel.sendMessage(message)
                    .queue(success -> updateMessageInformation(success.getIdLong(), group));

        } else {
            roleChannel.retrieveMessageById(group.getMessageId())
                    .queue(success -> success.editMessage(MessageEditData.fromCreateData(message)).queue(),
                            fail -> handleFailMessageRetrieving(group, roleChannel, message));
        }
    }

    private void handleFailMessageRetrieving(RoleGroup group, TextChannel roleChannel, MessageCreateData message) {
        roleChannel.sendMessage(message)
                .queue(success -> {
                        group.setMessageId(success.getIdLong());
                        service.save(group);
                });
    }

    public void updateMessageInformation(long messageId, RoleGroup group) {
        group.setMessageId(messageId);
        service.save(group);
    }
}