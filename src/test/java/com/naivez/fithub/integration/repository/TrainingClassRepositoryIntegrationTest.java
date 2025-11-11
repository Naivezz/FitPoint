package com.naivez.fithub.integration.repository;

import com.naivez.fithub.entity.*;
import com.naivez.fithub.repository.TrainingClassRepository;
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
class TrainingClassRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TrainingClassRepository trainingClassRepository;

    private User trainer1;
    private User trainer2;
    private Room room1;
    private Room room2;
    private TrainingClass pastClass;
    private TrainingClass currentClass;
    private TrainingClass futureClass1;
    private TrainingClass futureClass2;
    private Role trainerRole;

    @BeforeEach
    void setUp() {
        trainerRole = Role.builder()
                .name("ROLE_TRAINER")
                .users(new HashSet<>())
                .build();
        entityManager.persist(trainerRole);

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

        room1 = Room.builder()
                .name("room1")
                .capacity(20)
                .equipmentList(new HashSet<>())
                .classes(new HashSet<>())
                .build();
        entityManager.persist(room1);

        room2 = Room.builder()
                .name("room2")
                .capacity(30)
                .equipmentList(new HashSet<>())
                .classes(new HashSet<>())
                .build();
        entityManager.persist(room2);

        pastClass = TrainingClass.builder()
                .name("trainingClass4")
                .description("test3")
                .startTime(LocalDateTime.now().minusDays(1))
                .endTime(LocalDateTime.now().minusDays(1).plusHours(1))
                .capacity(15)
                .trainer(trainer1)
                .room(room1)
                .reservations(new HashSet<>())
                .averageRating(4.5)
                .build();
        entityManager.persist(pastClass);

        currentClass = TrainingClass.builder()
                .name("trainingClass9")
                .description("test4")
                .startTime(LocalDateTime.now().minusMinutes(30))
                .endTime(LocalDateTime.now().plusMinutes(30))
                .capacity(12)
                .trainer(trainer1)
                .room(room1)
                .reservations(new HashSet<>())
                .build();
        entityManager.persist(currentClass);

        futureClass1 = TrainingClass.builder()
                .name("trainingClass11")
                .description("description5")
                .startTime(LocalDateTime.now().plusHours(2))
                .endTime(LocalDateTime.now().plusHours(3))
                .capacity(25)
                .trainer(trainer2)
                .room(room2)
                .reservations(new HashSet<>())
                .build();
        entityManager.persist(futureClass1);

        futureClass2 = TrainingClass.builder()
                .name("trainingClass13")
                .description("description6")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .capacity(10)
                .trainer(trainer1)
                .room(room1)
                .reservations(new HashSet<>())
                .build();
        entityManager.persistAndFlush(futureClass2);
    }

    @Test
    void findUpcomingClasses_shouldReturnOnlyFutureClasses() {
        List<TrainingClass> result = trainingClassRepository.findUpcomingClasses(LocalDateTime.now());

        assertThat(result).hasSize(2);
        assertThat(result).extracting(TrainingClass::getName)
                .containsExactly("trainingClass11", "trainingClass13");

        for (TrainingClass tc : result) {
            assertThat(tc.getStartTime()).isAfter(LocalDateTime.now());
        }
    }

    @Test
    void findUpcomingClasses_shouldBeOrderedByStartTimeAsc() {
        List<TrainingClass> result = trainingClassRepository.findUpcomingClasses(LocalDateTime.now());

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isEqualTo(futureClass1);
        assertThat(result.get(1)).isEqualTo(futureClass2);
        assertThat(result.get(0).getStartTime()).isBefore(result.get(1).getStartTime());
    }

    @Test
    void findUpcomingClasses_whenNoUpcomingClasses_shouldReturnEmptyList() {
        LocalDateTime futureTime = LocalDateTime.now().plusDays(10);

        List<TrainingClass> result = trainingClassRepository.findUpcomingClasses(futureTime);

        assertThat(result).isEmpty();
    }

    @Test
    void findClassesBetween_shouldReturnClassesInRange() {
        LocalDateTime startRange = LocalDateTime.now().minusHours(1);
        LocalDateTime endRange = LocalDateTime.now().plusHours(4);

        List<TrainingClass> result = trainingClassRepository.findClassesBetween(startRange, endRange);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(TrainingClass::getName)
                .containsExactly("trainingClass9", "trainingClass11");
    }

    @Test
    void findClassesBetween_shouldBeOrderedByStartTimeAsc() {
        LocalDateTime startRange = LocalDateTime.now().minusDays(2);
        LocalDateTime endRange = LocalDateTime.now().plusDays(2);

        List<TrainingClass> result = trainingClassRepository.findClassesBetween(startRange, endRange);

        assertThat(result).hasSize(4);

        for (int i = 0; i < result.size() - 1; i++) {
            assertThat(result.get(i).getStartTime()).isBefore(result.get(i + 1).getStartTime());
        }
    }

    @Test
    void findClassesBetween_whenNoClassesInRange_shouldReturnEmptyList() {
        LocalDateTime startRange = LocalDateTime.now().plusDays(10);
        LocalDateTime endRange = LocalDateTime.now().plusDays(11);

        List<TrainingClass> result = trainingClassRepository.findClassesBetween(startRange, endRange);

        assertThat(result).isEmpty();
    }

    @Test
    void findByTrainerIdAndDateRange_shouldReturnTrainerClassesInRange() {
        LocalDateTime startRange = LocalDateTime.now().minusDays(2);
        LocalDateTime endRange = LocalDateTime.now().plusDays(2);

        List<TrainingClass> trainer1Results = trainingClassRepository
                .findByTrainerIdAndDateRange(trainer1.getId(), startRange, endRange);
        List<TrainingClass> trainer2Results = trainingClassRepository
                .findByTrainerIdAndDateRange(trainer2.getId(), startRange, endRange);

        assertThat(trainer1Results).hasSize(3);
        assertThat(trainer1Results).extracting(TrainingClass::getName)
                .containsExactly("trainingClass4", "trainingClass9", "trainingClass13");

        assertThat(trainer2Results).hasSize(1);
        assertThat(trainer2Results.get(0).getName()).isEqualTo("trainingClass11");
    }

    @Test
    void findByTrainerIdAndDateRange_shouldBeOrderedByStartTimeAsc() {
        LocalDateTime startRange = LocalDateTime.now().minusDays(2);
        LocalDateTime endRange = LocalDateTime.now().plusDays(2);

        List<TrainingClass> result = trainingClassRepository
                .findByTrainerIdAndDateRange(trainer1.getId(), startRange, endRange);

        assertThat(result).hasSize(3);

        assertThat(result.get(0)).isEqualTo(pastClass);
        assertThat(result.get(1)).isEqualTo(currentClass);
        assertThat(result.get(2)).isEqualTo(futureClass2);

        for (int i = 0; i < result.size() - 1; i++) {
            assertThat(result.get(i).getStartTime()).isBefore(result.get(i + 1).getStartTime());
        }
    }

    @Test
    void findByTrainerIdAndDateRange_whenTrainerHasNoClasses_shouldReturnEmptyList() {
        User trainerWithoutClasses = User.builder()
                .email("trainer3@test.com")
                .password("password")
                .firstName("user")
                .lastName("trainer1")
                .phone("3333333333")
                .roles(new HashSet<>())
                .memberships(new HashSet<>())
                .reservations(new HashSet<>())
                .notifications(new HashSet<>())
                .build();
        trainerWithoutClasses.getRoles().add(trainerRole);
        entityManager.persistAndFlush(trainerWithoutClasses);

        LocalDateTime startRange = LocalDateTime.now().minusDays(1);
        LocalDateTime endRange = LocalDateTime.now().plusDays(1);

        List<TrainingClass> result = trainingClassRepository
                .findByTrainerIdAndDateRange(trainerWithoutClasses.getId(), startRange, endRange);

        assertThat(result).isEmpty();
    }

    @Test
    void findClassesBetween_withExactBoundaryTimes_shouldIncludeBoundaryClasses() {
        TrainingClass boundaryClass = TrainingClass.builder()
                .name("Test Class")
                .description("Class")
                .startTime(LocalDateTime.now().plusHours(5))
                .endTime(LocalDateTime.now().plusHours(6))
                .capacity(10)
                .trainer(trainer1)
                .room(room1)
                .reservations(new HashSet<>())
                .build();
        entityManager.persistAndFlush(boundaryClass);

        LocalDateTime startRange = LocalDateTime.now().plusHours(5).minusSeconds(1);
        LocalDateTime endRange = LocalDateTime.now().plusHours(6).plusSeconds(1);

        List<TrainingClass> result = trainingClassRepository.findClassesBetween(startRange, endRange);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Test Class");
    }

    @Test
    void findByTrainerIdAndDateRange_withNarrowTimeRange_shouldFilterCorrectly() {
        LocalDateTime startRange = LocalDateTime.now().minusMinutes(45);
        LocalDateTime endRange = LocalDateTime.now().plusMinutes(45);

        List<TrainingClass> result = trainingClassRepository
                .findByTrainerIdAndDateRange(trainer1.getId(), startRange, endRange);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(currentClass);
    }
}