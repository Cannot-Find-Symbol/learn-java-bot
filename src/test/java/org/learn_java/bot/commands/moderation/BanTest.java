package org.learn_java.bot.commands.moderation;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.SelfMember;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

class BanTest {
    @Test
    void rejectsTargetWhenBotCannotBanIt() {
        var event = mock(SlashCommandInteractionEvent.class, RETURNS_DEEP_STUBS);
        var target = mock(Member.class);
        var guild = mock(Guild.class);
        when(event.getOption("member").getAsMember()).thenReturn(target);
        when(target.getGuild()).thenReturn(guild);
        when(guild.getSelfMember()).thenReturn(mock(SelfMember.class));

        new Ban().executeSlash(event);

        verify(target, never()).ban(anyInt(), any(TimeUnit.class));
        verify(event.getHook()).sendMessage("I don't have permission to ban that member");
    }
}
