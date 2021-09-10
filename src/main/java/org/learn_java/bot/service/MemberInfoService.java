package org.learn_java.bot.service;

import org.learn_java.bot.data.entities.MemberInfo;
import org.learn_java.bot.data.repositories.MemberInfoRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
public class MemberInfoService {

    private MemberInfoRepository repository;

    public MemberInfoService(MemberInfoRepository repository) {
        this.repository = repository;
    }

    public MemberInfo updateThankCountForMember(Long id) {
        MemberInfo info = repository.findById(id).orElse(null);
        if (info == null) {
            info = repository.save(new MemberInfo(id, 0, 0));
        }

        info.setMonthThankCount(info.getMonthThankCount() + 1);
        info.setTotalThankCount(info.getTotalThankCount() + 1);
        return info;
    }
}
