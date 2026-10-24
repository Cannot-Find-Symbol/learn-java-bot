package org.learn_java.bot.service;

import org.junit.jupiter.api.Test;
import org.learn_java.bot.configuration.Mapper;
import org.learn_java.bot.data.entities.Warn;
import org.learn_java.bot.data.repositories.WarnRepository;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WarnServiceTest {
    private final WarnRepository repository = mock(WarnRepository.class);
    private final WarnService service = new WarnService(repository, new Mapper());

    @Test
    void findByUsernameMapsWarnings() {
        var date = LocalDate.of(2026, 1, 2);
        when(repository.findByUsername("member")).thenReturn(List.of(new Warn("member", "reason", date)));

        var result = service.findByUsername("member");

        assertEquals(1, result.size());
        assertEquals("member", result.getFirst().getUsername());
        assertEquals("reason", result.getFirst().getReason());
        assertEquals(date, result.getFirst().getDate());
    }

    @Test
    void findByUsernameReturnsEmptyListWhenNoWarnings() {
        when(repository.findByUsername("member")).thenReturn(List.of());

        assertTrue(service.findByUsername("member").isEmpty());
    }

    @Test
    void saveMapsPersistedWarning() {
        var warn = new Warn("member", "reason", LocalDate.of(2026, 1, 2));
        when(repository.save(warn)).thenReturn(warn);

        var result = service.save(warn);

        assertEquals("member", result.getUsername());
        assertEquals("reason", result.getReason());
        verify(repository).save(warn);
    }

    @Test
    void saveAllMapsEveryPersistedWarning() {
        var warnings = List.of(
                new Warn("first", "one", LocalDate.of(2026, 1, 2)),
                new Warn("second", "two", LocalDate.of(2026, 1, 3)));
        when(repository.saveAll(warnings)).thenReturn(warnings);

        var results = service.saveAll(warnings);

        assertEquals(List.of("first", "second"), results.stream().map(dto -> dto.getUsername()).toList());
        assertEquals(List.of("one", "two"), results.stream().map(dto -> dto.getReason()).toList());
    }
}
