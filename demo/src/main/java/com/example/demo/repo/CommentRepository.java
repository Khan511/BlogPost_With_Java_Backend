package com.example.demo.repo;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entities.CommentEntity;
import com.example.demo.entities.PostEntity;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    // Find comments by user ID (directly via the user relationship)
    @Query("SELECT c FROM CommentEntity c WHERE c.userEntity.id = :userId")
    List<CommentEntity> findByUserEntityId(@Param("userId") Long userId);

    // Find comments containing a specific keyword (case-insensitive)
    @Query("SELECT c FROM CommentEntity c WHERE LOWER(c.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<CommentEntity> searchByKeyword(@Param("keyword") String keyword);

    // Delete comment by post ID
    @Modifying
    @Query("DELETE FROM CommentEntity c WHERE c.postEntity.id = :postId")
    void deleteByPostEntityId(@Param("postId") Long postId);

    @Query("SELECT c.likedUserIds FROM CommentEntity c WHERE c.id = :commentId")
    Set<String> findLikedUserIdsByCommentId(@Param("commentId") Long commentId);

    @EntityGraph(attributePaths = { "replies", "userEntity" })
    List<CommentEntity> findByPostEntityAndParentIsNull(PostEntity postEntity);

}
