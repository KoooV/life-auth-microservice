package com.kov.lifeauthmicroservice.service;

import com.kov.lifeauthmicroservice.exceptions.DuplicateUserException;
import com.kov.lifeauthmicroservice.exceptions.UserNotFoundException;
import com.kov.lifeauthmicroservice.model.Role;
import com.kov.lifeauthmicroservice.model.User;
import com.kov.lifeauthmicroservice.repo.UserRepository;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {// Не знает про JWT
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Transactional
    public User createUser(@NonNull String username, @NonNull String password, @NonNull String email, @NonNull Role role){
        if(userRepository.findByName(username).isPresent() || userRepository.findByEmail(email).isPresent()) {
            log.warn("Duplicate user creation attempt: username ->{}, email ->{}",username, email);
            throw new DuplicateUserException("Duplicate user creation attempt");
        }
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setHashedPassword(passwordEncoder.encode(password));
            user.setEnabled(false);
            user.setRole(role);
            User saved = userRepository.save(user);
            log.info("User successfully created: {}" ,email);
            return saved;
    }

    @Transactional
    public User findByUsername(@NonNull String username){
        return userRepository.findByName(username)
                .orElseThrow(() -> {log.warn("Not found user with username {}", username);
                    return new UserNotFoundException("User not found");
                });
    }
    @Transactional
    public User findByEmail(@NonNull String email){
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {log.warn("Not found user with email {}", email);
                    return new UserNotFoundException("User not found");
                });
    }

    @Transactional
    public void enableUser(@NonNull User user){
        if(user.isEnabled()){
            log.debug("User is already enabled: id ->{}, email ->{}", user.getId(), user.getEmail());
            return;// нет смысла дважды включать, выход
        }
        user.setEnabled(true);
        userRepository.save(user);
        log.info("User enabled: id ->{}, email ->{}" ,user.getId(), user.getEmail());
    }

    @Transactional
    public void lockUser(@NonNull User user){
        if(user.isLocked()){
            log.debug("User is already locked: id ->{}, email ->{}", user.getId(), user.getEmail());
            return;// нет смысла дважды блокировать, выход
        }
        user.setLocked(true);
        userRepository.save(user);
        log.info("User locked: id ->{}, email ->{}" ,user.getId(), user.getEmail());
    }

    public void unlockUser(@NonNull User user){
        if(!user.isLocked()){
            log.debug("User is already unlocked: id ->{}, email ->{}", user.getId(), user.getEmail());
            return;// нет смысла дважды разблокировать, выход
        }
        user.setLocked(false);
        userRepository.save(user);
        log.info("User unlocked: id ->{}, email ->{}" ,user.getId(), user.getEmail());
    }

    public boolean verifyPassword(@NonNull User user, @NonNull String newPassword){// newPassword -> пароль в сыром виде
        return passwordEncoder.matches(newPassword, user.getHashedPassword());

    }

    @Transactional
    public User findById(@NonNull UUID id){
        return userRepository.findUserById(id)
                .orElseThrow(()-> {log.warn("Not found user with id ->{}", id);
                    return new UserNotFoundException("User not found");
                });
    }

    @Transactional
    public void resetHashedPassword(@NonNull User user, @NonNull String newPassword){
        User currentUser = findById(user.getId());
        if(!verifyPassword(user, newPassword)){
            throw new IllegalArgumentException("New password is incorrect");
        }

        currentUser.setHashedPassword(passwordEncoder.encode(newPassword));
        userRepository.save(currentUser);
        log.info("Password reset for user: {}", currentUser.getId());
    }
}
