package com.example.demo.repo;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.entities.ConfirmationEntity;

public interface ConfirmationRepo extends JpaRepository<ConfirmationEntity, Long> {

    Optional<ConfirmationEntity> findByKey(String key);
}
