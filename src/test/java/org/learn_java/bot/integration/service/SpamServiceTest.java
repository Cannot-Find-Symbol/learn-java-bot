package org.learn_java.bot.integration.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.learn_java.bot.configuration.Mapper;
import org.learn_java.bot.data.entities.Spam;
import org.learn_java.bot.service.SpamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DataJpaTest(showSql = false, properties = "spring.jpa.hibernate.ddl-auto=validate")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({SpamService.class, Mapper.class})
class SpamServiceTest extends MariaDbTestContainer {
    @Autowired EntityManager entityManager;
    @Autowired SpamService service;

    @Test
    void findsPersistedSpamAndReturnsNullForMissingMessage() {
        service.save(new Spam("blocked text"));
        entityManager.flush();
        entityManager.clear();

        assertEquals("blocked text", service.findMessage("blocked text").getMessage());
        assertNull(service.findMessage("other text"));
    }
}
