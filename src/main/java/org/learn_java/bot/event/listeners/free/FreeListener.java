package org.learn_java.bot.event.listeners.free;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.learn_java.bot.commands.user.run.Run;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Consumer;

@Component
@ConditionalOnProperty(value = "autofree.enabled", havingValue = "true", matchIfMissing = true)
public class FreeListener extends ListenerAdapter {
    private final int hours;
    private final Set<String> helpChannelIds;
    private final String availableCategoryId;
    private final String takenCategoryId;
    private JDA jda;
    private static final Logger logger = LoggerFactory.getLogger(FreeListener.class);


    public FreeListener(@Value("${free.hours:3}") int hours,
                        @Value("${help.channelids}") String helpChannelIds,
                        @Value("${available.categoryid}") String availableCategoryId,
                        @Value("${taken.categoryid}") String takenCategoryId) {
        this.hours = hours;
        this.helpChannelIds = new HashSet<>(Arrays.asList(helpChannelIds.split(",")));
        this.availableCategoryId = availableCategoryId;
        this.takenCategoryId = takenCategoryId;
    }

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        jda = event.getJDA();
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }
        moveChannel(event.getChannel(), takenCategoryId);
    }

    private void moveChannel(TextChannel channel, String categoryId) {
        Category parent = channel.getParent();
        if (!helpChannelIds.contains(channel.getId()) || (parent != null && parent.getId().equals(categoryId))) {
            return;
        }
        Category category = jda.getCategoryById(categoryId);
        channel.getManager().setParent(category).queue();
    }

    @Scheduled(cron = "* */15 * * * *")
    public void freeChannels() {
        if (jda == null) return;

        helpChannelIds.stream().map(jda::getTextChannelById)
                .filter(Objects::nonNull)
                .forEach((channel) -> channel.getHistory().retrievePast(5)
                        .queue(handleResponse(channel)));
    }

    private Consumer<List<Message>> handleResponse(TextChannel channel) {
        return messages -> messages.stream()
                .filter(this::isFromUser).findFirst().ifPresent(message -> freeIfPastLimit(channel, message));
    }

    private boolean isFromUser(Message message) {
        return !message.getAuthor().isBot();
    }

    private void freeIfPastLimit(TextChannel channel, Message message) {
        OffsetDateTime lastMessage = message.getTimeCreated();
        OffsetDateTime limit = OffsetDateTime.now(lastMessage.getOffset()).minus(Duration.ofHours(hours));
        if (lastMessage.isBefore(limit)) {
            moveChannel(channel, availableCategoryId);
        }
    }
}
