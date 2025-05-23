package com.example.demo.entities;

import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;
import jakarta.persistence.Id;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.EntityListeners;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PostEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    String postId;
    private String title;
    private String content;
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userEntity_id")
    private UserEntity userEntity;

    @OneToMany(mappedBy = "postEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommentEntity> comments = new ArrayList<>();

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

}
