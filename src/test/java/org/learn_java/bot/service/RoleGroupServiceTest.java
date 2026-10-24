package org.learn_java.bot.service;

import org.junit.jupiter.api.Test;
import org.learn_java.bot.data.entities.RoleGroup;
import org.learn_java.bot.data.repositories.RoleGroupRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoleGroupServiceTest {
    private final RoleGroupRepository repository = mock(RoleGroupRepository.class);
    private final RoleGroupService service = new RoleGroupService(repository);

    @Test
    void findAllReturnsRepositoryResults() {
        var groups = List.of(new RoleGroup(), new RoleGroup());
        when(repository.findAll()).thenReturn(groups);

        assertSame(groups, service.findAll());
    }

    @Test
    void findByIdReturnsGroupOrNullWhenMissing() {
        var group = new RoleGroup();
        when(repository.findById(4L)).thenReturn(Optional.of(group));
        when(repository.findById(5L)).thenReturn(Optional.empty());

        assertSame(group, service.findById(4L));
        assertNull(service.findById(5L));
    }

    @Test
    void saveReturnsPersistedGroup() {
        var group = new RoleGroup();
        when(repository.save(group)).thenReturn(group);

        assertSame(group, service.save(group));
        verify(repository).save(group);
    }
}
