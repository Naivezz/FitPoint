package com.naivez.fithub.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naivez.fithub.dto.AuthResponse;
import com.naivez.fithub.dto.LoginRequest;
import com.naivez.fithub.dto.RegisterRequest;
import com.naivez.fithub.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    private RegisterRequest testRegisterRequest;
    private LoginRequest testLoginRequest;
    private AuthResponse testAuthResponse;

    @BeforeEach
    void setUp() {
        testRegisterRequest = RegisterRequest.builder()
                .email("newuser@example.com")
                .password("password")
                .firstName("user1")
                .lastName("user1")
                .phone("1111111111")
                .build();

        testLoginRequest = LoginRequest.builder()
                .email("user@gmail.com")
                .password("password123")
                .build();

        testAuthResponse = AuthResponse.builder()
                .token("jwt.token.here")
                .email("user@gmail.com")
                .roles(Set.of("ROLE_CLIENT"))
                .build();
    }

    @Test
    void register_withValidRequest_shouldReturnOkAndAuthResponse() throws Exception {
        when(authService.register(any(RegisterRequest.class))).thenReturn(testAuthResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRegisterRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("jwt.token.here"))
                .andExpect(jsonPath("$.email").value("user@gmail.com"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_CLIENT"));
    }

    @Test
    void register_whenEmailAlreadyExists_shouldReturnBadRequest() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("Email already exists"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRegisterRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldAssignDefaultClientRole() throws Exception {
        AuthResponse clientResponse = AuthResponse.builder()
                .token("jwt.token.here")
                .email("newuser@gmail.com")
                .roles(Set.of("ROLE_CLIENT"))
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(clientResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRegisterRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[0]").value("ROLE_CLIENT"));
    }

    @Test
    void login_withValidCredentials_shouldReturnOkAndAuthResponse() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenReturn(testAuthResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("user@gmail.com"))
                .andExpect(jsonPath("$.roles").isArray());
    }

    @Test
    void login_withInvalidCredentials_shouldReturnBadRequest() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testLoginRequest)))
                .andExpect(status().isBadRequest());
    }
}