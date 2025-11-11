package com.naivez.fithub.integration.repository;

import com.naivez.fithub.entity.*;
import com.naivez.fithub.repository.TrainerNoteRepository;
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
class TrainerNoteRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TrainerNoteRepository trainerNoteRepository;

    private User trainer1;
    private User trainer2;
    private User client1;
    private User client2;
    private TrainerNote note1;
    private TrainerNote note2;
    private TrainerNote note3;
    private Role trainerRole;
    private Role clientRole;

    @BeforeEach
    void setUp() {
        trainerRole = Role.builder()
                .name("ROLE_TRAINER")
                .users(new HashSet<>())
                .build();
        entityManager.persist(trainerRole);

        clientRole = Role.builder()
                .name("ROLE_CLIENT")
                .users(new HashSet<>())
                .build();
        entityManager.persist(clientRole);

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

        client1 = User.builder()
                .email("client1@test.com")
                .password("password")
                .firstName("user6")
                .lastName("user1")
                .phone("1111111111")
                .roles(new HashSet<>())
                .memberships(new HashSet<>())
                .reservations(new HashSet<>())
                .notifications(new HashSet<>())
                .build();
        client1.getRoles().add(clientRole);
        entityManager.persist(client1);

        client2 = User.builder()
                .email("client2@test.com")
                .password("password")
                .firstName("user")
                .lastName("user1")
                .phone("2222222222")
                .roles(new HashSet<>())
                .memberships(new HashSet<>())
                .reservations(new HashSet<>())
                .notifications(new HashSet<>())
                .build();
        client2.getRoles().add(clientRole);
        entityManager.persist(client2);

        note1 = TrainerNote.builder()
                .trainer(trainer1)
                .client(client1)
                .note("test trainer note 1")
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persist(note1);

        note2 = TrainerNote.builder()
                .trainer(trainer1)
                .client(client2)
                .note("test trainer note 5")
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();
        entityManager.persist(note2);

        note3 = TrainerNote.builder()
                .trainer(trainer2)
                .client(client1)
                .note("test trainer note 6")
                .createdAt(LocalDateTime.now().minusHours(2))
                .build();
        entityManager.persistAndFlush(note3);
    }

    @Test
    void findByTrainerId_shouldReturnNotesOrderedByCreatedAtDesc() {
        List<TrainerNote> result = trainerNoteRepository.findByTrainerId(trainer1.getId());

        assertThat(result).hasSize(2);

        assertThat(result.get(0)).isEqualTo(note1);
        assertThat(result.get(1)).isEqualTo(note2);
        assertThat(result.get(0).getCreatedAt()).isAfter(result.get(1).getCreatedAt());
    }

    @Test
    void findByTrainerId_whenTrainerHasNoNotes_shouldReturnEmptyList() {
        User trainerWithoutNotes = User.builder()
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
        trainerWithoutNotes.getRoles().add(trainerRole);
        entityManager.persistAndFlush(trainerWithoutNotes);

        List<TrainerNote> result = trainerNoteRepository.findByTrainerId(trainerWithoutNotes.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void findByTrainerIdAndClientId_shouldReturnSpecificNotes() {
        List<TrainerNote> trainer1Client1Notes = trainerNoteRepository
                .findByTrainerIdAndClientId(trainer1.getId(), client1.getId());
        List<TrainerNote> trainer1Client2Notes = trainerNoteRepository
                .findByTrainerIdAndClientId(trainer1.getId(), client2.getId());
        List<TrainerNote> trainer2Client1Notes = trainerNoteRepository
                .findByTrainerIdAndClientId(trainer2.getId(), client1.getId());

        assertThat(trainer1Client1Notes).hasSize(1);
        assertThat(trainer1Client1Notes.get(0)).isEqualTo(note1);

        assertThat(trainer1Client2Notes).hasSize(1);
        assertThat(trainer1Client2Notes.get(0)).isEqualTo(note2);

        assertThat(trainer2Client1Notes).hasSize(1);
        assertThat(trainer2Client1Notes.get(0)).isEqualTo(note3);
    }

    @Test
    void findByTrainerIdAndClientId_whenNoNotesExist_shouldReturnEmptyList() {
        List<TrainerNote> result = trainerNoteRepository
                .findByTrainerIdAndClientId(trainer2.getId(), client2.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void findByTrainerIdAndClientId_shouldBeOrderedByCreatedAtDesc() {
        TrainerNote newerNote = TrainerNote.builder()
                .trainer(trainer1)
                .client(client1)
                .note("test note 3")
                .createdAt(LocalDateTime.now().plusMinutes(30))
                .build();
        entityManager.persistAndFlush(newerNote);

        List<TrainerNote> result = trainerNoteRepository
                .findByTrainerIdAndClientId(trainer1.getId(), client1.getId());

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isEqualTo(newerNote);
        assertThat(result.get(1)).isEqualTo(note1);
        assertThat(result.get(0).getCreatedAt()).isAfter(result.get(1).getCreatedAt());
    }

    @Test
    void findByTrainerId_shouldIncludeNotesFromDifferentClients() {
        TrainerNote additionalNote = TrainerNote.builder()
                .trainer(trainer1)
                .client(client1)
                .note("Second session notes for client1")
                .createdAt(LocalDateTime.now().plusHours(1))
                .build();
        entityManager.persistAndFlush(additionalNote);

        List<TrainerNote> result = trainerNoteRepository.findByTrainerId(trainer1.getId());

        assertThat(result).hasSize(3);
        assertThat(result).extracting(note -> note.getClient().getEmail())
                .containsExactlyInAnyOrder("client1@test.com", "client2@test.com", "client1@test.com");

        assertThat(result.get(0).getCreatedAt()).isAfter(result.get(1).getCreatedAt());
        assertThat(result.get(1).getCreatedAt()).isAfter(result.get(2).getCreatedAt());
    }

    @Test
    void findByTrainerIdAndClientId_withNonExistentIds_shouldReturnEmptyList() {
        List<TrainerNote> result1 = trainerNoteRepository.findByTrainerIdAndClientId(999L, client1.getId());
        List<TrainerNote> result2 = trainerNoteRepository.findByTrainerIdAndClientId(trainer1.getId(), 999L);
        List<TrainerNote> result3 = trainerNoteRepository.findByTrainerIdAndClientId(999L, 999L);

        assertThat(result1).isEmpty();
        assertThat(result2).isEmpty();
        assertThat(result3).isEmpty();
    }
}