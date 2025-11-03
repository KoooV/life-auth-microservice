package com.kov.lifeauthmicroservice.service;

import com.kov.lifeauthmicroservice.model.Role;
import com.kov.lifeauthmicroservice.model.User;
import com.kov.lifeauthmicroservice.repo.UserRepository;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Transactional
    public void register(@NonNull String username, @NonNull String password, @NonNull String email){
        logger.info("Trying to reg: {}" ,email);
        if(userRepository.findByEmail(email).isEmpty() || userRepository.findByName(username).isEmpty()) {
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setHashedPassword(passwordEncoder.encode(password));
            user.setEnabled(true);
            user.setRole(Role.USER);
            userRepository.save(user);
            logger.info("User successfully registered: {}" ,email);
        }
        else{
            logger.info("User already exist: {}" ,email);
            throw new IllegalArgumentException("User already exists");
        }
    }

    @Transactional
    public User findUserById(@NonNull UUID id){
        return userRepository.findById(id).orElseThrow(()-> new IllegalArgumentException("User not found"));
    }

    @Transactional
    public User findByEmail(@NonNull String email){
        return userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Transactional
    public void enableUser(@NonNull User user){
        user.setEnabled(true);
    }

    @Transactional
    public void lockeUser(@NonNull User user){
        user.setLocked(true);
    }

    @Transactional
    public void resetHashedPassword(@NonNull User user, @NonNull String oldPassword, @NonNull String newPassword){
        userRepository.findUserById(user.getId()).orElseThrow(()-> new IllegalArgumentException("User not found"));
        if(!passwordEncoder.matches(oldPassword, user.getHashedPassword())){
            throw new IllegalArgumentException("New password is incorrect");
        }
        user.setHashedPassword(passwordEncoder.encode(newPassword));
        logger.info("Password reset for user: {}" ,user.getId());
    }
}
