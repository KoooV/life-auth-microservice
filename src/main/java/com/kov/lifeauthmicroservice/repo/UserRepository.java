package com.kov.lifeauthmicroservice.repo;

import com.kov.lifeauthmicroservice.model.Role;
import com.kov.lifeauthmicroservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>{
    Optional<User> findByEmail(String email);
    String getHashedPasswordById(UUID id);
    Optional<User> findUserById(UUID id);
    Optional<User> findByName(String email);
}
