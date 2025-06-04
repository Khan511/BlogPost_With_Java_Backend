package com.example.demo.mapper;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;
import com.example.demo.dto.CommentResponseDto;
import com.example.demo.dto.LoginResponseDto;
import com.example.demo.dto.PostWithCommentsDto;
import com.example.demo.dto.UserDto;
import com.example.demo.entities.CommentEntity;
import com.example.demo.entities.PostEntity;
import com.example.demo.entities.RoleEntity;
import com.example.demo.entities.UserEntity;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserMapper {

        public static UserEntity toUserEntity(UserDto userDto) {
                return UserEntity.builder().userId(userDto.getUserId()).username(userDto.getUsername())
                                .email(userDto.getEmail()).password(userDto.getPassword()).build();
        }

        public static UserDto toUserDto(UserEntity userEntity) {
                return UserDto.builder().userId(userEntity.getUserId()).username(userEntity.getUsername())
                                .email(userEntity.getEmail()).password("")
                                .roleNames(userEntity.getRoles().stream().map(RoleEntity::getName)
                                                .collect(Collectors.toList()))
                                .imageUrl(userEntity.getImageUrl())
                                .mfa(userEntity.isMfa())
                                .qrCodeImageUri(userEntity.getQrCodeImageUri())
                                .qrCodeSecret(userEntity.getQrCodeSecret())
                                .build();
        }

        public static LoginResponseDto tLoginResponseDto(UserEntity userEntity) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
                String formattedLastloginDate = userEntity.getLastLogin() != null
                                ? userEntity.getLastLogin().format(formatter)
                                : null;

                return LoginResponseDto.builder()
                                .id(userEntity.getId())
                                .userId(userEntity.getUserId())
                                .email(userEntity.getEmail())
                                .username(userEntity.getUsername())
                                // .lastLogin(userEntity.getLastLogin())
                                .lastLogin(formattedLastloginDate)
                                .mfa(userEntity.isMfa())
                                .qrCodeSecret(userEntity.getQrCodeSecret())
                                .qrCodeImageUri(userEntity.getQrCodeImageUri())
                                .imageUrl(userEntity.getImageUrl())
                                .roles(userEntity.getRoles()
                                                .stream().map(RoleEntity::getName)
                                                .collect(Collectors.toList()))
                                .build();
        }

        public static PostWithCommentsDto toPostWithCommentsDto(PostEntity postEntity, String currentUserId) {
                return PostWithCommentsDto.builder()
                                .postId(postEntity.getPostId())
                                .title(postEntity.getTitle())
                                .content(postEntity.getContent())
                                .username(postEntity.getUserEntity().getUsername())
                                .imageUrl(postEntity.getImageUrl())
                                .comments(postEntity.getComments().stream()
                                                .filter(comment -> comment.getParent() == null)
                                                .sorted(Comparator.comparing(CommentEntity::getCreatedAt).reversed())
                                                .map((comment) -> toCommentDto(comment, currentUserId))
                                                .collect(Collectors.toList()))
                                .build();
        }

        public static CommentResponseDto toCommentDto(CommentEntity commentEntity, String currentUserId) {
                return CommentResponseDto.builder()
                                .id(commentEntity.getId())
                                .userId(commentEntity.getUserEntity().getUserId())
                                .postId(commentEntity.getPostEntity().getPostId())
                                .username(commentEntity.getUserEntity().getUsername())
                                .imageUrl(commentEntity.getUserEntity().getImageUrl())
                                .content(commentEntity.getContent())
                                .createdAt(commentEntity.getCreatedAt())
                                .likeCount(commentEntity.getLikeCount())
                                .likedByCurrentUser(commentEntity.getLikedUserIds().contains(currentUserId))
                                .parentId(commentEntity.getParent() != null ? commentEntity.getParent().getId() : null)
                                .replies(commentEntity.getReplies().stream()
                                                .map(comment -> toCommentDto(comment, currentUserId)).toList())
                                .build();
        }

        // PostDto without comments
        public static PostWithCommentsDto toPostDtoWithoutComments(PostEntity postEntity) {
                return PostWithCommentsDto.builder()
                                .postId(postEntity.getPostId())
                                .userId(postEntity.getUserEntity().getUserId())
                                .title(postEntity.getTitle())
                                .content(postEntity.getContent())
                                .username(postEntity.getUserEntity().getUsername())
                                .imageUrl(postEntity.getImageUrl())
                                .createdAt(postEntity.getCreatedAt())
                                // Explicitly set comments to empty list to avoid null
                                .comments(Collections.emptyList())
                                .build();
        }

}
