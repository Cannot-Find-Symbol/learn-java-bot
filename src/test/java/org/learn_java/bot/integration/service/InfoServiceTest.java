package org.learn_java.bot.integration.service;

import org.junit.jupiter.api.Test;
import org.learn_java.bot.configuration.Mapper;
import org.learn_java.bot.data.entities.Info;
import org.learn_java.bot.service.InfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(showSql = false, properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({InfoService.class, Mapper.class})
class InfoServiceTest extends MariaDbTestContainer {
    @Autowired InfoService service;

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void savesListsAndDeletesTopicWithFullLengthMessage() {
        String message = "x".repeat(2000);
        try {
            var saved = service.save(new Info("java", message, "Java help"));
            assertEquals("java", saved.getTopic());
            assertEquals(message, saved.getMessage());

            assertTrue(service.existsById("java"));
            assertEquals(message, service.findById("java").orElseThrow().getMessage());
            var topics = service.findAll();
            assertEquals(1, topics.size());
            assertEquals("Java help", topics.getFirst().getDescription());

            service.deleteById("java");

            assertFalse(service.existsById("java"));
            assertTrue(service.findById("java").isEmpty());
        } finally {
            if (service.existsById("java")) {
                service.deleteById("java");
            }
        }
    }
}
