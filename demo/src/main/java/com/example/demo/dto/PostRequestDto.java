package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostRequestDto {
    private Long userId;
    private String postId;
    private String title;
    private String content;
    private String username;
    private String imageUrl;
    // private List<CommentDto> comments;
}
