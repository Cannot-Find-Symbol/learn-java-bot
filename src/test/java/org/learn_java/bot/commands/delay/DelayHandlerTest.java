package org.learn_java.bot.commands.delay;

import org.junit.jupiter.api.Test;
import org.learn_java.bot.commands.SlashCommand;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DelayHandlerTest {
    private final DelayHandler handler = new DelayHandler();

    @Test
    void allowsUseUntilQuickLimitIsReachedThenAppliesDelay() {
        var command = command("info", 120, 2);

        assertEquals(0, handler.secondsUntilNextUse(42L, command));
        handler.trackUse(42L, "info", OffsetDateTime.now().minusSeconds(20));
        assertEquals(0, handler.secondsUntilNextUse(42L, command));

        handler.trackUse(42L, "info", OffsetDateTime.now().minusSeconds(10));
        int remaining = handler.secondsUntilNextUse(42L, command);
        assertTrue(remaining >= 109 && remaining <= 110, "remaining seconds: " + remaining);
    }

    @Test
    void cooldownUsesMostRecentUseEvenWhenEventsArriveOutOfOrder() {
        var command = command("info", 120, 2);
        handler.trackUse(42L, "info", OffsetDateTime.now().minusSeconds(10));
        handler.trackUse(42L, "info", OffsetDateTime.now().minusSeconds(90));

        int remaining = handler.secondsUntilNextUse(42L, command);
        assertTrue(remaining >= 109 && remaining <= 110, "remaining seconds: " + remaining);
    }

    @Test
    void cooldownsAreSeparateForMembersAndCommands() {
        var info = command("info", 120, 1);
        var warn = command("warn", 120, 1);
        handler.trackUse(42L, "info", OffsetDateTime.now());

        assertEquals(0, handler.secondsUntilNextUse(43L, info));
        assertEquals(0, handler.secondsUntilNextUse(42L, warn));
        assertTrue(handler.secondsUntilNextUse(42L, info) >= 119);
    }

    @Test
    void expiredHistoryNoLongerCountsTowardQuickLimit() {
        var command = command("info", 120, 2);
        handler.trackUse(42L, "info", OffsetDateTime.now().minusMinutes(7));
        handler.trackUse(42L, "info", OffsetDateTime.now().minusMinutes(6));
        handler.trackUse(42L, "info", OffsetDateTime.now().minusMinutes(1));

        assertEquals(0, handler.secondsUntilNextUse(42L, command));
    }

    @Test
    void allowsUseAgainAfterDelayPasses() {
        var command = command("info", 120, 1);
        handler.trackUse(42L, "info", OffsetDateTime.now().minusMinutes(3));

        assertEquals(0, handler.secondsUntilNextUse(42L, command));
    }

    private SlashCommand command(String name, int delay, int quickLimit) {
        var command = mock(SlashCommand.class);
        when(command.getName()).thenReturn(name);
        when(command.getDelay()).thenReturn(delay);
        when(command.getQuickLimit()).thenReturn(quickLimit);
        return command;
    }
}
