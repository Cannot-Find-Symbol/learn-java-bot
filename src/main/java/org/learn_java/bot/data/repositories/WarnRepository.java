package org.learn_java.bot.data.repositories;

import java.util.List;
import org.learn_java.bot.data.entities.Warn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WarnRepository extends JpaRepository<Warn, Long> {
  List<Warn> findByUserID(long userID);
}
