package com.example.demo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostWithCommentsDto {

    private String postId;
    private String userId;
    private String title;
    private String content;
    private String username;
    private String imageUrl;
    private LocalDateTime createdAt;
    private List<CommentResponseDto> comments;

}
