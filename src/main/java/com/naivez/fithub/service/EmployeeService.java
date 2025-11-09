package com.naivez.fithub.service;

import com.naivez.fithub.dto.CreateEmployeeRequest;
import com.naivez.fithub.dto.EmployeeDTO;
import com.naivez.fithub.entity.Role;
import com.naivez.fithub.entity.User;
import com.naivez.fithub.mapper.UserMapper;
import com.naivez.fithub.repository.RoleRepository;
import com.naivez.fithub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public List<EmployeeDTO> getAllEmployees() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRoles().stream()
                        .anyMatch(role -> "ROLE_TRAINER".equals(role.getName()) ||
                                "ROLE_ADMIN".equals(role.getName())))
                .map(userMapper::toEmployeeDTO)
                .collect(Collectors.toList());
    }

    public EmployeeDTO getEmployeeById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
        return userMapper.toEmployeeDTO(user);
    }

    @Transactional
    public EmployeeDTO createEmployee(CreateEmployeeRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("User with email " + request.getEmail() + " already exists");
        }

        Set<Role> roles = new HashSet<>();
        for (String roleName : request.getRoles()) {
            String fullRoleName = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName.toUpperCase();
            Role role = roleRepository.findByName(fullRoleName)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + fullRoleName));
            roles.add(role);
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .roles(roles)
                .memberships(new HashSet<>())
                .reservations(new HashSet<>())
                .notifications(new HashSet<>())
                .build();

        user = userRepository.save(user);
        return userMapper.toEmployeeDTO(user);
    }

    @Transactional
    public void deleteEmployee(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));

        boolean isEmployee = user.getRoles().stream()
                .anyMatch(role -> "ROLE_TRAINER".equals(role.getName()) ||
                        "ROLE_ADMIN".equals(role.getName()));

        if (!isEmployee) {
            throw new RuntimeException("User is not an employee");
        }

        userRepository.deleteById(id);
    }
}
