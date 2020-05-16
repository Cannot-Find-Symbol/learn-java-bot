package org.learn_java.data.repositories;

import org.learn_java.data.entities.Info;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InfoRepository extends JpaRepository<Info, String> {}
