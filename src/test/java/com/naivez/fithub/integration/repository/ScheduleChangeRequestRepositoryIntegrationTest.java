package com.naivez.fithub.integration.repository;

import com.naivez.fithub.entity.*;
import com.naivez.fithub.repository.ScheduleChangeRequestRepository;
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
class ScheduleChangeRequestRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ScheduleChangeRequestRepository scheduleChangeRequestRepository;

    private User trainer1;
    private User trainer2;
    private User admin;
    private ScheduleChangeRequest pendingRequest;
    private ScheduleChangeRequest approvedRequest;
    private ScheduleChangeRequest rejectedRequest;
    private Role trainerRole;
    private Role adminRole;
    private Room testRoom;
    private TrainingClass testTrainingClass;

    @BeforeEach
    void setUp() {
        trainerRole = Role.builder()
                .name("ROLE_TRAINER")
                .users(new HashSet<>())
                .build();
        entityManager.persist(trainerRole);

        adminRole = Role.builder()
                .name("ROLE_ADMIN")
                .users(new HashSet<>())
                .build();
        entityManager.persist(adminRole);

        testRoom = Room.builder()
                .name("Test Room")
                .capacity(20)
                .equipmentList(new HashSet<>())
                .classes(new HashSet<>())
                .build();
        entityManager.persist(testRoom);

        trainer1 = User.builder()
                .email("trainer1@test.com")
                .password("password")
                .firstName("user1")
                .lastName("trainer1")
                .phone("1111111111")
                .roles(new HashSet<>())
                .memberships(new HashSet<>())
                .reservations(new HashSet<>())
                .notifications(new HashSet<>())
                .build();
        trainer1.getRoles().add(trainerRole);
        entityManager.persist(trainer1);

        trainer2 = User.builder()
                .email("trainer2@test.com")
                .password("password")
                .firstName("user2")
                .lastName("trainer1")
                .phone("0987654321")
                .roles(new HashSet<>())
                .memberships(new HashSet<>())
                .reservations(new HashSet<>())
                .notifications(new HashSet<>())
                .build();
        trainer2.getRoles().add(trainerRole);
        entityManager.persist(trainer2);

        admin = User.builder()
                .email("admin@test.com")
                .password("password")
                .firstName("Admin")
                .lastName("User")
                .phone("5555555555")
                .roles(new HashSet<>())
                .memberships(new HashSet<>())
                .reservations(new HashSet<>())
                .notifications(new HashSet<>())
                .build();
        admin.getRoles().add(adminRole);
        entityManager.persist(admin);

        testTrainingClass = TrainingClass.builder()
                .name("trainingClass1")
                .description("description1")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .capacity(10)
                .trainer(trainer1)
                .room(testRoom)
                .reservations(new HashSet<>())
                .build();
        entityManager.persist(testTrainingClass);

        pendingRequest = ScheduleChangeRequest.builder()
                .trainer(trainer1)
                .requestType("MODIFY")
                .trainingClass(testTrainingClass)
                .reason("Need to change time")
                .requestedStartTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .requestedEndTime(LocalDateTime.now().plusDays(1).plusHours(3))
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persist(pendingRequest);

        approvedRequest = ScheduleChangeRequest.builder()
                .trainer(trainer1)
                .requestType("CANCEL")
                .trainingClass(testTrainingClass)
                .reason("Trainer illness")
                .status("APPROVED")
                .createdAt(LocalDateTime.now().minusDays(1))
                .reviewedAt(LocalDateTime.now().minusHours(12))
                .reviewedBy(admin)
                .build();
        entityManager.persist(approvedRequest);

        rejectedRequest = ScheduleChangeRequest.builder()
                .trainer(trainer2)
                .requestType("ADD")
                .className("trainingClass8")
                .requestedStartTime(LocalDateTime.now().plusDays(2))
                .requestedEndTime(LocalDateTime.now().plusDays(2).plusHours(1))
                .requestedCapacity(15)
                .reason("Want to add new class type")
                .status("REJECTED")
                .createdAt(LocalDateTime.now().minusDays(2))
                .reviewedAt(LocalDateTime.now().minusDays(1))
                .reviewedBy(admin)
                .build();
        entityManager.persistAndFlush(rejectedRequest);
    }

    @Test
    void findByTrainerId_shouldReturnRequestsOrderedByCreatedAtDesc() {
        List<ScheduleChangeRequest> result = scheduleChangeRequestRepository.findByTrainerId(trainer1.getId());

        assertThat(result).hasSize(2);

        assertThat(result.get(0)).isEqualTo(pendingRequest);
        assertThat(result.get(1)).isEqualTo(approvedRequest);
        assertThat(result.get(0).getCreatedAt()).isAfter(result.get(1).getCreatedAt());
    }

    @Test
    void findByTrainerId_whenTrainerHasNoRequests_shouldReturnEmptyList() {
        User trainerWithoutRequests = User.builder()
                .email("trainer3@test.com")
                .password("password")
                .firstName("user3")
                .lastName("trainer1")
                .phone("1111111111")
                .roles(new HashSet<>())
                .memberships(new HashSet<>())
                .reservations(new HashSet<>())
                .notifications(new HashSet<>())
                .build();
        trainerWithoutRequests.getRoles().add(trainerRole);
        entityManager.persistAndFlush(trainerWithoutRequests);

        List<ScheduleChangeRequest> result = scheduleChangeRequestRepository.findByTrainerId(trainerWithoutRequests.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void findByTrainerIdAndStatus_shouldReturnFilteredRequests() {
        List<ScheduleChangeRequest> pendingResults = scheduleChangeRequestRepository
                .findByTrainerIdAndStatus(trainer1.getId(), "PENDING");
        List<ScheduleChangeRequest> approvedResults = scheduleChangeRequestRepository
                .findByTrainerIdAndStatus(trainer1.getId(), "APPROVED");

        assertThat(pendingResults).hasSize(1);
        assertThat(pendingResults.get(0)).isEqualTo(pendingRequest);

        assertThat(approvedResults).hasSize(1);
        assertThat(approvedResults.get(0)).isEqualTo(approvedRequest);
    }

    @Test
    void findByTrainerIdAndStatus_whenNoMatchingStatus_shouldReturnEmptyList() {
        List<ScheduleChangeRequest> result = scheduleChangeRequestRepository
                .findByTrainerIdAndStatus(trainer1.getId(), "REJECTED");

        assertThat(result).isEmpty();
    }

    @Test
    void findByStatus_shouldReturnAllRequestsWithStatus() {
        List<ScheduleChangeRequest> pendingResults = scheduleChangeRequestRepository.findByStatus("PENDING");
        List<ScheduleChangeRequest> approvedResults = scheduleChangeRequestRepository.findByStatus("APPROVED");
        List<ScheduleChangeRequest> rejectedResults = scheduleChangeRequestRepository.findByStatus("REJECTED");

        assertThat(pendingResults).hasSize(1);
        assertThat(pendingResults.get(0)).isEqualTo(pendingRequest);

        assertThat(approvedResults).hasSize(1);
        assertThat(approvedResults.get(0)).isEqualTo(approvedRequest);

        assertThat(rejectedResults).hasSize(1);
        assertThat(rejectedResults.get(0)).isEqualTo(rejectedRequest);
    }

    @Test
    void findByStatus_shouldBeOrderedByCreatedAtDesc() {
        ScheduleChangeRequest newerPendingRequest = ScheduleChangeRequest.builder()
                .trainer(trainer2)
                .requestType("CANCEL")
                .trainingClass(testTrainingClass)
                .reason("Schedule conflict")
                .status("PENDING")
                .createdAt(LocalDateTime.now().plusMinutes(1))
                .build();
        entityManager.persistAndFlush(newerPendingRequest);

        List<ScheduleChangeRequest> result = scheduleChangeRequestRepository.findByStatus("PENDING");

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isEqualTo(newerPendingRequest);
        assertThat(result.get(1)).isEqualTo(pendingRequest);
        assertThat(result.get(0).getCreatedAt()).isAfter(result.get(1).getCreatedAt());
    }

    @Test
    void findByStatus_whenNoRequestsWithStatus_shouldReturnEmptyList() {
        List<ScheduleChangeRequest> result = scheduleChangeRequestRepository.findByStatus("NONEXISTENT_STATUS");

        assertThat(result).isEmpty();
    }

    @Test
    void findByTrainerId_shouldHandleDifferentRequestTypes() {
        ScheduleChangeRequest cancelRequest = ScheduleChangeRequest.builder()
                .trainer(trainer1)
                .requestType("CANCEL")
                .trainingClass(testTrainingClass)
                .reason("vacation")
                .status("PENDING")
                .createdAt(LocalDateTime.now().plusMinutes(5))
                .build();
        entityManager.persistAndFlush(cancelRequest);

        List<ScheduleChangeRequest> result = scheduleChangeRequestRepository.findByTrainerId(trainer1.getId());

        assertThat(result).hasSize(3);
        assertThat(result).extracting(ScheduleChangeRequest::getRequestType)
                .containsExactlyInAnyOrder("CANCEL", "MODIFY", "CANCEL");
    }
}