package com.ecommerce.demo.entity;

import jakarta.persistence.*;

@Entity
public class Orders {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    public int getId() {
        return id;
    }

    public String getProduct() {
        return product;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public void setId(int id) {
        this.id = id;
    }

    private String product;
    private double price;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
