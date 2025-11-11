package com.naivez.fithub.integration.repository;

import com.naivez.fithub.entity.*;
import com.naivez.fithub.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ReservationRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ReservationRepository reservationRepository;

    private User testUser;
    private User testUser2;
    private TrainingClass testTrainingClass;
    private TrainingClass testTrainingClass2;
    private Reservation testReservation;
    private Room testRoom;
    private Role clientRole;

    @BeforeEach
    void setUp() {
        clientRole = Role.builder()
                .name("ROLE_CLIENT")
                .users(new HashSet<>())
                .build();
        entityManager.persist(clientRole);

        testRoom = Room.builder()
                .name("Test Room")
                .capacity(20)
                .equipmentList(new HashSet<>())
                .classes(new HashSet<>())
                .build();
        entityManager.persist(testRoom);

        testUser = User.builder()
                .email("user1@test.com")
                .password("password")
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

        testUser2 = User.builder()
                .email("user2@test.com")
                .password("password")
                .firstName("user2")
                .lastName("user2")
                .phone("0987654321")
                .roles(new HashSet<>())
                .memberships(new HashSet<>())
                .reservations(new HashSet<>())
                .notifications(new HashSet<>())
                .build();
        testUser2.getRoles().add(clientRole);
        entityManager.persist(testUser2);

        testTrainingClass = TrainingClass.builder()
                .name("trainingClass1")
                .description("description1")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .capacity(10)
                .trainer(testUser)
                .room(testRoom)
                .reservations(new HashSet<>())
                .build();
        entityManager.persist(testTrainingClass);

        testTrainingClass2 = TrainingClass.builder()
                .name("trainingClass10")
                .description("description10")
                .startTime(LocalDateTime.now().plusDays(2))
                .endTime(LocalDateTime.now().plusDays(2).plusHours(1))
                .capacity(15)
                .trainer(testUser2)
                .room(testRoom)
                .reservations(new HashSet<>())
                .build();
        entityManager.persist(testTrainingClass2);

        testReservation = Reservation.builder()
                .user(testUser)
                .trainingClass(testTrainingClass)
                .reservationDate(LocalDateTime.now())
                .status("CONFIRMED")
                .rating(null)
                .comment(null)
                .build();
        entityManager.persistAndFlush(testReservation);
    }

    @Test
    void findByUserIdOrderByReservationDateDesc_shouldReturnUserReservationsOrderedByDate() {
        Reservation olderReservation = Reservation.builder()
                .user(testUser)
                .trainingClass(testTrainingClass2)
                .reservationDate(LocalDateTime.now().minusDays(1))
                .status("CONFIRMED")
                .build();
        entityManager.persistAndFlush(olderReservation);

        List<Reservation> result = reservationRepository.findByUserIdOrderByReservationDateDesc(testUser.getId());

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isEqualTo(testReservation);
        assertThat(result.get(1)).isEqualTo(olderReservation);
        assertThat(result.get(0).getReservationDate()).isAfter(result.get(1).getReservationDate());
    }

    @Test
    void findByUserIdOrderByReservationDateDesc_whenUserHasNoReservations_shouldReturnEmptyList() {
        List<Reservation> result = reservationRepository.findByUserIdOrderByReservationDateDesc(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void findByUserWithTrainingClass_shouldReturnReservationsWithFetchedTrainingClass() {
        List<Reservation> result = reservationRepository.findByUserWithTrainingClass(testUser.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testReservation);
        assertThat(result.get(0).getTrainingClass()).isNotNull();
        assertThat(result.get(0).getTrainingClass().getName()).isEqualTo("trainingClass1");
        assertThat(result.get(0).getTrainingClass().getDescription()).isEqualTo("description1");
    }

    @Test
    void findByUserWithTrainingClass_whenUserHasNoReservations_shouldReturnEmptyList() {
        List<Reservation> result = reservationRepository.findByUserWithTrainingClass(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void findByUserAndTrainingClass_whenReservationExists_shouldReturnReservation() {
        Optional<Reservation> result = reservationRepository.findByUserAndTrainingClass(testUser, testTrainingClass);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testReservation);
    }

    @Test
    void findByUserAndTrainingClass_whenReservationDoesNotExist_shouldReturnEmpty() {
        Optional<Reservation> result = reservationRepository.findByUserAndTrainingClass(testUser2, testTrainingClass);

        assertThat(result).isEmpty();
    }

    @Test
    void countConfirmedReservationsByClassId_shouldReturnCorrectCount() {
        Reservation confirmedReservation2 = Reservation.builder()
                .user(testUser2)
                .trainingClass(testTrainingClass)
                .reservationDate(LocalDateTime.now())
                .status("CONFIRMED")
                .build();
        entityManager.persist(confirmedReservation2);

        Reservation pendingReservation = Reservation.builder()
                .user(testUser2)
                .trainingClass(testTrainingClass)
                .reservationDate(LocalDateTime.now())
                .status("PENDING")
                .build();
        entityManager.persist(pendingReservation);
        entityManager.flush();

        long count = reservationRepository.countConfirmedReservationsByClassId(testTrainingClass.getId());

        assertThat(count).isEqualTo(2);
    }

    @Test
    void countConfirmedReservationsByClassId_whenNoConfirmedReservations_shouldReturnZero() {
        Reservation cancelledReservation = Reservation.builder()
                .user(testUser2)
                .trainingClass(testTrainingClass2)
                .reservationDate(LocalDateTime.now())
                .status("CANCELLED")
                .build();
        entityManager.persistAndFlush(cancelledReservation);

        long count = reservationRepository.countConfirmedReservationsByClassId(testTrainingClass2.getId());

        assertThat(count).isEqualTo(0);
    }

    @Test
    void existsByUserAndTrainingClassAndStatus_whenReservationExists_shouldReturnTrue() {
        boolean exists = reservationRepository.existsByUserAndTrainingClassAndStatus(
                testUser, testTrainingClass, "CONFIRMED");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByUserAndTrainingClassAndStatus_whenReservationExistsWithDifferentStatus_shouldReturnFalse() {
        boolean exists = reservationRepository.existsByUserAndTrainingClassAndStatus(
                testUser, testTrainingClass, "CANCELLED");

        assertThat(exists).isFalse();
    }

    @Test
    void existsByUserAndTrainingClassAndStatus_whenReservationDoesNotExist_shouldReturnFalse() {
        boolean exists = reservationRepository.existsByUserAndTrainingClassAndStatus(
                testUser2, testTrainingClass, "CONFIRMED");

        assertThat(exists).isFalse();
    }

    @Test
    void findByUserWithTrainingClass_shouldOrderByTrainingClassStartTimeDesc() {
        Reservation futureReservation = Reservation.builder()
                .user(testUser)
                .trainingClass(testTrainingClass2)
                .reservationDate(LocalDateTime.now())
                .status("CONFIRMED")
                .build();
        entityManager.persistAndFlush(futureReservation);

        List<Reservation> result = reservationRepository.findByUserWithTrainingClass(testUser.getId());

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isEqualTo(futureReservation);
        assertThat(result.get(1)).isEqualTo(testReservation);
        assertThat(result.get(0).getTrainingClass().getStartTime())
                .isAfter(result.get(1).getTrainingClass().getStartTime());
    }
}