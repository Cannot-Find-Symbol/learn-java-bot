package org.learn_java.bot.event.listeners.free;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(value = "autofree.enabled", havingValue = "true", matchIfMissing = true)
public class FreeListener extends ListenerAdapter {
    private final int hours;
    private final Set<String> helpChannelIds;
    private final String availableCategoryId;
    private final String takenCategoryId;
    private JDA jda;


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
        if(event.getAuthor().isBot()) {
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

    @Scheduled(cron = "* 0/15 * * * ?")
    public void freeChannels() {
        List<TextChannel> helpChannels = helpChannelIds.stream().map(jda::getTextChannelById).collect(Collectors.toList());
        helpChannels.forEach((channel) -> {
            if(channel.hasLatestMessage()) {
                String latestMessageId = channel.getLatestMessageId();
                channel.retrieveMessageById(latestMessageId).queue(e -> {
                    OffsetDateTime lastMessage = e.getTimeCreated();
                    OffsetDateTime limit = OffsetDateTime.now(lastMessage.getOffset()).minus(Duration.ofHours(hours));
                    if (lastMessage.isBefore(limit)) {
                        moveChannel(channel, availableCategoryId);
                    }
                });
            }
        });
    }
}
