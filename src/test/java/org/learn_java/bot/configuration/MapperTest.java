package org.learn_java.bot.configuration;

import org.junit.jupiter.api.Test;
import org.learn_java.bot.data.entities.Info;
import org.learn_java.bot.data.entities.Spam;
import org.learn_java.bot.data.entities.Warn;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class MapperTest {
    private final Mapper mapper = new Mapper();

    @Test
    void mapsSpamFields() {
        var spam = new Spam("message");
        spam.setId(12L);

        var result = mapper.mapToSpamDTO(spam);

        assertEquals(12L, result.getId());
        assertEquals("message", result.getMessage());
    }

    @Test
    void mapsWarnFieldsIncludingDate() {
        var date = LocalDate.of(2026, 10, 24);
        var result = mapper.mapToWarnDTO(new Warn("member", "reason", date));

        assertEquals("member", result.getUsername());
        assertEquals("reason", result.getReason());
        assertEquals(date, result.getDate());
    }

    @Test
    void mapsInfoFields() {
        var result = mapper.mapToInfoDTO(new Info("topic", "message", "description"));

        assertEquals("topic", result.getTopic());
        assertEquals("message", result.getMessage());
        assertEquals("description", result.getDescription());
    }
}
