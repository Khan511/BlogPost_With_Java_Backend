package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommentResponseDto {

    private Long id;
    private String userId;
    private String postId;
    private String username;
    private String content;
    private String imageUrl;
    private LocalDateTime createdAt;
    private Long parentId; // for replies
    private List<CommentResponseDto> replies; // Nested replies
    private int likeCount;
    private boolean likedByCurrentUser;

}
