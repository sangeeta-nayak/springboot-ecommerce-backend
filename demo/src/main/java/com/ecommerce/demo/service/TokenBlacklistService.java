package com.ecommerce.demo.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class TokenBlacklistService {
    private final StringRedisTemplate redisTemplate;

    public TokenBlacklistService(StringRedisTemplate redisTemplate){
        this.redisTemplate = redisTemplate;
    }
    public void blacklistToken(String token, long expirationMillis){
        redisTemplate.opsForValue().set(
                "blacklistedToken: " + token,
                "logged_out",
                 Duration.ofMillis(expirationMillis)
        );
    }
    public boolean isTokenBlacklisted(String token){
        return redisTemplate.hasKey("blackList" + token);

    }
}
