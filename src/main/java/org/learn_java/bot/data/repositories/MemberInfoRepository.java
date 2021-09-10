package org.learn_java.bot.data.repositories;

import org.learn_java.bot.data.entities.MemberInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberInfoRepository extends JpaRepository<MemberInfo, Long> {

}
