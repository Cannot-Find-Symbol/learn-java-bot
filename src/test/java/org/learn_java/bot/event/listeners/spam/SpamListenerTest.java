package org.learn_java.bot.event.listeners.spam;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.junit.jupiter.api.Test;
import org.learn_java.bot.data.repositories.SpamRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SpamListenerTest {
    private final SpamRepository repository = mock(SpamRepository.class);
    private final JDA jda = mock(JDA.class);
    private final SpamListener listener = new SpamListener(repository, jda, "logs", "appeal-link");
    private final Member member = mock(Member.class);
    private final User user = mock(User.class);
    private final TextChannel channel = mock(TextChannel.class);

    @Test
    void sendsOneWarningWhenMemberSendsFifthMatchingMessage() {
        var sendAction = mock(MessageCreateAction.class);
        when(member.getUser()).thenReturn(user);
        when(user.getIdLong()).thenReturn(42L);
        when(user.getAsMention()).thenReturn("<@42>");
        when(channel.getIdLong()).thenReturn(7L);
        when(channel.sendMessage(any(MessageCreateData.class))).thenReturn(sendAction);

        for (int count = 0; count < 4; count++) {
            listener.checkForRepeated("repeat", member, channel);
        }
        verify(channel, never()).sendMessage(any(MessageCreateData.class));

        listener.checkForRepeated("repeat", member, channel);

        verify(channel, times(1)).sendMessage(any(MessageCreateData.class));
        verify(sendAction, times(1)).queue(any(java.util.function.Consumer.class));
    }

    @Test
    void warningsAreTrackedSeparatelyForEachMember() {
        var sendAction = mock(MessageCreateAction.class);
        when(member.getUser()).thenReturn(user);
        when(user.getIdLong()).thenReturn(42L);
        when(user.getAsMention()).thenReturn("<@42>");
        when(channel.getIdLong()).thenReturn(7L);
        when(channel.sendMessage(any(MessageCreateData.class))).thenReturn(sendAction);

        for (int count = 0; count < 5; count++) {
            listener.checkForRepeated("repeat", member, channel);
        }

        var secondMember = mock(Member.class);
        var secondUser = mock(User.class);
        when(secondMember.getUser()).thenReturn(secondUser);
        when(secondUser.getIdLong()).thenReturn(43L);
        when(secondUser.getAsMention()).thenReturn("<@43>");
        for (int count = 0; count < 4; count++) {
            listener.checkForRepeated("repeat", secondMember, channel);
        }

        verify(channel, times(1)).sendMessage(any(MessageCreateData.class));
    }
}
