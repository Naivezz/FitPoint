package com.naivez.fithub.unit.mapper;

import com.naivez.fithub.dto.ClientProfileDTO;
import com.naivez.fithub.dto.EmployeeDTO;
import com.naivez.fithub.dto.UserProfileDTO;
import com.naivez.fithub.entity.Role;
import com.naivez.fithub.entity.User;
import com.naivez.fithub.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userMapper = Mappers.getMapper(UserMapper.class);
    }

    @Test
    void toDto_whenEntityHasAllFields_shouldMapCorrectly() {
        User user = User.builder()
                .id(1L)
                .email("user@gmail.com")
                .firstName("user1")
                .lastName("user1")
                .phone("+1234567890")
                .build();

        UserProfileDTO result = userMapper.toDto(user);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("user@gmail.com");
        assertThat(result.getFirstName()).isEqualTo("user1");
        assertThat(result.getLastName()).isEqualTo("user1");
        assertThat(result.getPhone()).isEqualTo("+1234567890");
    }

    @Test
    void toDto_whenEntityIsNull_shouldReturnNull() {
        UserProfileDTO result = userMapper.toDto((User) null);

        assertThat(result).isNull();
    }

    @Test
    void toEntity_whenDtoHasAllFields_shouldMapCorrectly() {
        UserProfileDTO dto = UserProfileDTO.builder()
                .id(3L)
                .email("acacacacaca@gmail.com")
                .firstName("user2")
                .lastName("user2")
                .phone("+0987654321")
                .build();

        User result = userMapper.toEntity(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getEmail()).isEqualTo("acacacacaca@gmail.com");
        assertThat(result.getFirstName()).isEqualTo("user2");
        assertThat(result.getLastName()).isEqualTo("user2");
        assertThat(result.getPhone()).isEqualTo("+0987654321");
    }

    @Test
    void toEntity_whenDtoIsNull_shouldReturnNull() {
        User result = userMapper.toEntity((UserProfileDTO) null);

        assertThat(result).isNull();
    }

    @Test
    void toEmployeeDTO_whenUserHasRoles_shouldMapCorrectly() {
        Role adminRole = Role.builder()
                .id(1L)
                .name("ROLE_ADMIN")
                .build();

        Role trainerRole = Role.builder()
                .id(2L)
                .name("ROLE_TRAINER")
                .build();

        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        roles.add(trainerRole);

        User user = User.builder()
                .id(4L)
                .email("user1@gmail.com")
                .firstName("Employee")
                .lastName("Admin")
                .phone("+1111111111")
                .roles(roles)
                .build();

        EmployeeDTO result = userMapper.toEmployeeDTO(user);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(4L);
        assertThat(result.getEmail()).isEqualTo("user1@gmail.com");
        assertThat(result.getFirstName()).isEqualTo("Employee");
        assertThat(result.getLastName()).isEqualTo("Admin");
        assertThat(result.getPhone()).isEqualTo("+1111111111");
        assertThat(result.getRoles()).isNotNull();
        assertThat(result.getRoles()).hasSize(2);
        assertThat(result.getRoles()).contains("ROLE_ADMIN", "ROLE_TRAINER");
    }

    @Test
    void toEmployeeDTO_whenUserHasNoRoles_shouldMapWithEmptyRoles() {
        User user = User.builder()
                .id(5L)
                .email("user1@gmail.com")
                .firstName("No")
                .lastName("Roles")
                .roles(new HashSet<>())
                .build();

        EmployeeDTO result = userMapper.toEmployeeDTO(user);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getEmail()).isEqualTo("user1@gmail.com");
        assertThat(result.getRoles()).isNotNull();
        assertThat(result.getRoles()).isEmpty();
    }

    @Test
    void toEmployeeDTO_whenUserHasNullRoles_shouldMapWithNullRoles() {
        User user = User.builder()
                .id(6L)
                .email("user1@gmail.com")
                .firstName("Null")
                .lastName("Roles")
                .roles(null)
                .build();

        EmployeeDTO result = userMapper.toEmployeeDTO(user);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(6L);
        assertThat(result.getEmail()).isEqualTo("user1@gmail.com");
        assertThat(result.getRoles()).isNull();
    }

    @Test
    void toClientProfileDTO_whenUserHasAllFields_shouldMapCorrectly() {
        User user = User.builder()
                .id(7L)
                .email("user1@gmail.com")
                .firstName("Client")
                .lastName("User")
                .phone("+2222222222")
                .build();

        ClientProfileDTO result = userMapper.toClientProfileDTO(user);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(7L);
        assertThat(result.getEmail()).isEqualTo("user1@gmail.com");
        assertThat(result.getFirstName()).isEqualTo("Client");
        assertThat(result.getLastName()).isEqualTo("User");
        assertThat(result.getPhone()).isEqualTo("+2222222222");
    }

    @Test
    void toClientProfileDTO_whenUserIsNull_shouldReturnNull() {
        ClientProfileDTO result = userMapper.toClientProfileDTO((User) null);

        assertThat(result).isNull();
    }

    @Test
    void toClientProfileDTO_whenUserProfileDTO_shouldMapCorrectly() {
        UserProfileDTO userProfile = UserProfileDTO.builder()
                .id(8L)
                .email("profile@gmail.com")
                .firstName("Profile")
                .lastName("User")
                .phone("+3333333333")
                .build();

        ClientProfileDTO result = userMapper.toClientProfileDTO(userProfile);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(8L);
        assertThat(result.getEmail()).isEqualTo("profile@gmail.com");
        assertThat(result.getFirstName()).isEqualTo("Profile");
        assertThat(result.getLastName()).isEqualTo("User");
        assertThat(result.getPhone()).isEqualTo("+3333333333");
    }

    @Test
    void toClientProfileDTO_whenUserProfileDTOIsNull_shouldReturnNull() {
        ClientProfileDTO result = userMapper.toClientProfileDTO((UserProfileDTO) null);

        assertThat(result).isNull();
    }

    @Test
    void mapRolesToStrings_whenRolesHaveNames_shouldReturnRoleNames() {
        Role clientRole = Role.builder()
                .id(1L)
                .name("ROLE_CLIENT")
                .build();

        Role trainerRole = Role.builder()
                .id(2L)
                .name("ROLE_TRAINER")
                .build();

        Role adminRole = Role.builder()
                .id(3L)
                .name("ROLE_ADMIN")
                .build();

        Set<Role> roles = Set.of(clientRole, trainerRole, adminRole);

        Set<String> result = userMapper.mapRolesToStrings(roles);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result).contains("ROLE_CLIENT", "ROLE_TRAINER", "ROLE_ADMIN");
    }

    @Test
    void mapRolesToStrings_whenRolesIsNull_shouldReturnNull() {
        Set<String> result = userMapper.mapRolesToStrings(null);

        assertThat(result).isNull();
    }

    @Test
    void mapRolesToStrings_whenRolesIsEmpty_shouldReturnEmptySet() {
        Set<Role> emptyRoles = new HashSet<>();

        Set<String> result = userMapper.mapRolesToStrings(emptyRoles);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }
}