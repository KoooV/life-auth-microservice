package com.kov.lifeauthmicroservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)//переопределить e&hc только для полей с аннотацией @EqualsAndHashCode.Include
@Table(name = "users", indexes = {
        @Index(name = "idx_user_username", columnList = "username"),
        @Index(name = "idx_user_role", columnList = "role"),
        @Index(name = "idx_user_email", columnList = "email")
})
public class User {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)//новый тип TIME v7
    @GeneratedValue(strategy = GenerationType.AUTO)
    @EqualsAndHashCode.Include//сравниваем сущности только по id, BEST PRACTICE
    @Column(updatable = false, nullable = false)//запрет на UPDATE поля при sql запросе
    private UUID id;

    @NotBlank
    @Size(min = 3, max = 32)
    @Column(name = "username",unique = true, nullable = false, length = 50)
    private String username;

    @NotBlank
    @Size(max = 255)//максимум для email 254, стандарт 255
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @NotBlank
    @Size(min = 60, max = 255)//60, тк минимум для BCrypt хэша
    @Column(name = "hashed_password", nullable = false)
    private String hashedPassword;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = false;//аккаунт активен/неактивен (изменение при создании аккаунта или блокировке)

    @Column(name = "locked", nullable = false)
    private boolean locked = false;//наличие блокировки

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,//удаление всех токенов вместе с пользователем
            fetch = FetchType.LAZY,
            orphanRemoval = true//удаление токена из коллекции -> удаление из бд
    )
    private List<RefreshToken> refreshTokens = new ArrayList<>();

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    private List<VerificationToken> verificationTokens = new ArrayList<>();

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<PasswordResetToken> passwordResetTokens = new ArrayList<>();
}
