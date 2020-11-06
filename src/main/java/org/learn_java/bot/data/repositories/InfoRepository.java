package org.learn_java.bot.data.repositories;

import org.learn_java.bot.data.entities.Info;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(value = "info.enabled", havingValue = "true", matchIfMissing = true)
public interface InfoRepository extends JpaRepository<Info, String> {}
