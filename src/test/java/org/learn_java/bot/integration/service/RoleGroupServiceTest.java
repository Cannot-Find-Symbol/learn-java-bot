package org.learn_java.bot.integration.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.learn_java.bot.data.entities.MemberRole;
import org.learn_java.bot.data.entities.RoleGroup;
import org.learn_java.bot.service.RoleGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest(showSql = false, properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(RoleGroupService.class)
class RoleGroupServiceTest extends MariaDbTestContainer {
    @Autowired EntityManager entityManager;
    @Autowired RoleGroupService service;

    @Test
    void savingGroupCascadesItsRoles() {
        var group = new RoleGroup();
        group.setName("language roles");
        group.setGuildId(42L);
        var role = new MemberRole();
        role.setId(7L);
        role.setDescription("Java");
        role.setOrdinal(1);
        role.setGroup(group);
        group.getRoles().add(role);

        var id = service.save(group).getId();
        entityManager.flush();
        entityManager.clear();

        var restored = service.findById(id);
        assertEquals("language roles", restored.getName());
        assertEquals(1, restored.getRoles().size());
        assertEquals(7L, restored.getRoles().getFirst().getId());
        assertEquals("Java", restored.getRoles().getFirst().getDescription());
    }
}
