package org.learn_java.bot.event.listeners.spam;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.SelfMember;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.junit.jupiter.api.Test;
import org.learn_java.bot.data.repositories.SpamRepository;
import org.learn_java.bot.data.entities.Spam;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.concurrent.TimeUnit;

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

    @Test
    void doesNotTryToBanWhenBotLacksPermission() {
        var sendAction = mock(MessageCreateAction.class);
        var guild = mock(Guild.class);
        when(member.getUser()).thenReturn(user);
        when(member.getGuild()).thenReturn(guild);
        when(guild.getSelfMember()).thenReturn(mock(SelfMember.class));
        when(user.getIdLong()).thenReturn(42L);
        when(user.getAsMention()).thenReturn("<@42>");
        when(channel.getIdLong()).thenReturn(7L);
        when(channel.sendMessage(any(MessageCreateData.class))).thenReturn(sendAction);

        for (int count = 0; count < 6; count++) {
            listener.checkForRepeated("repeat", member, channel);
        }

        verify(member, never()).ban(anyInt(), any(TimeUnit.class));
        verify(user, never()).openPrivateChannel();
    }

    @Test
    void matchedSpamDoesNotAttemptBanWithoutPermission() {
        var event = mock(MessageReceivedEvent.class, RETURNS_DEEP_STUBS);
        var guild = mock(Guild.class);
        when(event.getChannelType()).thenReturn(ChannelType.TEXT);
        when(event.getMember()).thenReturn(member);
        when(event.getAuthor()).thenReturn(user);
        when(event.getMessage().getContentRaw()).thenReturn("blocked text");
        when(event.getMessage().getAttachments()).thenReturn(List.of());
        when(event.getChannel().asTextChannel()).thenReturn(channel);
        when(member.getUser()).thenReturn(user);
        when(member.getGuild()).thenReturn(guild);
        when(guild.getSelfMember()).thenReturn(mock(SelfMember.class));
        when(user.getIdLong()).thenReturn(42L);
        when(channel.getIdLong()).thenReturn(7L);
        when(repository.findByMessage("blocked text")).thenReturn(new Spam("blocked text"));

        listener.onMessageReceived(event);

        verify(member, never()).ban(anyInt(), any(TimeUnit.class));
        verify(event.getChannel()).sendMessage("You hit the spam filter, but I cannot ban you");
    }
}
