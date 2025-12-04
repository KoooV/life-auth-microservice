package com.kov.lifeauthmicroservice.service;

import com.kov.lifeauthmicroservice.DTO.*;
import com.kov.lifeauthmicroservice.JWT.JwtUtil;
import com.kov.lifeauthmicroservice.exceptions.UserNotRegisterException;
import com.kov.lifeauthmicroservice.model.Role;
import com.kov.lifeauthmicroservice.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserService userService;

    private final VerificationService verificationService;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;

    @Transactional
    public MessageResponse register(RegisterRequest request){
        try{
            userService.createUser(request.username(), request.password(), request.email(), Role.USER);
            verificationService.createAndSendVerification(request.email());
            log.info("User successfully registered: {}", request.email());
            return new MessageResponse("Success reg");
        }catch (Exception e){
            log.warn("Failed to register user with email ->{}", request.email());
            throw new UserNotRegisterException("Failed to register user: " + e);
        }
    }

    @Transactional
    public AuthResponse login(LoginRequest request){

    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request){

    }

    @Transactional
    public MessageResponse logout(RefreshTokenRequest request){

    }

    @Transactional
    public MessageResponse verifyRegistration(String token){

    }

    @Transactional
    public MessageResponse requestPasswordReset(String email){

    }

    @Transactional
    public MessageResponse resetPassword(String token, String newPassword){

    }
}
