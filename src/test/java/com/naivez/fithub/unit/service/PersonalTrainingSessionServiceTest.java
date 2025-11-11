package com.naivez.fithub.unit.service;

import com.naivez.fithub.dto.PersonalTrainingSessionDTO;
import com.naivez.fithub.dto.PersonalTrainingSessionRequest;
import com.naivez.fithub.entity.PersonalTrainingSession;
import com.naivez.fithub.entity.Role;
import com.naivez.fithub.entity.User;
import com.naivez.fithub.mapper.PersonalTrainingSessionMapper;
import com.naivez.fithub.repository.PersonalTrainingSessionRepository;
import com.naivez.fithub.repository.UserRepository;
import com.naivez.fithub.service.NotificationService;
import com.naivez.fithub.service.PersonalTrainingSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonalTrainingSessionServiceTest {

    @Mock
    private PersonalTrainingSessionRepository personalTrainingSessionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PersonalTrainingSessionMapper personalTrainingSessionMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PersonalTrainingSessionService personalTrainingSessionService;

    private User trainer;
    private User client;
    private User nonTrainer;
    private Role trainerRole;
    private Role clientRole;
    private PersonalTrainingSession session;
    private PersonalTrainingSessionDTO sessionDTO;
    private PersonalTrainingSessionRequest sessionRequest;

    @BeforeEach
    void setUp() {
        trainerRole = Role.builder()
                .id(1L)
                .name("ROLE_TRAINER")
                .build();

        clientRole = Role.builder()
                .id(2L)
                .name("ROLE_CLIENT")
                .build();

        trainer = User.builder()
                .id(1L)
                .email("trainer1@gmail.com")
                .firstName("user1")
                .lastName("trainer1")
                .roles(new HashSet<>(Set.of(trainerRole)))
                .memberships(new HashSet<>())
                .reservations(new HashSet<>())
                .notifications(new HashSet<>())
                .build();

        client = User.builder()
                .id(2L)
                .email("user1@gmail.com")
                .firstName("user2")
                .lastName("user1")
                .roles(new HashSet<>(Set.of(clientRole)))
                .memberships(new HashSet<>())
                .reservations(new HashSet<>())
                .notifications(new HashSet<>())
                .build();

        nonTrainer = User.builder()
                .id(3L)
                .email("nottrainer1@gmail.com")
                .firstName("Not")
                .lastName("trainer1")
                .roles(new HashSet<>(Set.of(clientRole)))
                .memberships(new HashSet<>())
                .reservations(new HashSet<>())
                .notifications(new HashSet<>())
                .build();

        session = PersonalTrainingSession.builder()
                .id(1L)
                .trainer(trainer)
                .client(client)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .sessionGoal("test session goal 4")
                .sessionNotes("Initial notes")
                .status("SCHEDULED")
                .createdAt(LocalDateTime.now())
                .build();

        sessionDTO = PersonalTrainingSessionDTO.builder()
                .id(1L)
                .clientId(2L)
                .clientName("user1")
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .sessionGoal("test session goal 4")
                .sessionNotes("Initial notes")
                .status("SCHEDULED")
                .build();

        sessionRequest = PersonalTrainingSessionRequest.builder()
                .clientId(2L)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .sessionGoal("test session goal 4")
                .sessionNotes("Initial notes")
                .build();
    }

    @Test
    void createPersonalSession_withValidRequest_shouldReturnSessionDto() {
        when(userRepository.findByEmail("trainer1@gmail.com")).thenReturn(Optional.of(trainer));
        when(userRepository.findById(2L)).thenReturn(Optional.of(client));
        when(personalTrainingSessionRepository.save(any(PersonalTrainingSession.class))).thenReturn(session);
        when(personalTrainingSessionMapper.toDto(session)).thenReturn(sessionDTO);
        doNothing().when(notificationService).createNotification(any(User.class), anyString());

        PersonalTrainingSessionDTO result = personalTrainingSessionService.createPersonalSession("trainer1@gmail.com", sessionRequest);

        assertThat(result).isEqualTo(sessionDTO);
        verify(userRepository).findByEmail("trainer1@gmail.com");
        verify(userRepository).findById(2L);
        verify(personalTrainingSessionRepository).save(any(PersonalTrainingSession.class));
        verify(personalTrainingSessionMapper).toDto(session);
        verify(notificationService).createNotification(eq(client),
                String.valueOf(ArgumentMatchers.anyString().contains("test notification message 1")));
    }

    @Test
    void createPersonalSession_withNonTrainer_shouldThrowException() {
        when(userRepository.findByEmail("nottrainer1@gmail.com")).thenReturn(Optional.of(nonTrainer));

        assertThatThrownBy(() -> personalTrainingSessionService.createPersonalSession("nottrainer1@gmail.com", sessionRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User is not a trainer");

        verify(userRepository).findByEmail("nottrainer1@gmail.com");
        verify(personalTrainingSessionRepository, never()).save(any());
    }

    @Test
    void createPersonalSession_withTrainerNotFound_shouldThrowException() {
        when(userRepository.findByEmail("unknown@gmail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> personalTrainingSessionService.createPersonalSession("unknown@gmail.com", sessionRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findByEmail("unknown@gmail.com");
        verify(personalTrainingSessionRepository, never()).save(any());
    }

    @Test
    void createPersonalSession_withClientNotFound_shouldThrowException() {
        when(userRepository.findByEmail("trainer1@gmail.com")).thenReturn(Optional.of(trainer));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        PersonalTrainingSessionRequest requestWithInvalidClient = PersonalTrainingSessionRequest.builder()
                .clientId(999L)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .sessionGoal("Test session")
                .build();

        assertThatThrownBy(() -> personalTrainingSessionService.createPersonalSession("trainer1@gmail.com", requestWithInvalidClient))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Client not found");

        verify(userRepository).findById(999L);
        verify(personalTrainingSessionRepository, never()).save(any());
    }

    @Test
    void createPersonalSession_withEndTimeBeforeStartTime_shouldThrowException() {
        PersonalTrainingSessionRequest invalidRequest = PersonalTrainingSessionRequest.builder()
                .clientId(2L)
                .startTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .sessionGoal("Test session")
                .build();

        when(userRepository.findByEmail("trainer1@gmail.com")).thenReturn(Optional.of(trainer));
        when(userRepository.findById(2L)).thenReturn(Optional.of(client));

        assertThatThrownBy(() -> personalTrainingSessionService.createPersonalSession("trainer1@gmail.com", invalidRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Start time must be before end time");

        verify(personalTrainingSessionRepository, never()).save(any());
    }

    @Test
    void createPersonalSession_withPastStartTime_shouldThrowException() {
        PersonalTrainingSessionRequest pastRequest = PersonalTrainingSessionRequest.builder()
                .clientId(2L)
                .startTime(LocalDateTime.now().minusHours(1))
                .endTime(LocalDateTime.now().plusHours(1))
                .sessionGoal("Test session")
                .build();

        when(userRepository.findByEmail("trainer1@gmail.com")).thenReturn(Optional.of(trainer));
        when(userRepository.findById(2L)).thenReturn(Optional.of(client));

        assertThatThrownBy(() -> personalTrainingSessionService.createPersonalSession("trainer1@gmail.com", pastRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot schedule session in the past");

        verify(personalTrainingSessionRepository, never()).save(any());
    }

    @Test
    void updatePersonalSession_withValidRequest_shouldReturnUpdatedDto() {
        PersonalTrainingSessionRequest updateRequest = PersonalTrainingSessionRequest.builder()
                .startTime(LocalDateTime.now().plusDays(2))
                .endTime(LocalDateTime.now().plusDays(2).plusHours(1))
                .sessionGoal("Updated goal")
                .sessionNotes("Updated notes")
                .build();

        PersonalTrainingSession updatedSession = PersonalTrainingSession.builder()
                .id(1L)
                .trainer(trainer)
                .client(client)
                .startTime(updateRequest.getStartTime())
                .endTime(updateRequest.getEndTime())
                .sessionGoal("Updated goal")
                .sessionNotes("Updated notes")
                .status("SCHEDULED")
                .build();

        PersonalTrainingSessionDTO updatedDTO = PersonalTrainingSessionDTO.builder()
                .id(1L)
                .sessionGoal("Updated goal")
                .sessionNotes("Updated notes")
                .build();

        when(userRepository.findByEmail("trainer1@gmail.com")).thenReturn(Optional.of(trainer));
        when(personalTrainingSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(personalTrainingSessionRepository.save(any(PersonalTrainingSession.class))).thenReturn(updatedSession);
        when(personalTrainingSessionMapper.toDto(updatedSession)).thenReturn(updatedDTO);
        doNothing().when(notificationService).createNotification(any(User.class), anyString());

        PersonalTrainingSessionDTO result = personalTrainingSessionService.updatePersonalSession("trainer1@gmail.com", 1L, updateRequest);

        assertThat(result).isEqualTo(updatedDTO);
        verify(personalTrainingSessionRepository).save(any(PersonalTrainingSession.class));
        verify(notificationService).createNotification(eq(client),
                String.valueOf(ArgumentMatchers.anyString().contains("test notification message 3")));
    }

    @Test
    void updatePersonalSession_withWrongTrainer_shouldThrowException() {
        User otherTrainer = User.builder()
                .id(4L)
                .email("other@gmail.com")
                .roles(new HashSet<>(Set.of(trainerRole)))
                .memberships(new HashSet<>())
                .reservations(new HashSet<>())
                .notifications(new HashSet<>())
                .build();

        when(userRepository.findByEmail("other@gmail.com")).thenReturn(Optional.of(otherTrainer));
        when(personalTrainingSessionRepository.findById(1L)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> personalTrainingSessionService.updatePersonalSession("other@gmail.com", 1L, sessionRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("You can only update your own sessions");

        verify(personalTrainingSessionRepository, never()).save(any());
    }

    @Test
    void cancelPersonalSession_withValidRequest_shouldCancelSession() {
        when(userRepository.findByEmail("trainer1@gmail.com")).thenReturn(Optional.of(trainer));
        when(personalTrainingSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(personalTrainingSessionRepository.save(any(PersonalTrainingSession.class))).thenReturn(session);
        doNothing().when(notificationService).createNotification(any(User.class), anyString());

        personalTrainingSessionService.cancelPersonalSession("trainer1@gmail.com", 1L);

        verify(personalTrainingSessionRepository).save(any(PersonalTrainingSession.class));
        verify(notificationService).createNotification(eq(client),
                String.valueOf(ArgumentMatchers.anyString().contains("test notification message 2")));
        verify(personalTrainingSessionRepository).save(argThat(s -> "CANCELLED".equals(s.getStatus())));
    }

    @Test
    void cancelPersonalSession_withWrongTrainer_shouldThrowException() {
        User otherTrainer = User.builder()
                .id(4L)
                .email("other@gmail.com")
                .roles(new HashSet<>(Set.of(trainerRole)))
                .build();

        when(userRepository.findByEmail("other@gmail.com")).thenReturn(Optional.of(otherTrainer));
        when(personalTrainingSessionRepository.findById(1L)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> personalTrainingSessionService.cancelPersonalSession("other@gmail.com", 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("You can only cancel your own sessions");

        verify(personalTrainingSessionRepository, never()).save(any());
    }

    @Test
    void completePersonalSession_withValidRequest_shouldCompleteSession() {
        when(userRepository.findByEmail("trainer1@gmail.com")).thenReturn(Optional.of(trainer));
        when(personalTrainingSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(personalTrainingSessionRepository.save(any(PersonalTrainingSession.class))).thenReturn(session);
        when(personalTrainingSessionMapper.toDto(session)).thenReturn(sessionDTO);

        PersonalTrainingSessionDTO result = personalTrainingSessionService.completePersonalSession("trainer1@gmail.com", 1L, "test session note 1");

        assertThat(result).isEqualTo(sessionDTO);
        verify(personalTrainingSessionRepository).save(argThat(s ->
                "COMPLETED".equals(s.getStatus()) && "test session note 1".equals(s.getSessionNotes())));
    }

    @Test
    void completePersonalSession_withWrongTrainer_shouldThrowException() {
        User otherTrainer = User.builder()
                .id(4L)
                .email("other@gmail.com")
                .roles(new HashSet<>(Set.of(trainerRole)))
                .build();

        when(userRepository.findByEmail("other@gmail.com")).thenReturn(Optional.of(otherTrainer));
        when(personalTrainingSessionRepository.findById(1L)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> personalTrainingSessionService.completePersonalSession("other@gmail.com", 1L, "Notes"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("You can only complete your own sessions");

        verify(personalTrainingSessionRepository, never()).save(any());
    }

    @Test
    void getAllPersonalSessions_shouldReturnTrainerSessions() {
        when(userRepository.findByEmail("trainer1@gmail.com")).thenReturn(Optional.of(trainer));
        when(personalTrainingSessionRepository.findByTrainerId(1L)).thenReturn(List.of(session));
        when(personalTrainingSessionMapper.toDto(session)).thenReturn(sessionDTO);

        List<PersonalTrainingSessionDTO> result = personalTrainingSessionService.getAllPersonalSessions("trainer1@gmail.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(sessionDTO);
        verify(personalTrainingSessionRepository).findByTrainerId(1L);
    }

    @Test
    void getClientPersonalSessions_shouldReturnClientSpecificSessions() {
        when(userRepository.findByEmail("trainer1@gmail.com")).thenReturn(Optional.of(trainer));
        when(personalTrainingSessionRepository.findByTrainerIdAndClientId(1L, 2L)).thenReturn(List.of(session));
        when(personalTrainingSessionMapper.toDto(session)).thenReturn(sessionDTO);

        List<PersonalTrainingSessionDTO> result = personalTrainingSessionService.getClientPersonalSessions("trainer1@gmail.com", 2L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(sessionDTO);
        verify(personalTrainingSessionRepository).findByTrainerIdAndClientId(1L, 2L);
    }

    @Test
    void getPersonalSessionsInRange_shouldReturnSessionsInDateRange() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusWeeks(1);

        when(userRepository.findByEmail("trainer1@gmail.com")).thenReturn(Optional.of(trainer));
        when(personalTrainingSessionRepository.findByTrainerIdAndDateRange(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(session));
        when(personalTrainingSessionMapper.toDto(session)).thenReturn(sessionDTO);

        List<PersonalTrainingSessionDTO> result = personalTrainingSessionService.getPersonalSessionsInRange("trainer1@gmail.com", startDate, endDate);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(sessionDTO);
        verify(personalTrainingSessionRepository).findByTrainerIdAndDateRange(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void cancelPersonalSession_sessionNotFound_shouldThrowException() {
        when(userRepository.findByEmail("trainer1@gmail.com")).thenReturn(Optional.of(trainer));
        when(personalTrainingSessionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> personalTrainingSessionService.cancelPersonalSession("trainer1@gmail.com", 999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Session not found");

        verify(personalTrainingSessionRepository).findById(999L);
        verify(personalTrainingSessionRepository, never()).save(any());
    }

    @Test
    void completePersonalSession_sessionNotFound_shouldThrowException() {
        when(userRepository.findByEmail("trainer1@gmail.com")).thenReturn(Optional.of(trainer));
        when(personalTrainingSessionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> personalTrainingSessionService.completePersonalSession("trainer1@gmail.com", 999L, "Notes"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Session not found");

        verify(personalTrainingSessionRepository).findById(999L);
        verify(personalTrainingSessionRepository, never()).save(any());
    }
}