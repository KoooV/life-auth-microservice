package com.kov.lifeauthmicroservice.service;

import com.kov.lifeauthmicroservice.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;



}
