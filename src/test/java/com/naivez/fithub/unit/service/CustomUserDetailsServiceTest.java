package com.naivez.fithub.unit.service;

import com.naivez.fithub.entity.Role;
import com.naivez.fithub.entity.User;
import com.naivez.fithub.repository.UserRepository;
import com.naivez.fithub.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User testUser;
    private Role adminRole;
    private Role clientRole;

    @BeforeEach
    void setUp() {
        adminRole = Role.builder()
                .id(1L)
                .name("ROLE_ADMIN")
                .build();

        clientRole = Role.builder()
                .id(2L)
                .name("ROLE_CLIENT")
                .build();

        testUser = User.builder()
                .id(1L)
                .email("admin1@gmail.com")
                .password("$2a$12$encodedPassword")
                .firstName("Admin")
                .lastName("User")
                .roles(Set.of(adminRole))
                .build();
    }

    @Test
    void loadUserByUsername_whenUserExists_shouldReturnUserDetails() {
        when(userRepository.findByEmail("admin1@gmail.com")).thenReturn(Optional.of(testUser));

        UserDetails result = customUserDetailsService.loadUserByUsername("admin1@gmail.com");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("admin1@gmail.com");
        assertThat(result.getPassword()).isEqualTo("$2a$12$encodedPassword");
        assertThat(result.getAuthorities()).hasSize(1);
        assertThat(result.getAuthorities())
                .extracting("authority")
                .contains("ROLE_ADMIN");
        assertThat(result.isEnabled()).isTrue();
        assertThat(result.isAccountNonExpired()).isTrue();
        assertThat(result.isAccountNonLocked()).isTrue();
        assertThat(result.isCredentialsNonExpired()).isTrue();

        verify(userRepository).findByEmail("admin1@gmail.com");
    }

    @Test
    void loadUserByUsername_whenUserNotFound_shouldThrowUsernameNotFoundException() {
        when(userRepository.findByEmail("none@gmail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("none@gmail.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found with email: none@gmail.com");

        verify(userRepository).findByEmail("none@gmail.com");
    }

    @Test
    void loadUserByUsername_whenUserHasMultipleRoles_shouldReturnAllAuthorities() {
        User multiRoleUser = User.builder()
                .id(2L)
                .email("user1@gmail.com")
                .password("$2a$12$managerPassword")
                .firstName("Manager")
                .lastName("User")
                .roles(Set.of(adminRole, clientRole))
                .build();

        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(multiRoleUser));

        UserDetails result = customUserDetailsService.loadUserByUsername("user1@gmail.com");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("user1@gmail.com");
        assertThat(result.getPassword()).isEqualTo("$2a$12$managerPassword");
        assertThat(result.getAuthorities()).hasSize(2);
        assertThat(result.getAuthorities())
                .extracting("authority")
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_CLIENT");

        verify(userRepository).findByEmail("user1@gmail.com");
    }

    @Test
    void loadUserByUsername_whenUserHasNoRoles_shouldReturnEmptyAuthorities() {
        User noRoleUser = User.builder()
                .id(3L)
                .email("user1@gmail.com")
                .password("$2a$12$noRolePassword")
                .firstName("No Role")
                .lastName("User")
                .roles(Set.of())
                .build();

        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(noRoleUser));

        UserDetails result = customUserDetailsService.loadUserByUsername("user1@gmail.com");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("user1@gmail.com");
        assertThat(result.getPassword()).isEqualTo("$2a$12$noRolePassword");
        assertThat(result.getAuthorities()).isEmpty();

        verify(userRepository).findByEmail("user1@gmail.com");
    }

    @Test
    void loadUserByUsername_withNullEmail_shouldThrowUsernameNotFoundException() {
        when(userRepository.findByEmail(null)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(null))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found with email: null");

        verify(userRepository).findByEmail(null);
    }
}