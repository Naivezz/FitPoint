package com.naivez.fithub.integration.repository;

import com.naivez.fithub.entity.Role;
import com.naivez.fithub.entity.User;
import com.naivez.fithub.repository.RoleRepository;
import com.naivez.fithub.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User testUser;
    private Role clientRole;

    @BeforeEach
    void setUp() {
        clientRole = Role.builder()
                .name("ROLE_CLIENT")
                .users(new HashSet<>())
                .build();
        entityManager.persist(clientRole);

        testUser = User.builder()
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
        testUser.getRoles().add(clientRole);
    }

    @Test
    void findByEmail_whenUserExists_shouldReturnUser() {
        entityManager.persistAndFlush(testUser);
        entityManager.clear();

        Optional<User> result = userRepository.findByEmail("user1@gmail.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("user1@gmail.com");
        assertThat(result.get().getFirstName()).isEqualTo("user1");
        assertThat(result.get().getLastName()).isEqualTo("user1");
    }

    @Test
    void findByEmail_whenUserDoesNotExist_shouldReturnEmpty() {
        Optional<User> result = userRepository.findByEmail("none@gmail.com");

        assertThat(result).isEmpty();
    }

    @Test
    void findByEmail_shouldLoadRolesEagerly() {
        entityManager.persistAndFlush(testUser);
        entityManager.clear();

        Optional<User> result = userRepository.findByEmail("user1@gmail.com");

        assertThat(result).isPresent();
        assertThat(result.get().getRoles()).isNotNull();
        assertThat(result.get().getRoles()).hasSize(1);
        assertThat(result.get().getRoles()).extracting(Role::getName)
                .containsExactly("ROLE_CLIENT");
    }

    @Test
    void existsByEmail_whenUserExists_shouldReturnTrue() {
        entityManager.persistAndFlush(testUser);
        entityManager.clear();

        boolean exists = userRepository.existsByEmail("user1@gmail.com");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_whenUserDoesNotExist_shouldReturnFalse() {
        boolean exists = userRepository.existsByEmail("none@gmail.com");

        assertThat(exists).isFalse();
    }
}