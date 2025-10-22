package com.naivez.fithub.service;

import com.naivez.fithub.dto.AuthResponse;
import com.naivez.fithub.dto.LoginRequest;
import com.naivez.fithub.dto.RegisterRequest;
import com.naivez.fithub.entity.Role;
import com.naivez.fithub.entity.User;
import com.naivez.fithub.repository.RoleRepository;
import com.naivez.fithub.repository.UserRepository;
import com.naivez.fithub.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;
    private Role clientRole;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .email("dima@gmail.com")
                .password("password123")
                .firstName("Dima")
                .lastName("Vykpish")
                .phone("+48333333333")
                .build();

        loginRequest = LoginRequest.builder()
                .email("dima@gmail.com")
                .password("password123")
                .build();

        clientRole = Role.builder()
                .id(1L)
                .name("ROLE_CLIENT")
                .build();

        Set<Role> roles = new HashSet<>();
        roles.add(clientRole);

        user = User.builder()
                .id(1L)
                .email("dima@gmail.com")
                .password("encodedPassword")
                .firstName("Dima")
                .lastName("Vykpish")
                .phone("+48333333333")
                .build();
        user.setRoles(roles);
    }

    @Test
    void register_withValidRequest_shouldReturnAuthResponse() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(roleRepository.findByName("ROLE_CLIENT")).thenReturn(Optional.of(clientRole));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userDetailsService.loadUserByUsername(registerRequest.getEmail())).thenReturn(mock(UserDetails.class));
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("dima@gmail.com", response.getEmail());
        assertTrue(response.getRoles().contains("ROLE_CLIENT"));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_whenEmailAlreadyExists_shouldThrowException() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_withValidCredentials_shouldReturnAuthResponse()  {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(userDetailsService.loadUserByUsername(loginRequest.getEmail())).thenReturn(mock(UserDetails.class));
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("dima@gmail.com", response.getEmail());
        assertTrue(response.getRoles().contains("ROLE_CLIENT"));
    }

    @Test
    void login_whenUserNotFound_shouldThrowException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
    }
}