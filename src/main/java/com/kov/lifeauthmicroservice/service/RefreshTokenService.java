package com.kov.lifeauthmicroservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RefreshTokenService {

    public void revokeToken(String token){
        // TODO: mark refresh token as revoked in repository
    }

}

