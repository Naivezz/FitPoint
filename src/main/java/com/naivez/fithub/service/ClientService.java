package com.naivez.fithub.service;

import com.naivez.fithub.dto.ChangePasswordRequest;
import com.naivez.fithub.dto.UpdateProfileRequest;
import com.naivez.fithub.dto.UserProfileDTO;
import com.naivez.fithub.entity.User;
import com.naivez.fithub.exception.EmailAlreadyExistsException;
import com.naivez.fithub.exception.IncorrectPasswordException;
import com.naivez.fithub.exception.UserNotFoundException;
import com.naivez.fithub.mapper.UserMapper;
import com.naivez.fithub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserProfileDTO getProfile(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + userEmail));

        return userMapper.toDto(user);
    }

    @Transactional
    public UserProfileDTO updateProfile(String userEmail, UpdateProfileRequest request) {
        log.info("Updating profile for user: {}", userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + userEmail));

        if (!request.getEmail().equals(user.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {
            log.warn("Profile update failed - email already in use: {}", request.getEmail());
            throw new EmailAlreadyExistsException("Email is already in use");
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        user = userRepository.save(user);
        log.info("Profile updated successfully for user: {}", userEmail);

        return userMapper.toDto(user);
    }

    @Transactional
    public void changePassword(String userEmail, ChangePasswordRequest request) {
        log.info("Changing password for user: {}", userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + userEmail));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            log.warn("Password change failed - incorrect old password for user: {}", userEmail);
            throw new IncorrectPasswordException("Old password is incorrect");
        }

        if (request.getOldPassword().equals(request.getNewPassword())) {
            log.warn("Password change failed - new password same as old password for user: {}", userEmail);
            throw new IncorrectPasswordException("New password must be different from old password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed successfully for user: {}", userEmail);
    }
}
