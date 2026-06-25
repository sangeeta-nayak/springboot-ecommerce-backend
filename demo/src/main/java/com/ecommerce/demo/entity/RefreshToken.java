package com.ecommerce.demo.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int Id;

    public String getToken() {
        return token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Instant getExpiryDate() {
        return ExpiryDate;
    }

    public void setExpiryDate(Instant expiryDate) {
        ExpiryDate = expiryDate;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    private String token;
    private Instant ExpiryDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}

