package org.learn_java.bot.integration.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.learn_java.bot.data.entities.MemberInfo;
import org.learn_java.bot.data.repositories.MemberInfoRepository;
import org.learn_java.bot.service.MemberInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest(showSql = false, properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(MemberInfoService.class)
class MemberInfoServiceTest extends MariaDbTestContainer {
    @Autowired EntityManager entityManager;
    @Autowired MemberInfoRepository repository;
    @Autowired MemberInfoService service;

    @Test
    void thankingExistingMemberPersistsBothCounters() {
        repository.saveAndFlush(new MemberInfo(42L, 10, 2));
        entityManager.clear();

        service.updateThankCountForMember(42L);
        entityManager.flush();
        entityManager.clear();

        var restored = repository.findById(42L).orElseThrow();
        assertEquals(11, restored.getTotalThankCount());
        assertEquals(3, restored.getMonthThankCount());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void thankingNewMemberCreatesPersistentCounters() {
        try {
            service.updateThankCountForMember(43L);

            var restored = repository.findById(43L).orElseThrow();
            assertEquals(1, restored.getTotalThankCount());
            assertEquals(1, restored.getMonthThankCount());
        } finally {
            repository.deleteById(43L);
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void resettingMonthPersistsWithoutChangingAllTimeCounts() {
        repository.save(new MemberInfo(44L, 10, 3));
        repository.save(new MemberInfo(45L, 5, 2));
        try {
            service.resetForMonth();

            var first = repository.findById(44L).orElseThrow();
            var second = repository.findById(45L).orElseThrow();
            assertEquals(0, first.getMonthThankCount());
            assertEquals(10, first.getTotalThankCount());
            assertEquals(0, second.getMonthThankCount());
            assertEquals(5, second.getTotalThankCount());
        } finally {
            repository.deleteById(44L);
            repository.deleteById(45L);
        }
    }
}
