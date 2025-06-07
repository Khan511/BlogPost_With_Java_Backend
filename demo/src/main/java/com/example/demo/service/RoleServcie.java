package com.example.demo.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.example.demo.entities.RoleEntity;
import com.example.demo.repo.RoleRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleServcie {

    private final RoleRepository roleRepository;

    public List<RoleEntity> getAllRolles() {
        return roleRepository.findAll();
    }

}
