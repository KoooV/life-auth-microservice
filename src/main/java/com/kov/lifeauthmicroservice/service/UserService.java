package com.kov.lifeauthmicroservice.service;

import com.kov.lifeauthmicroservice.DTO.UserServiceDTO;
import com.kov.lifeauthmicroservice.exceptions.DuplicateUserException;
import com.kov.lifeauthmicroservice.exceptions.PasswordsDoesntMatchException;
import com.kov.lifeauthmicroservice.exceptions.UserNotFoundException;
import com.kov.lifeauthmicroservice.model.Role;
import com.kov.lifeauthmicroservice.model.User;
import com.kov.lifeauthmicroservice.repo.UserRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Validated
@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    private UserServiceDTO toDto(User user) {
        if (user == null) return null;
        return new UserServiceDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.isEnabled(),
                user.isLocked()
        );
    }

    @Transactional
    public UserServiceDTO createUser(@NotBlank String username, @NotBlank String password, @NotBlank @Email String email, Role role) {
        Objects.requireNonNull(username, "username must not be null");
        Objects.requireNonNull(password, "password must not be null");
        Objects.requireNonNull(email, "email must not be null");

        String normalizedEmail = emailNormalize(email);
        String normalizedUsername = username.trim();

        if (userRepository.findByName(normalizedUsername).isPresent() || userRepository.findByEmail(normalizedEmail).isPresent()) {
            log.warn("Duplicate user creation attempt: username ->{}", normalizedUsername);
            throw new DuplicateUserException("Duplicate user creation attempt");
        }

        User user = User.builder()
                .username(normalizedUsername)
                .email(normalizedEmail)
                .hashedPassword(passwordEncoder.encode(password))
                .enabled(false)
                .role(role)
                .build();

        try {
            User saved = userRepository.saveAndFlush(user);
            log.info("User successfully created: {}", normalizedUsername);
            return toDto(saved);
        } catch (DataIntegrityViolationException e) {
            log.warn("Duplicate user creation attempt (DB constraint): username ->{}", normalizedUsername, e);
            if (userRepository.findByName(normalizedUsername).isPresent()) {
                throw new DuplicateUserException("Username already exists");
            }
            if (userRepository.findByEmail(normalizedEmail).isPresent()) {
                throw new DuplicateUserException("Email already exists");
            }
            throw new DuplicateUserException("Duplicate user creation attempt");
        }
    }

    @Transactional(readOnly = true)
    public UserServiceDTO findByUsername(@NotBlank String username) {
        Objects.requireNonNull(username, "Username must not be null");
        User user = userRepository.findByName(username)
                .orElseThrow(() -> {
                    log.warn("Not found user with username {}", username);
                    return new UserNotFoundException("User not found");
                });
        return toDto(user);
    }

    @Transactional(readOnly = true)
    public UserServiceDTO findByEmail(@NotBlank @Email String email) {
        Objects.requireNonNull(email, "Email must not be null");
        String normalizedEmail = emailNormalize(email);
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> {
                    log.warn("Not found user with email {}", normalizedEmail);
                    return new UserNotFoundException("User not found");
                });
        return toDto(user);
    }

    public String emailNormalize(@NotBlank @Email String email) {
        Objects.requireNonNull(email, "Email must not be null");
        return email.trim().toLowerCase(Locale.ROOT);
    }

    @Transactional
    public void enableUser(@NotNull UUID id) {
        Objects.requireNonNull(id, "User id must not be null");
        User user = findEntityById(id);
        if (user.isEnabled()) {
            log.debug("User is already enabled: id ->{}", user.getId());
            return;
        }
        user.setEnabled(true);
        userRepository.save(user);
        log.info("User enabled: id ->{}", user.getId());
    }

    @Transactional
    public void lockUser(@NotNull UUID id) {
        Objects.requireNonNull(id, "User id must not be null");
        User user = findEntityById(id);
        if (user.isLocked()) {
            log.warn("User is already locked: id ->{}", user.getId());
            return;
        }
        user.setLocked(true);
        userRepository.save(user);
        log.info("User locked: id ->{}", user.getId());
    }

    @Transactional
    public void unlockUser(@NotNull UUID id) {
        Objects.requireNonNull(id, "User id must not be null");
        User user = findEntityById(id);
        if (!user.isLocked()) {
            log.warn("User is already unlocked: id ->{}", user.getId());
            return;
        }
        user.setLocked(false);
        userRepository.save(user);
        log.info("User unlocked: id ->{}", user.getId());
    }

    @Transactional(readOnly = true)
    public boolean verifyPassword(@NotNull UUID id, @NotBlank String oldPassword) {
        Objects.requireNonNull(oldPassword, "oldPassword must not be null");
        if (oldPassword.isBlank()) throw new IllegalArgumentException("oldPassword must not be blank");
        String hashedPassword = findEntityById(id).getHashedPassword();
        return passwordEncoder.matches(oldPassword, hashedPassword);
    }

    // Internal entity loader for internal operations that need the JPA entity
    private User findEntityById(@NotNull UUID id) {
        Objects.requireNonNull(id, "User id must not be null");
        return userRepository.findUserById(id)
                .orElseThrow(() -> {
                    log.warn("Not found user with id ->{}", id);
                    return new UserNotFoundException("User not found");
                });
    }

    @Transactional(readOnly = true)
    public UserServiceDTO findById(@NotNull UUID id) {
        User user = findEntityById(id);
        return toDto(user);
    }

    @Transactional
    public void resetHashedPassword(@NotNull UUID id, @NotBlank String newPassword, @NotBlank String oldPassword) {
        Objects.requireNonNull(newPassword, "newPassword must not be null");
        Objects.requireNonNull(oldPassword, "oldPassword must not be null");
        if (newPassword.isBlank()) throw new IllegalArgumentException("newPassword must not be blank");
        if (oldPassword.isBlank()) throw new IllegalArgumentException("oldPassword must not be blank");

        User currentUser = findEntityById(id);
        if (!verifyPassword(id, oldPassword)) {
            throw new PasswordsDoesntMatchException("Entered password does not match the oldPassword.");
        }

        currentUser.setHashedPassword(passwordEncoder.encode(newPassword));
        userRepository.save(currentUser);
        log.info("Password reset for user: {}", currentUser.getId());
    }
}
