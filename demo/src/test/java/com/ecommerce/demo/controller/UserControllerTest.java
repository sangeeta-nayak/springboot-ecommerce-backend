package com.ecommerce.demo.controller;

import com.ecommerce.demo.dto.UserDTO;
import com.ecommerce.demo.entity.User;
import com.ecommerce.demo.security.JwtUtil;
import com.ecommerce.demo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserService userService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private AuthenticationManager authenticationManager;

    @Test
    void createUser_ShouldReturnSuccess() throws Exception {
        User user = new User();
        user.setId(1);
        user.setName("Sangeeta");
        user.setEmail("sangeeta@gmail.com");

        Mockito.when(userService.saveUser(any(UserDTO.class)))
                .thenReturn(user);

        UserDTO dto = new UserDTO();
        dto.setName("Sangeeta");
        dto.setEmail("sangeeta@gmail.com");
        dto.setPassword("123456");

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }
}