package org.learn_java.bot.data.repositories;

import java.util.List;
import org.learn_java.bot.data.entities.Warn;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarnRepository extends JpaRepository<Warn, Long> {
  List<Warn> findByUsername(String username);
}
