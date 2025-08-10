package com.listo.eskalink.user.repository;

import com.listo.eskalink.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByVerificationToken(String verificationToken);

    boolean existsByEmail(String email);

    void deleteByVerificationTokenExpiresAtBeforeAndIsVerifiedFalse(LocalDateTime expiredTime);
}
