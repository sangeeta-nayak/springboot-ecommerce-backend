package com.ecommerce.demo.controller;

import com.ecommerce.demo.dto.*;
import com.ecommerce.demo.entity.RefreshToken;
import com.ecommerce.demo.entity.User;
import com.ecommerce.demo.exception.InvalidCredentialsException;
import com.ecommerce.demo.security.JwtUtil;
import com.ecommerce.demo.service.PasswordResetService;
import com.ecommerce.demo.service.RefreshTokenService;
import com.ecommerce.demo.service.TokenBlacklistService;
import com.ecommerce.demo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/users")
public class UserController {
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserService service;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetService passwordResetService;
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    public UserController(UserService service, PasswordResetService passwordResetService, RefreshTokenService refreshTokenService, TokenBlacklistService tokenBlacklistService, JwtUtil jwtUtil, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder){
        this.service = service;
        this.refreshTokenService = refreshTokenService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetService = passwordResetService;
    }
    @PostMapping("/logout")
    public Map<String, String> logout(HttpServletRequest servletRequest){
        String authHeader = servletRequest.getHeader("Authorization");
        if(authHeader == null || !authHeader.startsWith("Bearer")){
            throw new RuntimeException("Token Missing");
        }
        String token = authHeader.substring(7);
        Date expiry = jwtUtil.extractExpiration(token);
        long remainingTime = expiry.getTime()-System.currentTimeMillis();
        if(remainingTime>0){
            tokenBlacklistService.blacklistToken(token, remainingTime);
        }
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        return response;
    }

    @PostMapping
    public ApiResponse<User> createUser(@Valid @RequestBody UserDTO dto){
        User user = service.saveUser(dto);

        return new ApiResponse<>(
                true,
                "user created successfully",
                user
        );
    }

    @PostMapping("/forgot-password")
    public Map<String, String> forgotPassword(@RequestBody ForgotPasswordDTO dto){
        String resetToken = passwordResetService.createResetToken(dto.getEmail());
        Map<String, String> response = new HashMap<>();
        response.put("message", "password reset token generated");
        response.put("resetToken", resetToken);
        return response;
    }


    @PutMapping("/{id}")
    public User updateUser(@PathVariable int id, @RequestBody User user){
        return service.updateUser(id, user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void>deleteUser(@PathVariable int id){
        service.deleteUser(id);
        return new ApiResponse<>(
            true,
            "user deleted successfully",
            null
        );
    }

    @GetMapping("/email/{email}")
    public UserResponseDTO getByEmail(@PathVariable String email){
        return service.getUserByEmail(email);
    }
    @GetMapping("/page")
    public Page<User> getUsersPage(@RequestParam int page, @RequestParam int size, @RequestParam String field){
        return service.getUsersWithPagination(page, size, field);

    }
    @GetMapping("/page-sort")
    public Page<User>getUsersPageSort(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam String field){
        return service.getUsersWithPagination(page, size, field);
    }
    @PostMapping("/reset-password")
    public Map<String, String>resetPassword(@RequestBody ResetPasswordDTO dto){
        passwordResetService.resetPassword(
                dto.getToken(),
                dto.getNewPassword()
        );
        Map<String, String>response = new HashMap<>();
        response.put("message", "password updated successfully");
        return response;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping
    public List<User> getUsers(){
        return service.getAllUsers();
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginDTO loginDTO){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDTO.getEmail(),
                        loginDTO.getPassword()
                )
        );
        User user = service.getUserEntityByEmail(loginDTO.getEmail());
        String accessToken = jwtUtil.generateToken(user.getEmail(), user.getRole());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        Map<String, String>tokens = new HashMap<>();
        tokens.put("refreshToken", refreshToken.getToken());


        return tokens;
    }
    @PostMapping("/refresh")
    public Map<String, String> refreshToken(@RequestBody Map<String, String>request){
        String oldRefreshToken = request.get("refreshToken");
        RefreshToken verifiedToken =refreshTokenService.verifyRefreshToken(oldRefreshToken);
        User user = verifiedToken.getUser();
        refreshTokenService.deleteByUser(user);
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);
        String newAccessToken = jwtUtil.generateToken(user.getEmail(), user.getRole());
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", newAccessToken);
        tokens.put("refreshToken", newRefreshToken.getToken());

        return tokens;
    }

}
