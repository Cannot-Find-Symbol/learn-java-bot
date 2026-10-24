package org.learn_java.bot.event.listeners;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.junit.jupiter.api.Test;
import org.learn_java.bot.data.entities.MemberRole;
import org.learn_java.bot.data.entities.RoleGroup;
import org.learn_java.bot.service.RoleGroupService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RoleListenerTest {
    private final RoleGroupService service = mock(RoleGroupService.class);
    private final JDA jda = mock(JDA.class);
    private final RoleListener listener = new RoleListener(service, jda);

    @Test
    void missingDiscordRolesAreOmittedFromMenu() {
        var group = groupWithRole(2L);
        group.getRoles().add(memberRole(3L));
        var available = mock(Role.class);
        when(available.getIdLong()).thenReturn(3L);
        when(available.getId()).thenReturn("3");
        when(available.getName()).thenReturn("Java");

        assertNotNull(listener.createRoleMessage(List.of(available), group));
    }

    @Test
    void groupWithNoAvailableRolesHasNoMenu() {
        assertNull(listener.createRoleMessage(List.of(), groupWithRole(2L)));
    }

    @Test
    void startupSkipsGroupsWhoseRolesWereDeleted() {
        var group = groupWithRole(2L);
        group.setGuildId(42L);
        group.setChannelId(7L);
        var guild = mock(Guild.class);
        var channel = mock(TextChannel.class);
        when(service.findAll()).thenReturn(List.of(group));
        when(jda.getGuildById(42L)).thenReturn(guild);
        when(guild.getRoles()).thenReturn(List.of());
        when(guild.getTextChannelById(7L)).thenReturn(channel);

        listener.startup();

        verify(channel, never()).sendMessage(any(MessageCreateData.class));
    }

    @Test
    void selectionOfDeletedRoleGetsAnExplanation() {
        var group = groupWithRole(2L);
        var event = mock(StringSelectInteractionEvent.class, RETURNS_DEEP_STUBS);
        when(event.getComponentId()).thenReturn("rolegroup:1");
        when(event.getValues()).thenReturn(List.of("2"));
        when(service.findById(1L)).thenReturn(group);

        listener.onStringSelectInteraction(event);

        verify(event.getHook()).sendMessage("This role is no longer available");
    }

    private RoleGroup groupWithRole(long id) {
        var group = new RoleGroup();
        group.setId(1L);
        group.getRoles().add(memberRole(id));
        return group;
    }

    private MemberRole memberRole(long id) {
        var role = new MemberRole();
        role.setId(id);
        role.setDescription("Java");
        return role;
    }
}
