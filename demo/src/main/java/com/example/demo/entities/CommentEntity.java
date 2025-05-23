
package com.example.demo.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class CommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String content;

    @CreatedDate
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "postEntity_id")
    private PostEntity postEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userEntity_id")
    private UserEntity userEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    @JsonBackReference // Prevents infinite recursion in JSON serialization
    private CommentEntity parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference // Manges serialization of repliese
    private List<CommentEntity> replies = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "comment_likes", joinColumns = @JoinColumn(name = "comment_id"))
    @Column(name = "user_id")
    private Set<String> likedUserIds = new HashSet<>();

    public boolean toggleLike(String userId) {
        if (likedUserIds.contains(userId)) {
            likedUserIds.remove(userId);
            return false;
        } else {
            likedUserIds.add(userId);
            return true;
        }
    }

    public int getLikeCount() {
        return likedUserIds.size();
    }
}