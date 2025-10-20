package com.kov.lifeauthmicroservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "verification_tokens", indexes = {
        @Index(name = "idx_verification_tokens_user_id", columnList = "user_id"),
        @Index(name = "idx_verification_tokens_user_created", columnList = "user_id, created_at")
})
public class VerificationToken{ //активация пользователя после регистрации

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token_hash",nullable = false, unique = true, length = 128)
    private String tokenHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expire_at", nullable = false)
    private Instant expiresAt;//токен не должен существовать долгое время

    @Column(name = "used", nullable = false)
    private boolean used = false;//после активации пользователя токен должен быть деактивирован

    @PrePersist
    void onCreate(){
        if(createdAt == null){
            createdAt = Instant.now();
        }
    }
}
