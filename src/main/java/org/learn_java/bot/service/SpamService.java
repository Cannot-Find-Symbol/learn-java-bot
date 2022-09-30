package org.learn_java.bot.service;

import org.learn_java.bot.configuration.Mapper;
import org.learn_java.bot.data.dtos.SpamDTO;
import org.learn_java.bot.data.entities.Spam;
import org.learn_java.bot.data.repositories.SpamRepository;
import org.springframework.stereotype.Service;

@Service
public class SpamService {

    private final SpamRepository repository;
    private final Mapper mapper;

    public SpamService(SpamRepository repository, Mapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public SpamDTO findMessage(String message) {
        Spam spam = repository.findByMessage(message);
        return spam == null ? null : mapper.mapToSpamDTO(spam);
    }

    public SpamDTO save(Spam spam) {
        return mapper.mapToSpamDTO(repository.save(spam));
    }
}
