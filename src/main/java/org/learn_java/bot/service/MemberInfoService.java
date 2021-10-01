package org.learn_java.bot.service;

import org.learn_java.bot.data.entities.MemberInfo;
import org.learn_java.bot.data.repositories.MemberInfoRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

    public List<MemberInfo> findTop10ForMonth() {
        return repository.findAll().stream().sorted(Comparator.comparing(MemberInfo::getMonthThankCount)).limit(10).collect(Collectors.toList());
    }

    public void resetForMonth() {
        repository.findAll().forEach(member -> member.setMonthThankCount(0));
    }

    public List<MemberInfo> findTop10AllTime() {
        return repository.findAll().stream().sorted(Comparator.comparing(MemberInfo::getTotalThankCount)).limit(10).collect(Collectors.toList());
    }
}
