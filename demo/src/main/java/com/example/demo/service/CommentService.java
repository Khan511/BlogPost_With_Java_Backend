package com.example.demo.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.example.demo.dto.CommentResponseDto;
import com.example.demo.entities.CommentEntity;
import com.example.demo.entities.PostEntity;
import com.example.demo.entities.UserEntity;
import com.example.demo.mapper.UserMapper;
import com.example.demo.repo.CommentRepository;
import com.example.demo.repo.PostRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public CommentEntity createComment(String postId, UserEntity userEntity, CommentEntity commentEntity) {

        PostEntity postEntity = postRepository.findByPostId(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        commentEntity.setPostEntity(postEntity);
        commentEntity.setUserEntity(userEntity);

        return commentRepository.save(commentEntity);
    }

    public List<CommentEntity> getCommentsByUser(Long userId) {
        return commentRepository.findByUserEntityId(userId);
    }

    public void deleteCommnet(Long commentId) {
        commentRepository.deleteById(commentId);
    }

    // Adding or removing likes
    @Transactional
    public CommentResponseDto toggleLike(Long commentId, String userId) {

        CommentEntity commentEntity = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        boolean isLiked = commentEntity.toggleLike(userId);
        commentRepository.save(commentEntity);

        CommentResponseDto responseDto = UserMapper.toCommentDto(commentEntity, userId);
        responseDto.setLikedByCurrentUser(isLiked);
        responseDto.setLikeCount(commentEntity.getLikedUserIds().size());

        return responseDto;
    }

    @Transactional
    public String deleteComment(Long commentId) {
        CommentEntity commentToDelete = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found."));

        commentRepository.deleteById(commentToDelete.getId());
        return ("Comment deleted successfully");
    }

}
