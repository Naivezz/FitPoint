package com.naivez.fithub.integration.repository;

import com.naivez.fithub.entity.Membership;
import com.naivez.fithub.entity.Role;
import com.naivez.fithub.entity.User;
import com.naivez.fithub.repository.MembershipRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class MembershipRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MembershipRepository membershipRepository;

    private User testUser;
    private Membership activeMembership;
    private Membership expiredMembership;
    private Membership futureMembership;

    @BeforeEach
    void setUp() {
        Role clientRole = Role.builder()
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
        entityManager.persist(testUser);

        activeMembership = Membership.builder()
                .user(testUser)
                .type("MONTHLY")
                .startDate(LocalDate.now().minusDays(5))
                .endDate(LocalDate.now().plusDays(25))
                .price(new BigDecimal("99.99"))
                .active(true)
                .build();

        expiredMembership = Membership.builder()
                .user(testUser)
                .type("WEEKLY")
                .startDate(LocalDate.now().minusDays(14))
                .endDate(LocalDate.now().minusDays(7))
                .price(new BigDecimal("29.99"))
                .active(false)
                .build();

        futureMembership = Membership.builder()
                .user(testUser)
                .type("QUARTERLY")
                .startDate(LocalDate.now().plusDays(10))
                .endDate(LocalDate.now().plusDays(100))
                .price(new BigDecimal("249.99"))
                .active(true)
                .build();

        entityManager.persist(activeMembership);
        entityManager.persist(expiredMembership);
        entityManager.persist(futureMembership);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void findByUserIdOrderByEndDateDesc_shouldReturnMembershipsOrderedByEndDate() {
        List<Membership> result = membershipRepository.findByUserIdOrderByEndDateDesc(testUser.getId());

        assertThat(result).hasSize(3);

        assertThat(result.get(0).getType()).isEqualTo("QUARTERLY");
        assertThat(result.get(1).getType()).isEqualTo("MONTHLY");
        assertThat(result.get(2).getType()).isEqualTo("WEEKLY");
    }

    @Test
    void findByUserIdOrderByEndDateDesc_whenNoMemberships_shouldReturnEmptyList() {
        User userWithoutMemberships = User.builder()
                .email("nomembership@gmail.com")
                .password("encodedPassword")
                .firstName("user2")
                .lastName("user1")
                .phone("2222222222")
                .roles(new HashSet<>())
                .memberships(new HashSet<>())
                .reservations(new HashSet<>())
                .notifications(new HashSet<>())
                .build();
        entityManager.persistAndFlush(userWithoutMemberships);

        List<Membership> result = membershipRepository.findByUserIdOrderByEndDateDesc(userWithoutMemberships.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void findActiveByUserId_shouldReturnOnlyActiveMemberships() {
        List<Membership> result = membershipRepository.findActiveByUserId(testUser.getId(), LocalDate.now());

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Membership::getType)
                .containsExactlyInAnyOrder("MONTHLY", "QUARTERLY");

        for (Membership membership : result) {
            assertThat(membership.isActive()).isTrue();
            assertThat(membership.getEndDate()).isAfterOrEqualTo(LocalDate.now());
        }
    }

    @Test
    void findActiveByUserId_withPastDate_shouldReturnDifferentResults() {
        LocalDate pastDate = LocalDate.now().minusDays(10);

        List<Membership> result = membershipRepository.findActiveByUserId(testUser.getId(), pastDate);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Membership::getType)
                .containsExactlyInAnyOrder("MONTHLY", "QUARTERLY");
    }

    @Test
    void findActiveByUserId_whenUserHasNoActiveMemberships_shouldReturnEmptyList() {
        List<Membership> userMemberships = membershipRepository.findByUserIdOrderByEndDateDesc(testUser.getId());
        for (Membership membership : userMemberships) {
            membership.setActive(false);
            entityManager.merge(membership);
        }
        entityManager.flush();
        entityManager.clear();

        List<Membership> result = membershipRepository.findActiveByUserId(testUser.getId(), LocalDate.now());

        assertThat(result).isEmpty();
    }

    @Test
    void findValidByUserId_shouldReturnNonExpiredMemberships() {
        List<Membership> result = membershipRepository.findValidByUserId(testUser.getId(), LocalDate.now());

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Membership::getType)
                .containsExactlyInAnyOrder("MONTHLY", "QUARTERLY");

        for (Membership membership : result) {
            assertThat(membership.getEndDate()).isAfterOrEqualTo(LocalDate.now());
        }
    }

    @Test
    void findValidByUserId_includesInactiveMembershipsIfNotExpired() {
        activeMembership.setActive(false);
        entityManager.merge(activeMembership);
        entityManager.flush();
        entityManager.clear();

        List<Membership> result = membershipRepository.findValidByUserId(testUser.getId(), LocalDate.now());

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Membership::getType)
                .containsExactlyInAnyOrder("MONTHLY", "QUARTERLY");
    }

    @Test
    void delete_shouldRemoveMembershipFromDatabase() {
        Long membershipId = activeMembership.getId();

        membershipRepository.delete(activeMembership);
        entityManager.flush();

        Membership deletedMembership = entityManager.find(Membership.class, membershipId);
        assertThat(deletedMembership).isNull();
    }

    @Test
    void findValidByUserId_withFutureDate_shouldReturnAllNonExpiredAtThatDate() {
        LocalDate futureDate = LocalDate.now().plusDays(50);

        List<Membership> result = membershipRepository.findValidByUserId(testUser.getId(), futureDate);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo("QUARTERLY");
    }
}