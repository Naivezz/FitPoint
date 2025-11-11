package com.naivez.fithub.unit.service;

import com.naivez.fithub.dto.*;
import com.naivez.fithub.entity.*;
import com.naivez.fithub.mapper.ScheduleChangeRequestMapper;
import com.naivez.fithub.mapper.TrainerNoteMapper;
import com.naivez.fithub.mapper.UserMapper;
import com.naivez.fithub.repository.*;
import com.naivez.fithub.service.TrainerService;
import com.naivez.fithub.service.TrainingClassService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    @Mock
    private TrainingClassRepository trainingClassRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TrainerNoteRepository trainerNoteRepository;

    @Mock
    private ScheduleChangeRequestRepository scheduleChangeRequestRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private TrainingClassService trainingClassService;

    @Mock
    private TrainerNoteMapper trainerNoteMapper;

    @Mock
    private ScheduleChangeRequestMapper scheduleChangeRequestMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private TrainerService trainerService;

    private User trainer;
    private User client;
    private User nonTrainer;
    private Role trainerRole;
    private Role clientRole;
    private TrainerNote trainerNote;
    private TrainerNoteDTO trainerNoteDTO;
    private TrainerNoteRequest trainerNoteRequest;
    private TrainingClass trainingClass;
    private Room room;
    private Reservation reservation;
    private ScheduleChangeRequestRequest scheduleChangeRequestRequest;
    private ScheduleChangeRequestDTO scheduleChangeRequestDTO;
    private ScheduleChangeRequest scheduleChangeRequest;
    private ClientProfileDTO clientProfileDTO;
    private UserProfileDTO userProfileDTO;

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

        room = Room.builder()
                .id(1L)
                .name("room1")
                .capacity(20)
                .build();

        trainingClass = TrainingClass.builder()
                .id(1L)
                .name("trainingClass1")
                .trainer(trainer)
                .startTime(LocalDateTime.now().minusDays(1))
                .endTime(LocalDateTime.now().minusDays(1).plusHours(1))
                .capacity(10)
                .reservations(new HashSet<>())
                .build();

        reservation = Reservation.builder()
                .id(1L)
                .user(client)
                .trainingClass(trainingClass)
                .status("CONFIRMED")
                .build();

        trainingClass.getReservations().add(reservation);

        trainerNote = TrainerNote.builder()
                .id(1L)
                .trainer(trainer)
                .client(client)
                .note("Great progress")
                .createdAt(LocalDateTime.now())
                .build();

        trainerNoteDTO = TrainerNoteDTO.builder()
                .id(1L)
                .clientId(2L)
                .clientName("user1")
                .note("Great progress")
                .createdAt(LocalDateTime.now())
                .build();

        trainerNoteRequest = TrainerNoteRequest.builder()
                .clientId(2L)
                .note("Great progress")
                .build();

        scheduleChangeRequestRequest = ScheduleChangeRequestRequest.builder()
                .requestType("ADD")
                .className("trainingClass8")
                .classDescription("description")
                .requestedStartTime(LocalDateTime.now().plusDays(2))
                .requestedEndTime(LocalDateTime.now().plusDays(2).plusHours(1))
                .requestedCapacity(15)
                .reason("Need to add new class type")
                .build();

        scheduleChangeRequestDTO = ScheduleChangeRequestDTO.builder()
                .id(1L)
                .requestType("ADD")
                .className("trainingClass8")
                .status("PENDING")
                .build();

        scheduleChangeRequest = ScheduleChangeRequest.builder()
                .id(1L)
                .trainer(trainer)
                .requestType("ADD")
                .className("trainingClass8")
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        clientProfileDTO = ClientProfileDTO.builder()
                .id(2L)
                .firstName("user2")
                .lastName("user1")
                .email("user1@gmail.com")
                .build();

        userProfileDTO = UserProfileDTO.builder()
                .id(2L)
                .firstName("user2")
                .lastName("user1")
                .email("user1@gmail.com")
                .build();
    }

    @Test
    void createScheduleChangeRequest_withAddRequest_shouldReturnDto() {
        when(userRepository.findByEmail("trainer1@gmail.com")).thenReturn(Optional.of(trainer));
        when(scheduleChangeRequestRepository.save(any(ScheduleChangeRequest.class)))
                .thenReturn(scheduleChangeRequest);
        when(scheduleChangeRequestMapper.toDto(scheduleChangeRequest))
                .thenReturn(scheduleChangeRequestDTO);

        ScheduleChangeRequestDTO result = trainerService.createScheduleChangeRequest("trainer1@gmail.com", scheduleChangeRequestRequest);

        assertThat(result).isEqualTo(scheduleChangeRequestDTO);
        verify(userRepository).findByEmail("trainer1@gmail.com");
        verify(scheduleChangeRequestRepository).save(any(ScheduleChangeRequest.class));
        verify(scheduleChangeRequestMapper).toDto(scheduleChangeRequest);
    }

    @Test
    void createScheduleChangeRequest_withModifyRequestAndWrongOwner_shouldThrowException() {
        User otherTrainer = User.builder()
                .id(4L)
                .email("other@gmail.com")
                .roles(new HashSet<>(Set.of(trainerRole)))
                .build();

        TrainingClass otherTrainerClass = TrainingClass.builder()
                .id(1L)
                .name("trainingClass1")
                .trainer(otherTrainer)
                .build();

        ScheduleChangeRequestRequest modifyRequest = ScheduleChangeRequestRequest.builder()
                .requestType("MODIFY")
                .trainingClassId(1L)
                .className("Updated Yoga Class")
                .reason("Need update time")
                .build();

        when(userRepository.findByEmail("trainer1@gmail.com")).thenReturn(Optional.of(trainer));
        when(trainingClassRepository.findById(1L)).thenReturn(Optional.of(otherTrainerClass));

        assertThatThrownBy(() -> trainerService.createScheduleChangeRequest("trainer1@gmail.com", modifyRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("You can only modify or cancel your own classes");

        verify(scheduleChangeRequestRepository, never()).save(any());
    }

    @Test
    void createScheduleChangeRequest_withInvalidRequestType_shouldThrowException() {
        ScheduleChangeRequestRequest invalidRequest = ScheduleChangeRequestRequest.builder()
                .requestType("INVALID_TYPE")
                .reason("Test reason")
                .build();

        when(userRepository.findByEmail("trainer1@gmail.com")).thenReturn(Optional.of(trainer));

        assertThatThrownBy(() -> trainerService.createScheduleChangeRequest("trainer1@gmail.com", invalidRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid request type. Must be ADD, MODIFY, or CANCEL");

        verify(scheduleChangeRequestRepository, never()).save(any());
    }

    @Test
    void getScheduleChangeRequests_shouldReturnTrainerRequests() {
        when(userRepository.findByEmail("trainer1@gmail.com")).thenReturn(Optional.of(trainer));
        when(scheduleChangeRequestRepository.findByTrainerId(1L)).thenReturn(List.of(scheduleChangeRequest));
        when(scheduleChangeRequestMapper.toDto(scheduleChangeRequest)).thenReturn(scheduleChangeRequestDTO);

        List<ScheduleChangeRequestDTO> result = trainerService.getScheduleChangeRequests("trainer1@gmail.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(scheduleChangeRequestDTO);
        verify(scheduleChangeRequestRepository).findByTrainerId(1L);
    }

    @Test
    void getPendingScheduleChangeRequests_shouldReturnPendingRequestsOnly() {
        when(userRepository.findByEmail("trainer1@gmail.com")).thenReturn(Optional.of(trainer));
        when(scheduleChangeRequestRepository.findByTrainerIdAndStatus(1L, "PENDING"))
                .thenReturn(List.of(scheduleChangeRequest));
        when(scheduleChangeRequestMapper.toDto(scheduleChangeRequest)).thenReturn(scheduleChangeRequestDTO);

        List<ScheduleChangeRequestDTO> result = trainerService.getPendingScheduleChangeRequests("trainer1@gmail.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(scheduleChangeRequestDTO);
        verify(scheduleChangeRequestRepository).findByTrainerIdAndStatus(1L, "PENDING");
    }

    @Test
    void getTrainerClients_shouldReturnClientsWithReservations() {
        when(userRepository.findByEmail("trainer1@gmail.com")).thenReturn(Optional.of(trainer));
        when(trainingClassRepository.findByTrainerIdAndDateRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(trainingClass));
        when(userMapper.toClientProfileDTO(client)).thenReturn(clientProfileDTO);

        List<ClientProfileDTO> result = trainerService.getTrainerClients("trainer1@gmail.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(clientProfileDTO);
        verify(trainingClassRepository).findByTrainerIdAndDateRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void getClientProfile_withValidAccess_shouldReturnClientProfile() {
        when(userRepository.findByEmail("trainer1@gmail.com")).thenReturn(Optional.of(trainer));
        when(userRepository.findById(2L)).thenReturn(Optional.of(client));
        when(trainingClassRepository.findByTrainerIdAndDateRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(trainingClass));
        when(userMapper.toClientProfileDTO(client)).thenReturn(clientProfileDTO);

        ClientProfileDTO result = trainerService.getClientProfile("trainer1@gmail.com", 2L);

        assertThat(result).isEqualTo(clientProfileDTO);
        verify(userRepository).findByEmail("trainer1@gmail.com");
        verify(userRepository).findById(2L);
    }

    @Test
    void getClientProfile_withNoAccess_shouldThrowException() {
        when(userRepository.findByEmail("trainer1@gmail.com")).thenReturn(Optional.of(trainer));
        when(userRepository.findById(2L)).thenReturn(Optional.of(client));
        when(trainingClassRepository.findByTrainerIdAndDateRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        assertThatThrownBy(() -> trainerService.getClientProfile("trainer1@gmail.com", 2L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("You don't have access to this client's profile");

        verify(userMapper, never()).toClientProfileDTO((User) any());
    }

    @Test
    void addClientNote_withValidRequest_shouldReturnTrainerNoteDto() {
        when(userRepository.findByEmail("trainer1@gmail.com")).thenReturn(Optional.of(trainer));
        when(userRepository.findById(2L)).thenReturn(Optional.of(client));
        when(trainingClassRepository.findByTrainerIdAndDateRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(trainingClass));
        when(trainerNoteRepository.save(any(TrainerNote.class))).thenReturn(trainerNote);
        when(trainerNoteMapper.toDto(trainerNote)).thenReturn(trainerNoteDTO);

        TrainerNoteDTO result = trainerService.addClientNote("trainer1@gmail.com", trainerNoteRequest);

        assertThat(result).isEqualTo(trainerNoteDTO);
        verify(userRepository).findByEmail("trainer1@gmail.com");
        verify(userRepository).findById(2L);
        verify(trainerNoteRepository).save(any(TrainerNote.class));
        verify(trainerNoteMapper).toDto(trainerNote);
    }

    @Test
    void addClientNote_withNonTrainer_shouldThrowException() {
        when(userRepository.findByEmail("nottrainer1@gmail.com")).thenReturn(Optional.of(nonTrainer));

        assertThatThrownBy(() -> trainerService.addClientNote("nottrainer1@gmail.com", trainerNoteRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User is not a trainer");

        verify(userRepository).findByEmail("nottrainer1@gmail.com");
        verify(trainerNoteRepository, never()).save(any());
    }

    @Test
    void addClientNote_whenClientNotFound_shouldThrowException() {
        TrainerNoteRequest requestWithInvalidClient = TrainerNoteRequest.builder()
                .clientId(999L)
                .note("Test note")
                .build();

        when(userRepository.findByEmail("trainer1@gmail.com")).thenReturn(Optional.of(trainer));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.addClientNote("trainer1@gmail.com", requestWithInvalidClient))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Client not found");

        verify(userRepository).findById(999L);
        verify(trainerNoteRepository, never()).save(any());
    }

    @Test
    void addClientNote_whenNoAccessToClient_shouldThrowException() {
        when(userRepository.findByEmail("trainer1@gmail.com")).thenReturn(Optional.of(trainer));
        when(userRepository.findById(2L)).thenReturn(Optional.of(client));
        when(trainingClassRepository.findByTrainerIdAndDateRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        assertThatThrownBy(() -> trainerService.addClientNote("trainer1@gmail.com", trainerNoteRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("You don't have access to this client");

        verify(trainerNoteRepository, never()).save(any());
    }

    @Test
    void updateClientNote_withValidRequest_shouldReturnUpdatedNote() {
        TrainerNoteRequest updateRequest = TrainerNoteRequest.builder()
                .clientId(2L)
                .note("Updated progress note")
                .build();

        when(userRepository.findByEmail("trainer1@gmail.com")).thenReturn(Optional.of(trainer));
        when(trainerNoteRepository.findById(1L)).thenReturn(Optional.of(trainerNote));
        when(trainerNoteRepository.save(any(TrainerNote.class))).thenReturn(trainerNote);
        when(trainerNoteMapper.toDto(trainerNote)).thenReturn(trainerNoteDTO);

        TrainerNoteDTO result = trainerService.updateClientNote("trainer1@gmail.com", 1L, updateRequest);

        assertThat(result).isEqualTo(trainerNoteDTO);
        verify(trainerNoteRepository).save(argThat(note -> "Updated progress note".equals(note.getNote())));
        verify(trainerNoteMapper).toDto(trainerNote);
    }

    @Test
    void updateClientNote_whenNoteNotFound_shouldThrowException() {
        when(userRepository.findByEmail("trainer1@gmail.com")).thenReturn(Optional.of(trainer));
        when(trainerNoteRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.updateClientNote("trainer1@gmail.com", 999L, trainerNoteRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Note not found");

        verify(trainerNoteRepository).findById(999L);
        verify(trainerNoteRepository, never()).save(any());
    }

    @Test
    void updateClientNote_whenNotOwnNote_shouldThrowException() {
        User otherTrainer = User.builder()
                .id(4L)
                .email("other@gmail.com")
                .roles(new HashSet<>(Set.of(trainerRole)))
                .build();

        TrainerNote otherTrainerNote = TrainerNote.builder()
                .id(1L)
                .trainer(otherTrainer)
                .client(client)
                .note("Other trainer's note")
                .build();

        when(userRepository.findByEmail("trainer1@gmail.com")).thenReturn(Optional.of(trainer));
        when(trainerNoteRepository.findById(1L)).thenReturn(Optional.of(otherTrainerNote));

        assertThatThrownBy(() -> trainerService.updateClientNote("trainer1@gmail.com", 1L, trainerNoteRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("You can only update your own notes");

        verify(trainerNoteRepository, never()).save(any());
    }

    @Test
    void deleteClientNote_withValidRequest_shouldDeleteNote() {
        when(userRepository.findByEmail("trainer1@gmail.com")).thenReturn(Optional.of(trainer));
        when(trainerNoteRepository.findById(1L)).thenReturn(Optional.of(trainerNote));
        doNothing().when(trainerNoteRepository).delete(trainerNote);

        trainerService.deleteClientNote("trainer1@gmail.com", 1L);

        verify(userRepository).findByEmail("trainer1@gmail.com");
        verify(trainerNoteRepository).findById(1L);
        verify(trainerNoteRepository).delete(trainerNote);
    }

    @Test
    void getAllNotes_shouldReturnTrainerNotes() {
        when(userRepository.findByEmail("trainer1@gmail.com")).thenReturn(Optional.of(trainer));
        when(trainerNoteRepository.findByTrainerId(1L)).thenReturn(List.of(trainerNote));
        when(trainerNoteMapper.toDto(trainerNote)).thenReturn(trainerNoteDTO);

        List<TrainerNoteDTO> result = trainerService.getAllNotes("trainer1@gmail.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(trainerNoteDTO);
        verify(trainerNoteRepository).findByTrainerId(1L);
        verify(trainerNoteMapper).toDto(trainerNote);
    }

    @Test
    void getClientNotes_withValidAccess_shouldReturnClientNotes() {
        when(userRepository.findByEmail("trainer1@gmail.com")).thenReturn(Optional.of(trainer));
        when(trainingClassRepository.findByTrainerIdAndDateRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(trainingClass));
        when(trainerNoteRepository.findByTrainerIdAndClientId(1L, 2L)).thenReturn(List.of(trainerNote));
        when(trainerNoteMapper.toDto(trainerNote)).thenReturn(trainerNoteDTO);

        List<TrainerNoteDTO> result = trainerService.getClientNotes("trainer1@gmail.com", 2L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(trainerNoteDTO);
        verify(trainerNoteRepository).findByTrainerIdAndClientId(1L, 2L);
    }

    @Test
    void getClassRegistrations_withValidClass_shouldReturnRegistrations() {
        when(userRepository.findByEmail("trainer1@gmail.com")).thenReturn(Optional.of(trainer));
        when(trainingClassRepository.findById(1L)).thenReturn(Optional.of(trainingClass));
        when(userMapper.toDto(client)).thenReturn(userProfileDTO);

        List<UserProfileDTO> result = trainerService.getClassRegistrations("trainer1@gmail.com", 1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(userProfileDTO);
        verify(trainingClassRepository).findById(1L);
        verify(userMapper).toDto(client);
    }

    @Test
    void getClassRegistrations_withNotOwnClass_shouldThrowException() {
        User otherTrainer = User.builder()
                .id(4L)
                .email("other@gmail.com")
                .roles(new HashSet<>(Set.of(trainerRole)))
                .build();

        TrainingClass otherTrainerClass = TrainingClass.builder()
                .id(1L)
                .name("trainingClass1")
                .trainer(otherTrainer)
                .reservations(new HashSet<>())
                .build();

        when(userRepository.findByEmail("trainer1@gmail.com")).thenReturn(Optional.of(trainer));
        when(trainingClassRepository.findById(1L)).thenReturn(Optional.of(otherTrainerClass));

        assertThatThrownBy(() -> trainerService.getClassRegistrations("trainer1@gmail.com", 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("You can only view registrations for your own classes");

        verify(userMapper, never()).toDto(any());
    }
}