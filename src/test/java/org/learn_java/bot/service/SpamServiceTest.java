package org.learn_java.bot.service;

import org.junit.jupiter.api.Test;
import org.learn_java.bot.configuration.Mapper;
import org.learn_java.bot.data.entities.Spam;
import org.learn_java.bot.data.repositories.SpamRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SpamServiceTest {
    private final SpamRepository repository = mock(SpamRepository.class);
    private final SpamService service = new SpamService(repository, new Mapper());

    @Test
    void findMessageMapsMatchAndReturnsNullWhenMissing() {
        var spam = new Spam("bad message");
        spam.setId(7L);
        when(repository.findByMessage("bad message")).thenReturn(spam);

        var result = service.findMessage("bad message");

        assertEquals(7L, result.getId());
        assertEquals("bad message", result.getMessage());
        assertNull(service.findMessage("clean message"));
    }

    @Test
    void saveMapsPersistedSpamRecord() {
        var spam = new Spam("bad message");
        spam.setId(7L);
        when(repository.save(spam)).thenReturn(spam);

        var result = service.save(spam);

        assertEquals(7L, result.getId());
        assertEquals("bad message", result.getMessage());
        verify(repository).save(spam);
    }
}
