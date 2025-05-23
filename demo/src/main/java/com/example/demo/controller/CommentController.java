package com.example.demo.controller;

import lombok.RequiredArgsConstructor;
import com.example.demo.dto.CommentResponseDto;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.CommentService;
import com.example.demo.service.PostService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/blog")
public class CommentController {
    private final PostService postService;
    private final CommentService commentService;

    @PostMapping("/createComment")
    public CommentResponseDto createComment(@RequestBody CommentRequestDto commentRequestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String userId = userDetails.getUserId();
        Long parentId = commentRequestDto.getParentId();
        return postService.addCommentToPost(commentRequestDto.getPostId(), parentId, userId,
                commentRequestDto.getContent());
    }

    @PutMapping("/comments/{commentId}/incrementLike")
    public ResponseEntity<CommentResponseDto> toggleThelike(@PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        CommentResponseDto requestDto = commentService.toggleLike(commentId, userDetails.getUserId());

        return ResponseEntity.ok(requestDto);
    }

    @DeleteMapping("/delete-comment/{commentId}")
    public String deleteComment(@PathVariable Long commentId) {
        return commentService.deleteComment(commentId);
    }

}
