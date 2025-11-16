package com.kov.lifeauthmicroservice.service;

import com.kov.lifeauthmicroservice.DTO.MessageResponse;
import com.kov.lifeauthmicroservice.DTO.RegisterRequest;
import com.kov.lifeauthmicroservice.exceptions.UserNotRegisterException;
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

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserService userService;

    @Transactional
    public MessageResponse register(@NonNull RegisterRequest request){
        try{
            userService.createUser(
                    request.username(),
                    request.password(),
                    request.email(),
                    Role.USER
            );
            log.info("User successfully registered: {}" ,request.email());
            return new MessageResponse("success reg");
        }catch (Exception e){
            log.warn("Failed to register user with email ->{}", request.email());
            throw new UserNotRegisterException("Failed to register user: " + e);
        }
    }

    @Transactional
    public MessageResponse login(String username, String password){}




}
