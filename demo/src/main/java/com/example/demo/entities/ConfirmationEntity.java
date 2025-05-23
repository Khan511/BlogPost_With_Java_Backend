package com.example.demo.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import java.util.UUID;
import lombok.ToString;
import jakarta.persistence.Id;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.GenerationType;
import org.hibernate.annotations.OnDelete;
import jakarta.persistence.GeneratedValue;
import org.hibernate.annotations.OnDeleteAction;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.JsonIdentityReference;

@Getter
@Setter
@Entity
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "confirmations")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class ConfirmationEntity {
    private String key;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(targetEntity = UserEntity.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    @JsonProperty("user_id")
    private UserEntity userEntity;

    public ConfirmationEntity(UserEntity userEntity) {
        this.userEntity = userEntity;
        this.key = UUID.randomUUID().toString();
    }

}
