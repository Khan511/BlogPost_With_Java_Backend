package com.example.demo.entities;

import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.util.AlternativeJdkIdGenerator;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "documents")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DocumentEntity {
    @Id
    @SequenceGenerator(name = "primary_key_seq", sequenceName = "primary_key_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "primary_key_seq")
    @Column(name = "id", updatable = false)
    private Long id;

    @Builder.Default()
    private String referenceId = new AlternativeJdkIdGenerator().generateId().toString();

    @Column(name = "document_id", updatable = false, unique = true, nullable = false)
    private String documentId;
    private String name;
    private String description;
    private String uri;
    private Long size;
    private String formattedSize;
    private String icon;
    private String extension;

    @NotNull
    private Long createdBy;
    @NotNull
    private Long updatedBy;

    @NotNull
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreatedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_documents_owner"))
    private UserEntity owner;

    @PrePersist
    public void beforePersist() {
        setCreatedAt(LocalDateTime.now());
        setCreatedBy(owner.getId());
        setUpdatedAt(LocalDateTime.now());
        setUpdatedBy(owner.getId());
    }

    @PreUpdate
    public void beforeUpdate() {
        setUpdatedAt(LocalDateTime.now());
        setUpdatedBy(owner.getId());
    }

}
