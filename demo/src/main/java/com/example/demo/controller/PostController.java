package com.example.demo.controller;

import com.example.demo.dto.PostRequestDto;
import lombok.RequiredArgsConstructor;
import com.example.demo.service.PostService;
import com.example.demo.dto.PostWithCommentsDto;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/blog")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping("/createPost")
    public PostWithCommentsDto createPost(@RequestBody PostRequestDto postDto) {
        return postService.createPost(postDto);
    }

    @GetMapping("/allPostst")
    public ResponseEntity<List<PostWithCommentsDto>> getAllPosts() {
        List<PostWithCommentsDto> posts = postService.allPostsWithOutCommnets();

        return ResponseEntity.ok(posts);
    }

    @GetMapping("/postWithComments/{postId}")
    public PostWithCommentsDto getPostWithCom(@PathVariable String postId) {
        return postService.getPostWithComments(postId);
    }

    @DeleteMapping("/delete-post/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable String postId) {
        postService.deletePost(postId);

        return ResponseEntity.ok().build();
    }

}
