package com.naivez.fithub.integration.repository;

import com.naivez.fithub.entity.Role;
import com.naivez.fithub.entity.User;
import com.naivez.fithub.repository.RoleRepository;
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
class RoleRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RoleRepository roleRepository;

    private Role clientRole;
    private Role trainerRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        clientRole = Role.builder()
                .name("ROLE_CLIENT")
                .users(new HashSet<>())
                .build();

        trainerRole = Role.builder()
                .name("ROLE_TRAINER")
                .users(new HashSet<>())
                .build();

        adminRole = Role.builder()
                .name("ROLE_ADMIN")
                .users(new HashSet<>())
                .build();

        entityManager.persist(clientRole);
        entityManager.persist(trainerRole);
        entityManager.persistAndFlush(adminRole);
    }

    @Test
    void findByName_whenRoleExists_shouldReturnRole() {
        Optional<Role> result = roleRepository.findByName("ROLE_CLIENT");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("ROLE_CLIENT");
        assertThat(result.get().getId()).isEqualTo(clientRole.getId());
    }

    @Test
    void findByName_whenRoleDoesNotExist_shouldReturnEmpty() {
        Optional<Role> result = roleRepository.findByName("ROLE_NONEXISTENT");

        assertThat(result).isEmpty();
    }

    @Test
    void findByName_shouldBeCaseExact() {
        Optional<Role> uppercaseResult = roleRepository.findByName("ROLE_CLIENT");
        Optional<Role> lowercaseResult = roleRepository.findByName("role_client");

        assertThat(uppercaseResult).isPresent();
        assertThat(lowercaseResult).isEmpty();
    }

}