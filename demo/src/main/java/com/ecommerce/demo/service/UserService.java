package com.ecommerce.demo.service;

import com.ecommerce.demo.dto.UserDTO;
import com.ecommerce.demo.dto.UserResponseDTO;
import com.ecommerce.demo.entity.User;
import com.ecommerce.demo.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.beans.Encoder;
import java.util.List;

import static org.apache.logging.log4j.util.Strings.isBlank;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }
    @CacheEvict(value="users", allEntries = true)
    public User saveUser(UserDTO dto) {
        log.info("Saving user: {}", dto.getName());

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        if(dto.getRole()==null || dto.getRole().isBlank()){
            user.setRole("USER");
        }else {
            user.setRole(dto.getRole().toUpperCase());
        }
        user.setEnabled(false);

        return repo.save(user);
    }

    @Cacheable(value="users")
    public List<User> getAllUsers() {
        log.info("Fetching all users");
        return repo.findAll();
    }
    @CacheEvict(value="users", allEntries = true)
    public User updateUser(int id, User newUser) {
        log.info("Updating user with id: {}", id);

        User user = repo.findById(id).orElseThrow();
        user.setName(newUser.getName());
        user.setEmail(newUser.getEmail());
        if(newUser.getPassword() != null && !newUser.getPassword().isBlank()){
            user.setPassword(passwordEncoder.encode(newUser.getPassword()));
        }
        if(newUser.getRole() != null && !newUser.getRole().isBlank()){
            user.setRole(passwordEncoder.encode(newUser.getPassword()));
        }

        return repo.save(user);
    }
    @CacheEvict(value="users", allEntries = true)
    public String deleteUser(int id) {
        log.info("Deleting user with id: {}", id);

        repo.deleteById(id);
        return "User Deleted";
    }
    @Cacheable(value="users", key="#email")
    public UserResponseDTO getUserByEmail(String email) {
        log.info("Fetching user by email: {}", email);

        User user = repo.findByEmail(email);
        return mapToResponse(user);
    }

    private UserResponseDTO mapToResponse(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        return dto;
    }

    public Page<User> getUsersWithPagination(int page, int size, String field) {
        log.info("Fetching users with pagination");

        Pageable pageable = PageRequest.of(page, size, Sort.by(field));
        return repo.findAll(pageable);
    }

    public User getUserEntityByEmail(String email) {
        return repo.findByEmail(email);
    }
}