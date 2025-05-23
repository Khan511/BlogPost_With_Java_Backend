package com.example.demo.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequestDto {
    private String userId;
    private String postId;
    private String content;
    private Long parentId;

}
