package com.ecommerce.demo.controller;

import com.ecommerce.demo.dto.ApiResponse;
import com.ecommerce.demo.dto.LoginDTO;
import com.ecommerce.demo.dto.UserDTO;
import com.ecommerce.demo.dto.UserResponseDTO;
import com.ecommerce.demo.entity.RefreshToken;
import com.ecommerce.demo.entity.User;
import com.ecommerce.demo.exception.InvalidCredentialsException;
import com.ecommerce.demo.security.JwtUtil;
import com.ecommerce.demo.service.RefreshTokenService;
import com.ecommerce.demo.service.UserService;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/users")
public class UserController {
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final UserService service;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    public UserController(UserService service, RefreshTokenService refreshTokenService, JwtUtil jwtUtil, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder){
        this.service = service;
        this.refreshTokenService = refreshTokenService;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
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


    @PutMapping("/{id}")
    public User updateUser(@PathVariable int id, @RequestBody User user){
        return service.updateUser(id, user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable int id){
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
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);

        return tokens;
    }
    @PostMapping("/refresh")
    public String refreshToken(@RequestBody Map<String, String>request){
        String refreshToken = request.get("refreshToken");
        String username = jwtUtil.extractUsername(refreshToken);
        return jwtUtil.generateToken(username, "USER");

    }

}
