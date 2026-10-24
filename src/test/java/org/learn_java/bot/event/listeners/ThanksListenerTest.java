package org.learn_java.bot.event.listeners;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.junit.jupiter.api.Test;
import org.learn_java.bot.service.MemberInfoService;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ThanksListenerTest {
    private final ThanksListener listener = new ThanksListener(mock(MemberInfoService.class), "help-channel");
    private final MessageReceivedEvent event = mock(MessageReceivedEvent.class);
    private final Member thankingMember = member(1L, false);

    @Test
    void filterKeepsRecentDistinctHumanMembersAndExcludesThanker() {
        var first = member(2L, false);
        var second = member(3L, false);
        var messages = List.of(
                message(thankingMember, OffsetDateTime.now().minusMinutes(3)),
                message(first, OffsetDateTime.now().minusMinutes(2)),
                message(first, OffsetDateTime.now().minusMinutes(1)),
                message(second, OffsetDateTime.now().minusMinutes(30)));
        when(event.getMember()).thenReturn(thankingMember);

        assertEquals(List.of(first, second), listener.filterMemberList(messages, event));
    }

    @Test
    void filterExcludesBotsMessagesWithoutMembersAndMessagesOlderThanAnHour() {
        var bot = member(4L, true);
        var human = member(5L, false);
        var messages = List.of(
                message(bot, OffsetDateTime.now().minusMinutes(5)),
                message(null, OffsetDateTime.now().minusMinutes(5)),
                message(human, OffsetDateTime.now().minusHours(2)));
        when(event.getMember()).thenReturn(thankingMember);

        assertTrue(listener.filterMemberList(messages, event).isEmpty());
    }

    @Test
    void filterReturnsEmptyWhenThereIsNoRecentHistory() {
        when(event.getMember()).thenReturn(thankingMember);

        assertTrue(listener.filterMemberList(List.of(), event).isEmpty());
    }

    private Member member(long id, boolean isBot) {
        var member = mock(Member.class);
        var user = mock(User.class);
        when(member.getIdLong()).thenReturn(id);
        when(member.getUser()).thenReturn(user);
        when(user.isBot()).thenReturn(isBot);
        return member;
    }

    private Message message(Member member, OffsetDateTime time) {
        var message = mock(Message.class);
        when(message.getMember()).thenReturn(member);
        when(message.getTimeCreated()).thenReturn(time);
        return message;
    }
}
