package com.kov.lifeauthmicroservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "security_log", indexes = {
        @Index(name = "idx_security_log_user_id", columnList = "user_id"),
        @Index(name = "idx_security_log_event_type", columnList = "event_type"),
        @Index(name = "idx_security_log_created", columnList = "user_id, created_at")}
)
public class SecurityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id",  nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private SecurityEvent eventType;

    @NotBlank
    @Email//соответствие формату user@example.com
    @Size(min = 2, max = 39)//IPv4 7-15, IPv6 2-39 -> 2-39
    @Column(name = "ip", nullable = false)
    private String ipAddress;

    @Size(max = 512)
    @Column(name = "user_agent", nullable = false)
    private String userAgent;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist//актуальное время перед вставкой в бд
    void onCreate(){
        if(createdAt == null) {//для тестов(значение задано вручную) или миграции
            createdAt = Instant.now();
        }
    }
}
