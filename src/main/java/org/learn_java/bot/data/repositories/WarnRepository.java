package org.learn_java.bot.data.repositories;

import org.learn_java.bot.data.entities.Warn;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@ConditionalOnProperty(value = "warn.enabled", havingValue = "true", matchIfMissing = true)
public interface WarnRepository extends JpaRepository<Warn, Long> {
    List<Warn> findByUsername(String username);
}
