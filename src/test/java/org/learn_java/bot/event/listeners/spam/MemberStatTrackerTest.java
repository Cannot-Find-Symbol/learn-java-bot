package org.learn_java.bot.event.listeners.spam;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MemberStatTrackerTest {
    private final MemberStatTracker tracker = new MemberStatTracker(42L);

    @Test
    void newMemberHasNoViolations() {
        assertFalse(tracker.memberShouldBeWarned());
        assertFalse(tracker.messageIsInViolation("unknown"));
    }

    @Test
    void warnsOnFifthIdenticalMessage() {
        for (int count = 0; count < 4; count++) {
            tracker.trackMessageOrFilename("hello", 1L);
            assertFalse(tracker.memberShouldBeWarned());
        }

        tracker.trackMessageOrFilename("hello", 1L);

        assertTrue(tracker.memberShouldBeWarned());
        assertTrue(tracker.messageIsInViolation("hello"));
        assertFalse(tracker.messageIsInViolation("different"));
    }

    @Test
    void warnsWhenSameMessageAppearsInThreeDistinctChannels() {
        tracker.trackMessageOrFilename("hello", 1L);
        tracker.trackMessageOrFilename("hello", 1L);
        tracker.trackMessageOrFilename("hello", 2L);
        assertFalse(tracker.memberShouldBeWarned());

        tracker.trackMessageOrFilename("hello", 3L);

        assertTrue(tracker.memberShouldBeWarned());
        assertTrue(tracker.messageIsInViolation("hello"));
    }

    @Test
    void diverseMessagesClearPreviousSpamHistory() {
        for (int count = 0; count < 6; count++) {
            tracker.trackMessageOrFilename("repeated", 1L);
        }
        assertTrue(tracker.memberShouldBeWarned());

        for (int count = 0; count < 4; count++) {
            tracker.trackMessageOrFilename("unique-" + count, 1L);
        }

        assertFalse(tracker.memberShouldBeWarned());
        assertFalse(tracker.messageIsInViolation("repeated"));
    }

    @Test
    void fifthUniqueMessageResetsHistoryButKeepsTheNewMessage() {
        for (int count = 0; count < 4; count++) {
            tracker.trackMessageOrFilename("repeated", 1L);
        }
        for (int count = 0; count < 3; count++) {
            tracker.trackMessageOrFilename("unique-" + count, 1L);
        }

        tracker.trackMessageOrFilename("fifth", 1L);

        assertFalse(tracker.messageIsInViolation("repeated"));
        for (int count = 0; count < 4; count++) {
            tracker.trackMessageOrFilename("fifth", 1L);
        }
        assertTrue(tracker.messageIsInViolation("fifth"));
    }

    @Test
    void onlyTheMessageCrossingTheThresholdIsInViolation() {
        for (int count = 0; count < 4; count++) {
            tracker.trackMessageOrFilename("first", 1L);
        }
        tracker.trackMessageOrFilename("second", 1L);
        tracker.trackMessageOrFilename("first", 1L);

        assertTrue(tracker.messageIsInViolation("first"));
        assertFalse(tracker.messageIsInViolation("second"));
    }

    @Test
    void membersHaveIndependentHistories() {
        var otherMember = new MemberStatTracker(43L);
        for (int count = 0; count < 6; count++) {
            tracker.trackMessageOrFilename("hello", 1L);
        }

        assertFalse(otherMember.memberShouldBeWarned());
    }
}
