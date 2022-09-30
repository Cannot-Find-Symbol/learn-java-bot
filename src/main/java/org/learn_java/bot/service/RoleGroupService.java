package org.learn_java.bot.service;

import org.learn_java.bot.data.entities.RoleGroup;
import org.learn_java.bot.data.repositories.RoleGroupRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleGroupService {

    private final RoleGroupRepository repository;

    public RoleGroupService(RoleGroupRepository repository) {
        this.repository = repository;
    }

    public List<RoleGroup> findAll() {
        return repository.findAll();
    }

    public RoleGroup findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public RoleGroup save(RoleGroup group) {
        return repository.save(group);
    }
}
