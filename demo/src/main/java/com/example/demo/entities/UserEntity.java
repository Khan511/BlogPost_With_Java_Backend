package com.example.demo.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import java.util.List;
import java.util.ArrayList;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Cacheable;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.CascadeType;
import org.hibernate.annotations.Cache;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.EntityListeners;
import org.springframework.data.annotation.CreatedDate;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Getter
@Setter
@Builder
@Cacheable
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Cache(region = "userCache", usage = CacheConcurrencyStrategy.READ_WRITE)
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String userId;
    @Column(unique = true)
    private String username;
    @Column(unique = true)
    private String email;
    private String password;
    private String imageUrl;
    private Integer loginAttempts;

    @Column(name = "mfa")
    private boolean mfa = false;

    @JsonIgnore
    @Column(columnDefinition = "text")
    private String qrCodeSecret;
    @Column(columnDefinition = "text")
    private String qrCodeImageUri;

    @Column(name = "enabled")
    private boolean enabled;
    @Column(name = "account_non_locked")
    private boolean accountNonLocked = true;

    @Column(name = "account_non_expired")
    private boolean accountNonExpired = true;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @OneToMany(mappedBy = "userEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostEntity> posts = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "userEntity_id"), inverseJoinColumns = @JoinColumn(name = "roleEntity_id"))
    private List<RoleEntity> roles = new ArrayList<>();

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Helpers to manage relationships
    public void addPost(PostEntity postEntity) {
        posts.add(postEntity);
        postEntity.setUserEntity(this);
    }

}
