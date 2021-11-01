package org.learn_java.bot.event.listeners.spam;

import java.time.LocalDateTime;
import java.util.*;

public class MemberStatTracker {
    private static final int MAX_REPEATED_MESSAGE = 5;
    private static final int MAX_REPEATED_DIFFERENT_CHANNELS = 2;
    private static final int UNIQUE_MESSAGES_DUMP_STATS_LIMIT = 5;
    private final long userId;
    private final Map<String, Integer> messageFrequencyTracker;
    private final Map<String, Set<Long>> messageToChannelTracker;
    private boolean warned;
    private LocalDateTime lastUpdated;
    private int uniqueConcurrentMessageCount;

    public MemberStatTracker(long userId) {
        uniqueConcurrentMessageCount = 0;
        messageFrequencyTracker = new HashMap<>();
        messageToChannelTracker = new HashMap<>();
        this.userId = userId;
    }

    public void trackMessageOrFilename(String message, long channelId){
        if(!messageFrequencyTracker.containsKey(message) && !messageToChannelTracker.containsKey(message)) {
            uniqueConcurrentMessageCount++;
        }

        if(uniqueConcurrentMessageCount >= UNIQUE_MESSAGES_DUMP_STATS_LIMIT) {
            messageFrequencyTracker.clear();
            messageToChannelTracker.clear();
            uniqueConcurrentMessageCount = 0;
        }

        messageFrequencyTracker.put(message, messageFrequencyTracker.getOrDefault(message, 1) + 1);
        Set<Long> channelIds = messageToChannelTracker.computeIfAbsent(message, (k) -> new HashSet<>());
        channelIds.add(channelId);
        lastUpdated = LocalDateTime.now();
    }

    public boolean memberShouldBeWarned() {
        return hasTooManyRepeatedMessagesAcrossChannels() || hasTooManyRepeatedMessages();
    }

    private boolean hasTooManyRepeatedMessages() {
        return messageFrequencyTracker.values().stream()
                .anyMatch(value -> value > MAX_REPEATED_MESSAGE);
    }

    private boolean hasTooManyRepeatedMessagesAcrossChannels() {
        return messageToChannelTracker.values().stream()
                .anyMatch(value -> value.size() > MAX_REPEATED_DIFFERENT_CHANNELS);
    }

    public boolean messageIsInViolation(String message) {
        Integer messageCount = messageFrequencyTracker.get(message);
        Set<Long> channelsMessageSentTo = messageToChannelTracker.get(message);

        return (messageCount != null && messageCount > MAX_REPEATED_MESSAGE)
                || (channelsMessageSentTo != null && channelsMessageSentTo.size() > MAX_REPEATED_DIFFERENT_CHANNELS);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MemberStatTracker that = (MemberStatTracker) o;
        return userId == that.userId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    public boolean isWarned() {
        return warned;
    }

    public void setWarned(boolean warned) {
        this.warned = warned;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
}
