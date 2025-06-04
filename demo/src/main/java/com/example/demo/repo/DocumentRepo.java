package com.example.demo.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entities.DocumentEntity;

public interface DocumentRepo extends JpaRepository<DocumentEntity, Long> {

    public List<DocumentEntity> findAllByCreatedBy(Long id);

}
