package com.ecommerce.demo.entity;

import io.lettuce.core.dynamic.annotation.CommandNaming;
import jakarta.persistence.*;
import org.springframework.boot.autoconfigure.web.WebProperties;

import java.time.Instant;

@Entity
public class PasswordResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String token;
    private Instant expiry;
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    public int getId() {
        return id;
    }

    public Instant getExpiry() {
        return expiry;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setExpiry(Instant expiry) {
        this.expiry = expiry;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setId(int id) {
        this.id = id;
    }
}
