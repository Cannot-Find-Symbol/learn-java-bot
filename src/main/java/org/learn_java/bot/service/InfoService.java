package org.learn_java.bot.service;

import org.learn_java.bot.configuration.Mapper;
import org.learn_java.bot.data.dtos.InfoDTO;
import org.learn_java.bot.data.entities.Info;
import org.learn_java.bot.data.repositories.InfoRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(value = "info.enabled", havingValue = "true", matchIfMissing = true)
public class InfoService {

    private final InfoRepository repository;
    private final Mapper mapper;

    public InfoService(InfoRepository repository, Mapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<InfoDTO> findAll(){
        return repository.findAll().stream()
                .map(mapper::mapToInfoDTO)
                .collect(Collectors.toList());
    }

    public Optional<Info> findById(String id){
        return repository.findById(id);
    }

    public InfoDTO save(Info info) {
        return mapper.mapToInfoDTO(repository.save(info));
    }

    public boolean existsById(String id){
        return repository.existsById(id);
    }

    public void deleteById(String id) {
        repository.deleteById(id);
    }
}
