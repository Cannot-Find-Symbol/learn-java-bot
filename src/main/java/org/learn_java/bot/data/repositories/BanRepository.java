package org.learn_java.bot.data.repositories;

import java.util.List;
import org.learn_java.bot.data.entities.Ban;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BanRepository extends JpaRepository<Ban, Long> {
  List<Ban> findByUserID(long userID);
}
