package org.learn_java.bot.commands.user;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.junit.jupiter.api.Test;
import org.learn_java.bot.data.dtos.InfoDTO;
import org.learn_java.bot.service.InfoService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class InfoTest {
    private final InfoService service = mock(InfoService.class);
    private final Info command = new Info(service);
    private final SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class, RETURNS_DEEP_STUBS);
    private final InteractionHook hook = mock(InteractionHook.class, RETURNS_DEEP_STUBS);

    @Test
    void topicCommandSendsTheStoredMessage() {
        when(event.getHook()).thenReturn(hook);
        when(event.getOption("topic").getAsString()).thenReturn("java");
        when(service.findById("java")).thenReturn(Optional.of(new org.learn_java.bot.data.entities.Info("java", "Java help", "Help")));

        command.sendTopic(event);

        verify(hook).sendMessage("Java help");
    }

    @Test
    void topicCommandExplainsWhenTheTopicIsMissing() {
        when(event.getHook()).thenReturn(hook);
        when(event.getOption("topic").getAsString()).thenReturn("missing");
        when(service.findById("missing")).thenReturn(Optional.empty());

        command.sendTopic(event);

        verify(hook).sendMessage("Sorry, couldn't find a topic by that name");
    }

    @Test
    void listCommandShowsEveryTopicInTheEmbed() {
        when(event.getHook()).thenReturn(hook);
        var java = new InfoDTO();
        java.setTopic("java");
        var help = new InfoDTO();
        help.setTopic("help");
        when(service.findAll()).thenReturn(List.of(java, help));

        command.listTopics(event);

        var embed = org.mockito.ArgumentCaptor.forClass(MessageEmbed.class);
        verify(hook).sendMessageEmbeds(embed.capture());
        assertEquals("List of topics", embed.getValue().getTitle());
        assertTrue(embed.getValue().getDescription().contains("1.  java"));
        assertTrue(embed.getValue().getDescription().contains("2.  help"));
    }
}
