package com.example.demo.repo;

import java.util.List;
import java.util.Optional;
import com.example.demo.entities.PostEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<PostEntity, Long> {

    @Query("SELECT p FROM PostEntity p JOIN FETCH p.comments WHERE p.userEntity.id = :userId")
    List<PostEntity> findByUserEntityIdWithComments(@Param("userId") Long userId);

    Optional<PostEntity> findByPostId(String postId);

}
