package com.kov.lifeauthmicroservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "password_reset_tokens")
//добавить индексы
public class PasswordResetToken{ //для сброса пароля
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id",  nullable = false)
    private User user;

    @Column(name = "created_at",  nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "token_hash", nullable = false, unique = true, length = 128)//для отсечения атак с длинными строками и переполнения памяти
    private String tokenHash;

    @Column(name = "expire_at",  nullable = false)
    private LocalDateTime expireAt;

    @Column(name = "used", nullable = false)
    private boolean used = false;

    @PrePersist
    void onCreate(){
        if(createdAt == null){
            createdAt = LocalDateTime.now();
        }
    }
}
