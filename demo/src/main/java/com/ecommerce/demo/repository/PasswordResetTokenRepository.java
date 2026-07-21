package com.ecommerce.demo.repository;

import com.ecommerce.demo.entity.PasswordResetToken;
import com.ecommerce.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {
    Optional<PasswordResetToken> findByToken (String token);
    void deleteByUser(User user);
}
