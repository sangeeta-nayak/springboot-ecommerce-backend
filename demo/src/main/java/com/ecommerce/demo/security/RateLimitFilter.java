package com.ecommerce.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

public class RateLimitFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redisTemplate;
    private static final int MAX_REQUESTS = 5;
    private static final int TIME_WINDOW = 60;

    public RateLimitFilter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // bypass the public endpoints
        if (path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/h2-console") ||
                path.startsWith("/actuator") ||
                path.startsWith("/users/login") ||
                path.startsWith("/users/refresh")) {

            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = request.getRemoteAddr();
        String key = "rate_limit:" + clientIp;

        String currentCount = redisTemplate.opsForValue().get(key);

        if (currentCount == null) {
            redisTemplate.opsForValue()
                    .set(key, "1", Duration.ofSeconds(TIME_WINDOW));
        } else {
            int count = Integer.parseInt(currentCount);

            if (count >= MAX_REQUESTS) {
                response.setStatus(429);
                response.getWriter().write("Too many requests, try again.");
                return;
            }

            redisTemplate.opsForValue().increment(key);
        }

        filterChain.doFilter(request, response);
    }
}