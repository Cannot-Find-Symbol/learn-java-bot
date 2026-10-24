package org.learn_java.bot.integration.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.learn_java.bot.configuration.Mapper;
import org.learn_java.bot.data.dtos.WarnDTO;
import org.learn_java.bot.data.entities.Warn;
import org.learn_java.bot.service.WarnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(showSql = false, properties = "spring.jpa.hibernate.ddl-auto=validate")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({WarnService.class, Mapper.class})
class WarnServiceTest extends MariaDbTestContainer {
    @Autowired EntityManager entityManager;
    @Autowired WarnService service;

    @Test
    void findsOnlyPersistedWarningsForTheRequestedUser() {
        service.saveAll(List.of(
                new Warn("alice", "first", LocalDate.of(2026, 1, 2)),
                new Warn("bob", "other", LocalDate.of(2026, 1, 3)),
                new Warn("alice", "second", LocalDate.of(2026, 1, 4))));
        entityManager.flush();
        entityManager.clear();

        var results = service.findByUsername("alice");

        assertEquals(Set.of("first", "second"),
                results.stream().map(WarnDTO::getReason).collect(Collectors.toSet()));
        assertTrue(service.findByUsername("missing").isEmpty());
    }
}
