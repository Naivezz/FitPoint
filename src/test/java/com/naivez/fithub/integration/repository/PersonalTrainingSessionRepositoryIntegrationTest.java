package com.naivez.fithub.integration.repository;

import com.naivez.fithub.entity.*;
import com.naivez.fithub.repository.PersonalTrainingSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class PersonalTrainingSessionRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PersonalTrainingSessionRepository personalTrainingSessionRepository;

    private User testClient;
    private User testTrainer;
    private PersonalTrainingSession testSession1;
    private PersonalTrainingSession testSession2;
    private PersonalTrainingSession testSession3;

    @BeforeEach
    void setUp() {
        Role clientRole = Role.builder()
                .name("ROLE_CLIENT")
                .build();
        entityManager.persistAndFlush(clientRole);

        Role trainerRole = Role.builder()
                .name("ROLE_TRAINER")
                .build();
        entityManager.persistAndFlush(trainerRole);

        testClient = User.builder()
                .firstName("user1")
                .lastName("user1")
                .email("user@gmail.com")
                .password("password")
                .phone("1111111111")
                .roles(new HashSet<>(Set.of(clientRole)))
                .memberships(new HashSet<>())
                .reservations(new HashSet<>())
                .notifications(new HashSet<>())
                .build();
        entityManager.persistAndFlush(testClient);

        testTrainer = User.builder()
                .firstName("user2")
                .lastName("user2")
                .email("user1@gmail.com")
                .password("password")
                .phone("0987654321")
                .roles(new HashSet<>(Set.of(trainerRole)))
                .memberships(new HashSet<>())
                .reservations(new HashSet<>())
                .notifications(new HashSet<>())
                .build();
        entityManager.persistAndFlush(testTrainer);

        testSession1 = PersonalTrainingSession.builder()
                .client(testClient)
                .trainer(testTrainer)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .sessionGoal("test session goal 3")
                .sessionNotes("test notes")
                .status("SCHEDULED")
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persistAndFlush(testSession1);

        testSession2 = PersonalTrainingSession.builder()
                .client(testClient)
                .trainer(testTrainer)
                .startTime(LocalDateTime.now().plusDays(2))
                .endTime(LocalDateTime.now().plusDays(2).plusHours(1))
                .sessionGoal("test session goal 5")
                .sessionNotes("test session goal 7")
                .status("SCHEDULED")
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persistAndFlush(testSession2);

        testSession3 = PersonalTrainingSession.builder()
                .client(testClient)
                .trainer(testTrainer)
                .startTime(LocalDateTime.now().minusDays(1))
                .endTime(LocalDateTime.now().minusDays(1).plusHours(1))
                .sessionGoal("goal1")
                .sessionNotes("notes1")
                .status("COMPLETED")
                .createdAt(LocalDateTime.now().minusDays(2))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();
        entityManager.persistAndFlush(testSession3);

        entityManager.clear();
    }

    @Test
    void findByTrainerId_shouldReturnSessionsForSpecificTrainer() {
        List<PersonalTrainingSession> sessions = personalTrainingSessionRepository.findByTrainerId(testTrainer.getId());

        assertThat(sessions).hasSize(3);
        assertThat(sessions).allSatisfy(session -> {
            assertThat(session.getTrainer().getId()).isEqualTo(testTrainer.getId());
        });

        assertThat(sessions.get(0).getStartTime()).isAfter(sessions.get(1).getStartTime());
        assertThat(sessions.get(1).getStartTime()).isAfter(sessions.get(2).getStartTime());
    }

    @Test
    void findByTrainerIdAndDateRange_shouldReturnSessionsInDateRange() {
        LocalDateTime startRange = LocalDateTime.now().minusDays(2);
        LocalDateTime endRange = LocalDateTime.now().plusDays(3);

        List<PersonalTrainingSession> sessions = personalTrainingSessionRepository
                .findByTrainerIdAndDateRange(testTrainer.getId(), startRange, endRange);

        assertThat(sessions).hasSize(3);
        assertThat(sessions).allSatisfy(session -> {
            assertThat(session.getStartTime()).isAfter(startRange);
            assertThat(session.getStartTime()).isBefore(endRange);
            assertThat(session.getTrainer().getId()).isEqualTo(testTrainer.getId());
        });

        assertThat(sessions.get(0).getStartTime()).isBefore(sessions.get(1).getStartTime());
        assertThat(sessions.get(1).getStartTime()).isBefore(sessions.get(2).getStartTime());
    }

    @Test
    void findByTrainerIdAndDateRange_withNarrowRange_shouldReturnLimitedSessions() {
        LocalDateTime startRange = LocalDateTime.now().plusDays(1).minusHours(1);
        LocalDateTime endRange = LocalDateTime.now().plusDays(1).plusHours(2);

        List<PersonalTrainingSession> sessions = personalTrainingSessionRepository
                .findByTrainerIdAndDateRange(testTrainer.getId(), startRange, endRange);

        assertThat(sessions).hasSize(1);
        assertThat(sessions.get(0).getSessionGoal()).isEqualTo("test session goal 3");
        assertThat(sessions.get(0).getStartTime()).isAfter(startRange);
        assertThat(sessions.get(0).getStartTime()).isBefore(endRange);
    }

    @Test
    void findByTrainerIdAndDateRange_withPastRange_shouldReturnPastSessions() {
        LocalDateTime startRange = LocalDateTime.now().minusDays(3);
        LocalDateTime endRange = LocalDateTime.now().minusHours(12);

        List<PersonalTrainingSession> sessions = personalTrainingSessionRepository
                .findByTrainerIdAndDateRange(testTrainer.getId(), startRange, endRange);

        assertThat(sessions).hasSize(1);
        assertThat(sessions.get(0).getStatus()).isEqualTo("COMPLETED");
        assertThat(sessions.get(0).getStartTime()).isBefore(LocalDateTime.now());
    }

    @Test
    void findByTrainerIdAndClientId_shouldReturnSessionsForSpecificTrainerAndClient() {
        List<PersonalTrainingSession> sessions = personalTrainingSessionRepository
                .findByTrainerIdAndClientId(testTrainer.getId(), testClient.getId());

        assertThat(sessions).hasSize(3);
        assertThat(sessions).allSatisfy(session -> {
            assertThat(session.getTrainer().getId()).isEqualTo(testTrainer.getId());
            assertThat(session.getClient().getId()).isEqualTo(testClient.getId());
        });

        assertThat(sessions.get(0).getStartTime()).isAfter(sessions.get(1).getStartTime());
        assertThat(sessions.get(1).getStartTime()).isAfter(sessions.get(2).getStartTime());
    }
}