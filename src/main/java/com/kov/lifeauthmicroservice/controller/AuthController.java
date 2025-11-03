package com.kov.lifeauthmicroservice.controller;

import com.kov.lifeauthmicroservice.DTO.LoginRequest;
import com.kov.lifeauthmicroservice.DTO.MessageResponce;
import com.kov.lifeauthmicroservice.DTO.RefreshTokenRequest;
import com.kov.lifeauthmicroservice.DTO.RegisterRequest;
import com.kov.lifeauthmicroservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@RestController
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<MessageResponce> register(@ResponseBody RegisterRequest request){
        return ResponseEntity.ok();
    }

    @PostMapping("/login")
    public ResponseEntity<MessageResponce> login(@ResponseBody LoginRequest request){
        return ResponseEntity.ok();
    }

    @PostMapping("/refresh")
    public ResponseEntity<MessageResponce> refreshToken(@ResponseBody RefreshTokenRequest request){
        return
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponce> logout(@ResponseBody RegisterRequest request){
        return
    }
}

