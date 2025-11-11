package com.naivez.fithub.unit.service;

import com.naivez.fithub.dto.ChangePasswordRequest;
import com.naivez.fithub.dto.UpdateProfileRequest;
import com.naivez.fithub.dto.UserProfileDTO;
import com.naivez.fithub.entity.User;
import com.naivez.fithub.mapper.UserMapper;
import com.naivez.fithub.repository.UserRepository;
import com.naivez.fithub.service.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private ClientService clientService;

    private User testUser;
    private UserProfileDTO testUserProfileDTO;
    private UpdateProfileRequest updateProfileRequest;
    private ChangePasswordRequest changePasswordRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("user1@gmail.com")
                .password("encodedPassword")
                .firstName("user1")
                .lastName("user1")
                .phone("1111111111")
                .roles(new HashSet<>())
                .memberships(new HashSet<>())
                .reservations(new HashSet<>())
                .notifications(new HashSet<>())
                .build();

        testUserProfileDTO = UserProfileDTO.builder()
                .id(1L)
                .email("user1@gmail.com")
                .firstName("user1")
                .lastName("user1")
                .phone("1111111111")
                .build();

        updateProfileRequest = UpdateProfileRequest.builder()
                .email("user1@gmail.com")
                .firstName("user1")
                .lastName("user1")
                .phone("1111111111")
                .build();

        changePasswordRequest = ChangePasswordRequest.builder()
                .oldPassword("oldPassword")
                .newPassword("newPassword")
                .build();
    }

    @Test
    void getProfile_whenUserExists_shouldReturnUserProfileDto() {
        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(testUser)).thenReturn(testUserProfileDTO);

        UserProfileDTO result = clientService.getProfile("user1@gmail.com");

        assertThat(result).isEqualTo(testUserProfileDTO);
        verify(userRepository).findByEmail("user1@gmail.com");
        verify(userMapper).toDto(testUser);
    }

    @Test
    void getProfile_whenUserNotFound_shouldThrowException() {
        when(userRepository.findByEmail("none@gmail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.getProfile("none@gmail.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findByEmail("none@gmail.com");
        verify(userMapper, never()).toDto(any());
    }

    @Test
    void updateProfile_withValidRequest_shouldReturnUpdatedProfile() {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .email("updated@gmail.com")
                .firstName("user2")
                .lastName("user2")
                .phone("0987654321")
                .build();

        User updatedUser = User.builder()
                .id(1L)
                .email("updated@gmail.com")
                .password("encodedPassword")
                .firstName("user2")
                .lastName("user2")
                .phone("0987654321")
                .roles(new HashSet<>())
                .memberships(new HashSet<>())
                .reservations(new HashSet<>())
                .notifications(new HashSet<>())
                .build();

        UserProfileDTO updatedProfileDTO = UserProfileDTO.builder()
                .id(1L)
                .email("updated@gmail.com")
                .firstName("user2")
                .lastName("user2")
                .phone("0987654321")
                .build();

        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("updated@gmail.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toDto(updatedUser)).thenReturn(updatedProfileDTO);

        UserProfileDTO result = clientService.updateProfile("user1@gmail.com", request);

        assertThat(result).isEqualTo(updatedProfileDTO);
        verify(userRepository).findByEmail("user1@gmail.com");
        verify(userRepository).existsByEmail("updated@gmail.com");
        verify(userRepository).save(any(User.class));
        verify(userMapper).toDto(updatedUser);
    }

    @Test
    void updateProfile_withSameEmail_shouldNotCheckEmailExistence() {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .email("user1@gmail.com")
                .firstName("user2")
                .lastName("user2")
                .phone("0987654321")
                .build();

        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenReturn(testUserProfileDTO);

        UserProfileDTO result = clientService.updateProfile("user1@gmail.com", request);

        assertThat(result).isNotNull();
        verify(userRepository).findByEmail("user1@gmail.com");
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateProfile_withExistingEmail_shouldThrowException() {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .email("user2@gmail.com")
                .firstName("user2")
                .lastName("user2")
                .phone("0987654321")
                .build();

        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("user2@gmail.com")).thenReturn(true);

        assertThatThrownBy(() -> clientService.updateProfile("user1@gmail.com", request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email is already in use");

        verify(userRepository).findByEmail("user1@gmail.com");
        verify(userRepository).existsByEmail("user2@gmail.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateProfile_whenUserNotFound_shouldThrowException() {
        when(userRepository.findByEmail("none@gmail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.updateProfile("none@gmail.com", updateProfileRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findByEmail("none@gmail.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_withValidRequest_shouldUpdatePassword() {
        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

        clientService.changePassword("user1@gmail.com", changePasswordRequest);

        verify(userRepository).findByEmail("user1@gmail.com");
        verify(passwordEncoder).matches("oldPassword", "encodedPassword");
        verify(passwordEncoder).encode("newPassword");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void changePassword_withIncorrectOldPassword_shouldThrowException() {
        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .oldPassword("wrongPassword")
                .newPassword("newPassword")
                .build();

        assertThatThrownBy(() -> clientService.changePassword("user1@gmail.com", request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Old password is incorrect");

        verify(userRepository).findByEmail("user1@gmail.com");
        verify(passwordEncoder).matches("wrongPassword", "encodedPassword");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_withSameOldAndNewPassword_shouldThrowException() {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .oldPassword("samePassword")
                .newPassword("samePassword")
                .build();

        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("samePassword", "encodedPassword")).thenReturn(true);

        assertThatThrownBy(() -> clientService.changePassword("user1@gmail.com", request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("New password must be different from old password");

        verify(userRepository).findByEmail("user1@gmail.com");
        verify(passwordEncoder).matches("samePassword", "encodedPassword");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_whenUserNotFound_shouldThrowException() {
        when(userRepository.findByEmail("none@gmail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.changePassword("none@gmail.com", changePasswordRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findByEmail("none@gmail.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(userRepository, never()).save(any());
    }
}