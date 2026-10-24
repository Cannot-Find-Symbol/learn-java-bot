package org.learn_java.bot.service;

import org.junit.jupiter.api.Test;
import org.learn_java.bot.configuration.Mapper;
import org.learn_java.bot.data.entities.Info;
import org.learn_java.bot.data.repositories.InfoRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InfoServiceTest {
    private final InfoRepository repository = mock(InfoRepository.class);
    private final InfoService service = new InfoService(repository, new Mapper());

    @Test
    void findAllMapsEveryRepositoryEntry() {
        when(repository.findAll()).thenReturn(List.of(
                new Info("java", "Java info", "About Java"),
                new Info("help", "Help text", "Help")));

        var results = service.findAll();

        assertEquals(2, results.size());
        assertEquals("java", results.getFirst().getTopic());
        assertEquals("Java info", results.getFirst().getMessage());
        assertEquals("About Java", results.getFirst().getDescription());
        assertEquals("help", results.get(1).getTopic());
    }

    @Test
    void findByIdReturnsRepositoryOptional() {
        var info = new Info("java", "Java info", "About Java");
        when(repository.findById("java")).thenReturn(Optional.of(info));
        when(repository.findById("missing")).thenReturn(Optional.empty());

        assertSame(info, service.findById("java").orElseThrow());
        assertTrue(service.findById("missing").isEmpty());
    }

    @Test
    void savePersistsAndMapsTheSavedEntity() {
        var info = new Info("java", "Java info", "About Java");
        when(repository.save(info)).thenReturn(info);

        var result = service.save(info);

        assertEquals("java", result.getTopic());
        assertEquals("Java info", result.getMessage());
        assertEquals("About Java", result.getDescription());
        verify(repository).save(info);
    }

    @Test
    void existsAndDeleteDelegateToRepository() {
        when(repository.existsById("java")).thenReturn(true);

        assertTrue(service.existsById("java"));
        assertFalse(service.existsById("missing"));
        service.deleteById("java");

        verify(repository).deleteById("java");
    }
}
