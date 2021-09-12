package org.learn_java.bot.data.repositories;

import org.learn_java.bot.data.entities.MemberInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberInfoRepository extends JpaRepository<MemberInfo, Long> {
}
