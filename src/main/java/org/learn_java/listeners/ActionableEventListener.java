package org.learn_java.listeners;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import org.jetbrains.annotations.NotNull;
import org.learn_java.event.ActionableEvent;

import java.util.ArrayList;
import java.util.List;


public class ActionableEventListener extends ListenerAdapter {

    List<ActionableEvent<GuildMessageReceivedEvent>> guildMessageReceivedEvents = new ArrayList<>();

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        for (ActionableEvent<GuildMessageReceivedEvent> actionableEvent : guildMessageReceivedEvents) {
            actionableEvent.handle(event);
        }
    }

    public void registerEvent(ActionableEvent<GuildMessageReceivedEvent> event) {
        guildMessageReceivedEvents.add(event);
    }
}
