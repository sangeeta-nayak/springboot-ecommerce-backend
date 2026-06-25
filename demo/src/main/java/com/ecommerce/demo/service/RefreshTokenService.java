package com.ecommerce.demo.service;

import com.ecommerce.demo.entity.RefreshToken;
import com.ecommerce.demo.entity.User;
import com.ecommerce.demo.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.UUID;

public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository){
        this.refreshTokenRepository = refreshTokenRepository;
    }
    @Transactional
    public RefreshToken createRefreshToken(User user){
        refreshTokenRepository.deleteByUser(user);
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant().now().plusSeconds(7*24*60*60));
        return refreshTokenRepository.save(refreshToken);
    }
    public RefreshToken verifyRefreshToken(String token){
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
        if(refreshToken.getExpiryDate().isBefore(Instant.now())){
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("refresh token expired")
        }
        return refreshToken;
    }
    @Transactional
    public void deleteByUser(User user){
        refreshTokenRepository.deleteByUser(user);
    }

}
