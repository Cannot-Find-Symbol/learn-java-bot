package org.learn_java.bot.commands.moderation;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.SelfMember;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class BanPermissionsTest {
    private final Guild guild = mock(Guild.class);
    private final SelfMember bot = mock(SelfMember.class);
    private final Member target = mock(Member.class);

    BanPermissionsTest() {
        when(target.getGuild()).thenReturn(guild);
        when(guild.getSelfMember()).thenReturn(bot);
    }

    @Test
    void requiresBanPermissionEvenForARegularMember() {
        when(bot.canInteract(target)).thenReturn(true);

        assertFalse(BanPermissions.canBan(target));
    }

    @Test
    void requiresRoleHierarchyPermission() {
        when(bot.hasPermission(Permission.BAN_MEMBERS)).thenReturn(true);

        assertFalse(BanPermissions.canBan(target));
    }

    @Test
    void allowsBanOnlyWhenBothChecksPass() {
        when(bot.hasPermission(Permission.BAN_MEMBERS)).thenReturn(true);
        when(bot.canInteract(target)).thenReturn(true);

        assertTrue(BanPermissions.canBan(target));
    }
}
