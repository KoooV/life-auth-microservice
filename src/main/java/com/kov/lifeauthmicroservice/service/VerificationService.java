package com.kov.lifeauthmicroservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class VerificationService {

    public void createAndSendVerification(String email){
        // TODO: create VerificationToken entity, save and send email
    }

    public void verifyToken(String token){
        // TODO: lookup token, check expiry, enable user
    }

    public void createAndSendPasswordReset(String email){
        // TODO: create PasswordResetToken and send
    }

    public void resetPassword(String token, String newPassword){
        // TODO: validate token and set new password
    }
}

