package org.learn_java.bot.commands.user;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.junit.jupiter.api.Test;
import org.learn_java.bot.service.MemberInfoService;

import static org.mockito.Mockito.*;

class WhoisTest {
    private final Whois command = new Whois(mock(MemberInfoService.class));
    private final SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class, RETURNS_DEEP_STUBS);

    WhoisTest() {
        when(event.getOption("member").getAsMember()).thenReturn(null);
    }

    @Test
    void defaultsToPrivateReply() {
        when(event.getOption("visible")).thenReturn(null);

        command.executeSlash(event);

        verify(event).deferReply(true);
    }

    @Test
    void trueVisibleOptionKeepsReplyPrivateAsDescribed() {
        var option = mock(OptionMapping.class);
        when(option.getAsBoolean()).thenReturn(true);
        when(event.getOption("visible")).thenReturn(option);

        command.executeSlash(event);

        verify(event).deferReply(true);
    }

    @Test
    void falseVisibleOptionMakesReplyPublic() {
        var option = mock(OptionMapping.class);
        when(option.getAsBoolean()).thenReturn(false);
        when(event.getOption("visible")).thenReturn(option);

        command.executeSlash(event);

        verify(event).deferReply(false);
    }
}
