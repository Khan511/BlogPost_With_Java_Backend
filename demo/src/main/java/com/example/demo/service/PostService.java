package com.example.demo.service;

import com.example.demo.entities.PostEntity;
import com.example.demo.entities.UserEntity;
import com.example.demo.mapper.UserMapper;
import com.example.demo.repo.CommentRepository;
import com.example.demo.repo.PostRepository;
import com.example.demo.repo.UserRepository;
import com.example.demo.security.CustomUserDetails;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.demo.dto.CommentResponseDto;
import com.example.demo.dto.PostRequestDto;
import com.example.demo.dto.PostWithCommentsDto;
import com.example.demo.entities.CommentEntity;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentService commentService;
    private final CommentRepository commentRepository;

    // public PostService(
    // PostRepository postRepository,
    // UserRepository userRepository,
    // CommentService commentService) {
    // this.postRepository = postRepository;
    // this.userRepository = userRepository;
    // this.commentService = commentService;
    // }

    // Create a post with user validation
    public PostWithCommentsDto createPost(PostRequestDto postDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = ((CustomUserDetails) authentication.getPrincipal()).getUserId();

        UserEntity user = userRepository.findByUserId(userId)

                .orElseThrow(() -> new RuntimeException("User not found"));

        // Business rule: Title cannot be blank
        if (postDto.getTitle() == null || postDto.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("postDto title is required");
        }

        PostEntity postEntity = new PostEntity();
        postEntity.setContent(postDto.getContent());
        postEntity.setTitle(postDto.getTitle());
        postEntity.setImageUrl(postDto.getImageUrl());
        postEntity.setPostId(UUID.randomUUID().toString());

        user.addPost(postEntity); // Bidirectional relationship update

        PostEntity savedPostEntity = postRepository.save(postEntity);
        return UserMapper.toPostDtoWithoutComments(savedPostEntity);

    }

    // Add a comment to a post (delegates to CommentService)
    public CommentResponseDto addCommentToPost(String postId, Long parentId, String userId, String content) {

        PostEntity post = postRepository.findByPostId(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        UserEntity user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CommentEntity comment = new CommentEntity();
        comment.setContent(content);
        comment.setPostEntity(post);
        comment.setUserEntity(user);

        if (parentId != null) {
            CommentEntity parent = commentRepository.findById(parentId)
                    .orElseThrow(() -> new EntityNotFoundException("Parent comment not found"));
            comment.setParent(parent);
        }

        CommentResponseDto commentResponseDto = UserMapper
                .toCommentDto(commentService.createComment(postId, user, comment), user.getUserId());

        commentResponseDto.setLikedByCurrentUser(comment.getLikedUserIds().contains(user.getUserId()));
        // return UserMapper.toCommentDto(commentService.createComment(postId, user,
        // comment));

        return commentResponseDto;
    }

    // Fetch a post with eager-loaded comments (avoid lazy loading issues)
    @Transactional(readOnly = true)
    public PostWithCommentsDto getPostWithComments(String postId) {
        CustomUserDetails user = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        String userId = user.getUserId();

        PostEntity post = postRepository.findByPostId(postId).orElseThrow(() -> new RuntimeException("Post not found"));

        return UserMapper.toPostWithCommentsDto(post, userId);

    }

    // This will return All post but without comments. Look in the return statement
    public List<PostWithCommentsDto> allPostsWithOutCommnets() {
        return postRepository.findAll().stream()
                .map(post -> UserMapper.toPostDtoWithoutComments(post)).collect(Collectors.toList());
    }

    // Delete a post and its comments (cascading)
    public void deletePost(String postId) {
        PostEntity post = postRepository.findByPostId(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        postRepository.delete(post); // Orphan removal deletes comments
    }
}