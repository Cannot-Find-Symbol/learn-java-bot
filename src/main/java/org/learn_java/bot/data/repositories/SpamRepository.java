package org.learn_java.bot.data.repositories;

import org.learn_java.bot.data.entities.Spam;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(value = "spam.enabled", havingValue = "true", matchIfMissing = true)
public interface SpamRepository extends JpaRepository<Spam, Long> {
  Spam findByMessage(String message);
}
