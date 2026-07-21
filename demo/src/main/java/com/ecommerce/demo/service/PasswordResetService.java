package com.ecommerce.demo.service;

import com.ecommerce.demo.entity.PasswordResetToken;
import com.ecommerce.demo.entity.User;
import com.ecommerce.demo.repository.PasswordResetTokenRepository;
import com.ecommerce.demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class PasswordResetService {
    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public PasswordResetService(PasswordResetTokenRepository tokenRepository, EmailService emailService, PasswordEncoder passwordEncoder, UserRepository userRepository){
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }
    public String createResetToken(String email){
        User user = userRepository.findByEmail(email);
        if(user == null){
            throw  new RuntimeException("User not found");
        }
        tokenRepository.deleteByUser((user));
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setToken(UUID.randomUUID().toString());
        resetToken.setExpiry(
                Instant.now().plusSeconds(15*60)
        );
        tokenRepository.save(resetToken);
        return resetToken.getToken();
    }
    public void resetPassword(String token, String newPassword){
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid reset token"));
        if(resetToken.getExpiry().isBefore(Instant.now())){
            tokenRepository.delete(resetToken);
            throw new RuntimeException("Reset Token expired");
        }
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        String body =
                "Hello,\n\n" +
                        "Your password reset token is:\n" +
                        resetToken.getToken();
        emailService.sendEmail(
                user.getEmail(),
                "Password Reset",
                body
        );
        userRepository.save(user);
        tokenRepository.delete(resetToken);

    }
}
