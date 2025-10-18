package com.kov.lifeauthmicroservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "password_reset_tokens", indexes = {
        @Index(name = "idx_password_reset_tokens_user_id", columnList = "user_id"),
        @Index(name = "idx_password_reset_tokens_token_hash", columnList = "token_hash"),
}
)
public class PasswordResetToken{ //для сброса пароля
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",  nullable = false)
    private User user;

    @Column(name = "created_at",  nullable = false)
    private Instant createdAt;

    @Column(name = "token_hash", nullable = false, unique = true, length = 128)//для отсечения атак с длинными строками и переполнения памяти
    private String tokenHash;

    @Column(name = "expire_at",  nullable = false)
    private Instant expireAt;

    @Column(name = "used", nullable = false)
    private boolean used = false;

    @PrePersist
    void onCreate(){
        if(createdAt == null){
            createdAt = Instant.now();
        }
    }
}
