package com.example.demo.repo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.demo.entities.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH  u.posts WHERE u.email = :email")
    Optional<UserEntity> findByEmailWithPosts(@Param("email") String email);

    @Query("SELECT u FROM UserEntity u JOIN FETCH u.roles WHERE u.id = :id")
    Optional<UserEntity> findByIdWIthRoles(@Param("id") Long id);

    Optional<UserEntity> findByUsername(String name);

    Optional<UserEntity> findByEmail(String email);

    @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.roles WHERE u.userId = :userId")
    Optional<UserEntity> findByUserId(String userId);

    List<UserEntity> findByLastLoginBefore(LocalDateTime date);

    boolean existsByEmail(String email);
}
