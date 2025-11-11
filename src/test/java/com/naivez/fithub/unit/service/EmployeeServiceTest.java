package com.naivez.fithub.unit.service;

import com.naivez.fithub.dto.CreateEmployeeRequest;
import com.naivez.fithub.dto.EmployeeDTO;
import com.naivez.fithub.entity.Role;
import com.naivez.fithub.entity.User;
import com.naivez.fithub.mapper.UserMapper;
import com.naivez.fithub.repository.RoleRepository;
import com.naivez.fithub.repository.UserRepository;
import com.naivez.fithub.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private EmployeeService employeeService;

    private User trainerUser;
    private User adminUser;
    private User clientUser;
    private Role trainerRole;
    private Role adminRole;
    private Role clientRole;
    private EmployeeDTO trainerEmployeeDTO;
    private EmployeeDTO adminEmployeeDTO;
    private CreateEmployeeRequest createEmployeeRequest;

    @BeforeEach
    void setUp() {
        trainerRole = Role.builder()
                .id(1L)
                .name("ROLE_TRAINER")
                .build();

        adminRole = Role.builder()
                .id(2L)
                .name("ROLE_ADMIN")
                .build();

        clientRole = Role.builder()
                .id(3L)
                .name("ROLE_CLIENT")
                .build();

        trainerUser = User.builder()
                .id(1L)
                .email("trainer1@gmail.com")
                .firstName("user1")
                .lastName("trainer1")
                .phone("1111111111")
                .roles(new HashSet<>(Set.of(trainerRole)))
                .memberships(new HashSet<>())
                .reservations(new HashSet<>())
                .notifications(new HashSet<>())
                .build();

        adminUser = User.builder()
                .id(2L)
                .email("admin@gmail.com")
                .firstName("user2")
                .lastName("Admin")
                .phone("0987654321")
                .roles(new HashSet<>(Set.of(adminRole)))
                .memberships(new HashSet<>())
                .reservations(new HashSet<>())
                .notifications(new HashSet<>())
                .build();

        clientUser = User.builder()
                .id(3L)
                .email("user1@gmail.com")
                .firstName("user")
                .lastName("user1")
                .phone("5555555555")
                .roles(new HashSet<>(Set.of(clientRole)))
                .memberships(new HashSet<>())
                .reservations(new HashSet<>())
                .notifications(new HashSet<>())
                .build();

        trainerEmployeeDTO = EmployeeDTO.builder()
                .id(1L)
                .email("trainer1@gmail.com")
                .firstName("user1")
                .lastName("trainer1")
                .phone("1111111111")
                .roles(Set.of("ROLE_TRAINER"))
                .build();

        adminEmployeeDTO = EmployeeDTO.builder()
                .id(2L)
                .email("admin@gmail.com")
                .firstName("user2")
                .lastName("Admin")
                .phone("0987654321")
                .roles(Set.of("ROLE_ADMIN"))
                .build();

        createEmployeeRequest = CreateEmployeeRequest.builder()
                .email("newemployee@gmail.com")
                .password("password123")
                .firstName("New")
                .lastName("user1")
                .phone("1111111111")
                .roles(Set.of("TRAINER"))
                .build();
    }

    @Test
    void getAllEmployees_shouldReturnOnlyEmployeeUsers() {
        when(userRepository.findAll()).thenReturn(List.of(trainerUser, adminUser, clientUser));
        when(userMapper.toEmployeeDTO(trainerUser)).thenReturn(trainerEmployeeDTO);
        when(userMapper.toEmployeeDTO(adminUser)).thenReturn(adminEmployeeDTO);

        List<EmployeeDTO> result = employeeService.getAllEmployees();

        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(trainerEmployeeDTO, adminEmployeeDTO);
        verify(userRepository).findAll();
        verify(userMapper).toEmployeeDTO(trainerUser);
        verify(userMapper).toEmployeeDTO(adminUser);
        verify(userMapper, never()).toEmployeeDTO(clientUser);
    }

    @Test
    void getAllEmployees_whenNoEmployees_shouldReturnEmptyList() {
        when(userRepository.findAll()).thenReturn(List.of(clientUser));

        List<EmployeeDTO> result = employeeService.getAllEmployees();

        assertThat(result).isEmpty();
        verify(userRepository).findAll();
        verify(userMapper, never()).toEmployeeDTO(any());
    }

    @Test
    void getAllEmployees_shouldIncludeMultiRoleUsers() {
        User multiRoleUser = User.builder()
                .id(4L)
                .email("multi@gmail.com")
                .firstName("Multi")
                .lastName("Role")
                .roles(new HashSet<>(Set.of(trainerRole, adminRole)))
                .memberships(new HashSet<>())
                .reservations(new HashSet<>())
                .notifications(new HashSet<>())
                .build();

        EmployeeDTO multiRoleEmployeeDTO = EmployeeDTO.builder()
                .id(4L)
                .email("multi@gmail.com")
                .firstName("Multi")
                .lastName("Role")
                .roles(Set.of("ROLE_TRAINER", "ROLE_ADMIN"))
                .build();

        when(userRepository.findAll()).thenReturn(List.of(trainerUser, multiRoleUser, clientUser));
        when(userMapper.toEmployeeDTO(trainerUser)).thenReturn(trainerEmployeeDTO);
        when(userMapper.toEmployeeDTO(multiRoleUser)).thenReturn(multiRoleEmployeeDTO);

        List<EmployeeDTO> result = employeeService.getAllEmployees();

        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(trainerEmployeeDTO, multiRoleEmployeeDTO);
    }

    @Test
    void getEmployeeById_whenEmployeeExists_shouldReturnEmployeeDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(trainerUser));
        when(userMapper.toEmployeeDTO(trainerUser)).thenReturn(trainerEmployeeDTO);

        EmployeeDTO result = employeeService.getEmployeeById(1L);

        assertThat(result).isEqualTo(trainerEmployeeDTO);
        verify(userRepository).findById(1L);
        verify(userMapper).toEmployeeDTO(trainerUser);
    }

    @Test
    void getEmployeeById_whenEmployeeNotFound_shouldThrowException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getEmployeeById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Employee not found with id: 999");

        verify(userRepository).findById(999L);
        verify(userMapper, never()).toEmployeeDTO(any());
    }

    @Test
    void createEmployee_withValidRequest_shouldReturnEmployeeDto() {
        User savedUser = User.builder()
                .id(5L)
                .email("newemployee@gmail.com")
                .password("encodedPassword123")
                .firstName("New")
                .lastName("user1")
                .phone("1111111111")
                .roles(new HashSet<>(Set.of(trainerRole)))
                .memberships(new HashSet<>())
                .reservations(new HashSet<>())
                .notifications(new HashSet<>())
                .build();

        EmployeeDTO savedEmployeeDTO = EmployeeDTO.builder()
                .id(5L)
                .email("newemployee@gmail.com")
                .firstName("New")
                .lastName("user1")
                .phone("1111111111")
                .roles(Set.of("ROLE_TRAINER"))
                .build();

        when(userRepository.findByEmail("newemployee@gmail.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_TRAINER")).thenReturn(Optional.of(trainerRole));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toEmployeeDTO(savedUser)).thenReturn(savedEmployeeDTO);

        EmployeeDTO result = employeeService.createEmployee(createEmployeeRequest);

        assertThat(result).isEqualTo(savedEmployeeDTO);
        verify(userRepository).findByEmail("newemployee@gmail.com");
        verify(roleRepository).findByName("ROLE_TRAINER");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(userMapper).toEmployeeDTO(savedUser);
    }

    @Test
    void createEmployee_withExistingEmail_shouldThrowException() {
        when(userRepository.findByEmail("newemployee@gmail.com")).thenReturn(Optional.of(trainerUser));

        assertThatThrownBy(() -> employeeService.createEmployee(createEmployeeRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User with email newemployee@gmail.com already exists");

        verify(userRepository).findByEmail("newemployee@gmail.com");
        verify(roleRepository, never()).findByName(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createEmployee_withInvalidRole_shouldThrowException() {
        CreateEmployeeRequest requestWithInvalidRole = CreateEmployeeRequest.builder()
                .email("newemployee@gmail.com")
                .password("password123")
                .firstName("New")
                .lastName("user1")
                .phone("1111111111")
                .roles(Set.of("INVALID_ROLE"))
                .build();

        when(userRepository.findByEmail("newemployee@gmail.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_INVALID_ROLE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.createEmployee(requestWithInvalidRole))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Role not found: ROLE_INVALID_ROLE");

        verify(userRepository).findByEmail("newemployee@gmail.com");
        verify(roleRepository).findByName("ROLE_INVALID_ROLE");
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteEmployee_whenEmployeeExists_shouldDeleteSuccessfully() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(trainerUser));
        doNothing().when(userRepository).deleteById(1L);

        employeeService.deleteEmployee(1L);

        verify(userRepository).findById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteEmployee_whenEmployeeNotFound_shouldThrowException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.deleteEmployee(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Employee not found with id: 999");

        verify(userRepository).findById(999L);
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteEmployee_whenUserIsNotEmployee_shouldThrowException() {
        when(userRepository.findById(3L)).thenReturn(Optional.of(clientUser));

        assertThatThrownBy(() -> employeeService.deleteEmployee(3L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User is not an employee");

        verify(userRepository).findById(3L);
        verify(userRepository, never()).deleteById(anyLong());
    }
}