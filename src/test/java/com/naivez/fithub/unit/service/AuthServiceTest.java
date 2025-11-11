package com.naivez.fithub.unit.service;

import com.naivez.fithub.dto.AuthResponse;
import com.naivez.fithub.dto.LoginRequest;
import com.naivez.fithub.dto.RegisterRequest;
import com.naivez.fithub.entity.Role;
import com.naivez.fithub.entity.User;
import com.naivez.fithub.repository.RoleRepository;
import com.naivez.fithub.repository.UserRepository;
import com.naivez.fithub.security.JwtUtil;
import com.naivez.fithub.service.AuthService;
import com.naivez.fithub.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;
    private Role clientRole;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .email("user1@gmail.com")
                .password("password123")
                .firstName("user1")
                .lastName("user1")
                .phone("1111111111")
                .build();

        loginRequest = LoginRequest.builder()
                .email("user1@gmail.com")
                .password("password123")
                .build();

        clientRole = Role.builder()
                .id(1L)
                .name("ROLE_CLIENT")
                .users(new HashSet<>())
                .build();

        testUser = User.builder()
                .id(1L)
                .email("user1@gmail.com")
                .password("encodedPassword")
                .firstName("user1")
                .lastName("user1")
                .phone("1111111111")
                .roles(new HashSet<>(Set.of(clientRole)))
                .memberships(new HashSet<>())
                .reservations(new HashSet<>())
                .notifications(new HashSet<>())
                .build();
    }

    @Test
    void register_withValidRequest_shouldReturnAuthResponse() {
        when(userRepository.existsByEmail("user1@gmail.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(roleRepository.findByName("ROLE_CLIENT")).thenReturn(Optional.of(clientRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userDetailsService.loadUserByUsername("user1@gmail.com")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("jwt-token");

        AuthResponse result = authService.register(registerRequest);

        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("jwt-token");
        assertThat(result.getEmail()).isEqualTo("user1@gmail.com");
        assertThat(result.getRoles()).contains("ROLE_CLIENT");

        verify(userRepository).existsByEmail("user1@gmail.com");
        verify(passwordEncoder).encode("password123");
        verify(roleRepository).findByName("ROLE_CLIENT");
        verify(userRepository).save(any(User.class));
        verify(userDetailsService).loadUserByUsername("user1@gmail.com");
        verify(jwtUtil).generateToken(userDetails);
    }

    @Test
    void register_whenEmailAlreadyExists_shouldThrowException() {
        when(userRepository.existsByEmail("user1@gmail.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email already exists");

        verify(userRepository).existsByEmail("user1@gmail.com");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_whenClientRoleNotFound_shouldThrowException() {
        when(userRepository.existsByEmail("user1@gmail.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(roleRepository.findByName("ROLE_CLIENT")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Default role not found");

        verify(userRepository).existsByEmail("user1@gmail.com");
        verify(passwordEncoder).encode("password123");
        verify(roleRepository).findByName("ROLE_CLIENT");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_withValidCredentials_shouldReturnAuthResponse() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));
        when(userDetailsService.loadUserByUsername("user1@gmail.com")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("jwt-token");

        AuthResponse result = authService.login(loginRequest);

        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("jwt-token");
        assertThat(result.getEmail()).isEqualTo("user1@gmail.com");
        assertThat(result.getRoles()).contains("ROLE_CLIENT");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("user1@gmail.com");
        verify(userDetailsService).loadUserByUsername("user1@gmail.com");
        verify(jwtUtil).generateToken(userDetails);
    }

    @Test
    void login_withInvalidCredentials_shouldThrowException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid credentials");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByEmail(anyString());
        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    void login_whenUserNotFoundAfterAuthentication_shouldThrowException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("user1@gmail.com");
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    void login_withUserHavingMultipleRoles_shouldReturnAllRoles() {
        Role trainerRole = Role.builder()
                .id(2L)
                .name("ROLE_TRAINER")
                .build();

        User multiRoleUser = User.builder()
                .id(1L)
                .email("trainer1@gmail.com")
                .password("encodedPassword")
                .firstName("user2")
                .lastName("trainer1")
                .roles(Set.of(clientRole, trainerRole))
                .memberships(new HashSet<>())
                .reservations(new HashSet<>())
                .notifications(new HashSet<>())
                .build();

        LoginRequest trainerLogin = LoginRequest.builder()
                .email("trainer1@gmail.com")
                .password("password123")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail("trainer1@gmail.com")).thenReturn(Optional.of(multiRoleUser));
        when(userDetailsService.loadUserByUsername("trainer1@gmail.com")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("jwt-token");

        AuthResponse result = authService.login(trainerLogin);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("trainer1@gmail.com");
        assertThat(result.getRoles()).hasSize(2);
        assertThat(result.getRoles()).contains("ROLE_CLIENT", "ROLE_TRAINER");
    }
}