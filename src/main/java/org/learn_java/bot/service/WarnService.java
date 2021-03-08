package org.learn_java.bot.service;

import org.learn_java.bot.configuration.Mapper;
import org.learn_java.bot.data.dtos.WarnDTO;
import org.learn_java.bot.data.entities.Warn;
import org.learn_java.bot.data.repositories.WarnRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WarnService {

    private WarnRepository repository;
    private Mapper mapper;

    public WarnService(WarnRepository repository, Mapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<WarnDTO> findByUsername(String username) {
        return repository.findByUsername(username).stream()
                .map(mapper::mapToWarnDTO)
                .collect(Collectors.toList());
    }

    public List<WarnDTO> saveAll(List<Warn> warns) {
        return repository.saveAll(warns).stream()
                .map(mapper::mapToWarnDTO)
                .collect(Collectors.toList());
    }

    public WarnDTO save(Warn warn) {
        return mapper.mapToWarnDTO(repository.save(warn));
    }
}
