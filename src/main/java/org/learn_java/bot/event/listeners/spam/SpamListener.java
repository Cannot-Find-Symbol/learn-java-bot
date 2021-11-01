package org.learn_java.bot.event.listeners.spam;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.learn_java.bot.data.repositories.SpamRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(value = "spam.enabled", havingValue = "true", matchIfMissing = true)
public class SpamListener extends ListenerAdapter {

    private static final int REMOVE_HISTORY_DAYS = 1;
    private static final String REASON = "Matched spam filter";
    private final SpamRepository repository;

    private final Map<Long, MemberStatTracker> trackers = new ConcurrentHashMap<>();

    public SpamListener(SpamRepository repository) {
        this.repository = repository;
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (event.isWebhookMessage() || event.getMember() == null || event.getAuthor().isBot()) return;
        String message = event.getMessage().getContentRaw().trim();
        Member member = event.getMember();
        TextChannel channel = event.getChannel();

        if(!message.isEmpty()) {
            checkForRepeated(message, member, channel);
        }

        if(!event.getMessage().getAttachments().isEmpty()){
            for(Message.Attachment attachment : event.getMessage().getAttachments()){
                checkForRepeated(attachment.getFileName(), member, channel);
            }
        }

        checkIfMatchesSpam(event, message, event.getAuthor().getName());
    }

    public void checkForRepeated(String message, Member member, TextChannel channel) {
        MemberStatTracker tracker = trackers.computeIfAbsent(member.getUser().getIdLong(), MemberStatTracker::new);
        tracker.trackMessageOrFilename(message, channel.getIdLong());

        if (tracker.isWarned() && tracker.messageIsInViolation(message)) {
            channel.sendMessage("BOOM heashot...").queue();
            // should have been banned
            trackers.remove(member.getUser().getIdLong());
        }

        if (tracker.memberShouldBeWarned() && !tracker.isWarned()) {
            MessageBuilder builder = new MessageBuilder();
            builder.setContent("You just hit the rate limit. You've sent too many repeated messages either across channels, or to one channel. Next repeated message will result in a ban ");
            builder.append(member.getAsMention());
            channel.sendMessage(builder.build()).queue();
            tracker.setWarned(true);
        }
    }

    private void checkIfMatchesSpam(GuildMessageReceivedEvent event, String message, String author) {
        if (repository.findByMessage(message) != null) {
            Member member = event.getMember();
            if (member != null) {
                try {
                    member.ban(REMOVE_HISTORY_DAYS, REASON).flatMap(
                                    rest -> event.getChannel().sendMessage(author + " has hit the spam filter, goodbye"))
                            .queue();
                } catch (HierarchyException ex) {
                    event.getChannel().sendMessage("You hit the spam filter, but I cannot harm you master").queue();
                }
            }
        }
    }

    @Scheduled(cron = "* */15 * * * *")
    public void purgeOldMessageTrackers() {
        trackers.values().removeIf(v -> Duration.between(v.getLastUpdated(), LocalDateTime.now()).toMinutes() >= 30);
    }
}
