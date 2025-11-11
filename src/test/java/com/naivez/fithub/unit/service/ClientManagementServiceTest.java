package com.naivez.fithub.unit.service;

import com.naivez.fithub.dto.ClientProfileDTO;
import com.naivez.fithub.dto.MembershipTypeDTO;
import com.naivez.fithub.entity.Role;
import com.naivez.fithub.entity.User;
import com.naivez.fithub.mapper.UserMapper;
import com.naivez.fithub.repository.UserRepository;
import com.naivez.fithub.service.ClientManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientManagementServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private ClientManagementService clientManagementService;

    private User clientUser;
    private User trainerUser;
    private Role clientRole;
    private Role trainerRole;
    private ClientProfileDTO clientProfileDTO;

    @BeforeEach
    void setUp() {
        clientRole = Role.builder()
                .id(1L)
                .name("ROLE_CLIENT")
                .build();

        trainerRole = Role.builder()
                .id(2L)
                .name("ROLE_TRAINER")
                .build();

        clientUser = User.builder()
                .id(1L)
                .email("user1@gmail.com")
                .firstName("user1")
                .lastName("user1")
                .phone("1111111111")
                .roles(new HashSet<>(Set.of(clientRole)))
                .memberships(new HashSet<>())
                .reservations(new HashSet<>())
                .notifications(new HashSet<>())
                .build();

        trainerUser = User.builder()
                .id(2L)
                .email("trainer1@gmail.com")
                .firstName("user2")
                .lastName("trainer1")
                .phone("0987654321")
                .roles(new HashSet<>(Set.of(trainerRole)))
                .memberships(new HashSet<>())
                .reservations(new HashSet<>())
                .notifications(new HashSet<>())
                .build();

        clientProfileDTO = ClientProfileDTO.builder()
                .id(1L)
                .email("user1@gmail.com")
                .firstName("user1")
                .lastName("user1")
                .phone("1111111111")
                .build();
    }

    @Test
    void getAllClients_shouldReturnOnlyClientUsers() {
        when(userRepository.findAll()).thenReturn(List.of(clientUser, trainerUser));
        when(userMapper.toClientProfileDTO(clientUser)).thenReturn(clientProfileDTO);

        List<ClientProfileDTO> result = clientManagementService.getAllClients();

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(clientProfileDTO);
        verify(userRepository).findAll();
        verify(userMapper).toClientProfileDTO(clientUser);
        verify(userMapper, never()).toClientProfileDTO(trainerUser);
    }

    @Test
    void getAllClients_whenNoClients_shouldReturnEmptyList() {
        when(userRepository.findAll()).thenReturn(List.of(trainerUser));

        List<ClientProfileDTO> result = clientManagementService.getAllClients();

        assertThat(result).isEmpty();
        verify(userRepository).findAll();
        verify(userMapper, never()).toClientProfileDTO((User) any());
    }

    @Test
    void getAllClients_shouldFilterMultiRoleUsers() {
        User multiRoleUser = User.builder()
                .id(3L)
                .email("user@gmail.com")
                .firstName("Multi")
                .lastName("Role")
                .roles(new HashSet<>(Set.of(clientRole, trainerRole)))
                .memberships(new HashSet<>())
                .reservations(new HashSet<>())
                .notifications(new HashSet<>())
                .build();

        ClientProfileDTO multiRoleClientDTO = ClientProfileDTO.builder()
                .id(3L)
                .email("user@gmail.com")
                .firstName("Multi")
                .lastName("Role")
                .build();

        when(userRepository.findAll()).thenReturn(List.of(clientUser, trainerUser, multiRoleUser));
        when(userMapper.toClientProfileDTO(clientUser)).thenReturn(clientProfileDTO);
        when(userMapper.toClientProfileDTO(multiRoleUser)).thenReturn(multiRoleClientDTO);

        List<ClientProfileDTO> result = clientManagementService.getAllClients();

        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(clientProfileDTO, multiRoleClientDTO);
        verify(userMapper).toClientProfileDTO(clientUser);
        verify(userMapper).toClientProfileDTO(multiRoleUser);
        verify(userMapper, never()).toClientProfileDTO(trainerUser);
    }

    @Test
    void getClientById_whenClientExists_shouldReturnClientProfile() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(clientUser));
        when(userMapper.toClientProfileDTO(clientUser)).thenReturn(clientProfileDTO);

        ClientProfileDTO result = clientManagementService.getClientById(1L);

        assertThat(result).isEqualTo(clientProfileDTO);
        verify(userRepository).findById(1L);
        verify(userMapper).toClientProfileDTO(clientUser);
    }

    @Test
    void getClientById_whenClientNotFound_shouldThrowException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientManagementService.getClientById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Client not found with id: 999");

        verify(userRepository).findById(999L);
        verify(userMapper, never()).toClientProfileDTO((User) any());
    }

    @Test
    void getMembershipTypes_shouldReturnAllMembershipTypes() {
        List<MembershipTypeDTO> result = clientManagementService.getMembershipTypes();

        assertThat(result).hasSize(3);

        MembershipTypeDTO monthly = result.stream()
                .filter(m -> "MONTHLY".equals(m.getType()))
                .findFirst()
                .orElseThrow();
        assertThat(monthly.getDurationDays()).isEqualTo(30);
        assertThat(monthly.getPrice()).isEqualTo(new BigDecimal("99.99"));

        MembershipTypeDTO quarterly = result.stream()
                .filter(m -> "QUARTERLY".equals(m.getType()))
                .findFirst()
                .orElseThrow();
        assertThat(quarterly.getDurationDays()).isEqualTo(90);
        assertThat(quarterly.getPrice()).isEqualTo(new BigDecimal("249.99"));

        MembershipTypeDTO annual = result.stream()
                .filter(m -> "ANNUAL".equals(m.getType()))
                .findFirst()
                .orElseThrow();
        assertThat(annual.getDurationDays()).isEqualTo(365);
        assertThat(annual.getPrice()).isEqualTo(new BigDecimal("899.99"));
    }

    @Test
    void getAllClients_shouldHandleEmptyRoles() {
        User userWithoutRoles = User.builder()
                .id(4L)
                .email("noroles@gmail.com")
                .firstName("No")
                .lastName("Roles")
                .roles(new HashSet<>())
                .memberships(new HashSet<>())
                .reservations(new HashSet<>())
                .notifications(new HashSet<>())
                .build();

        when(userRepository.findAll()).thenReturn(List.of(clientUser, userWithoutRoles));
        when(userMapper.toClientProfileDTO(clientUser)).thenReturn(clientProfileDTO);

        List<ClientProfileDTO> result = clientManagementService.getAllClients();

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(clientProfileDTO);
        verify(userMapper).toClientProfileDTO(clientUser);
        verify(userMapper, never()).toClientProfileDTO(userWithoutRoles);
    }
}