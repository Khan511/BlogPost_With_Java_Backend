package com.example.demo.repo;

import java.util.List;
import java.util.Optional;
import com.example.demo.entities.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByName(String name);
    List<RoleEntity> findByNameIn(List<String> names);
}



