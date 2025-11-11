package com.naivez.fithub.unit.service;

import com.naivez.fithub.dto.ReviewScheduleChangeRequest;
import com.naivez.fithub.dto.ScheduleChangeRequestDTO;
import com.naivez.fithub.entity.*;
import com.naivez.fithub.mapper.ScheduleChangeRequestMapper;
import com.naivez.fithub.repository.ScheduleChangeRequestRepository;
import com.naivez.fithub.repository.TrainingClassRepository;
import com.naivez.fithub.repository.UserRepository;
import com.naivez.fithub.service.ScheduleChangeRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleChangeRequestServiceTest {

    @Mock
    private ScheduleChangeRequestRepository scheduleChangeRequestRepository;

    @Mock
    private TrainingClassRepository trainingClassRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ScheduleChangeRequestMapper scheduleChangeRequestMapper;

    @InjectMocks
    private ScheduleChangeRequestService scheduleChangeRequestService;

    private User trainer;
    private User admin;
    private Role trainerRole;
    private Role adminRole;
    private TrainingClass trainingClass;
    private Room room;
    private ScheduleChangeRequest scheduleChangeRequest;
    private ScheduleChangeRequestDTO scheduleChangeRequestDTO;
    private ReviewScheduleChangeRequest reviewRequest;

    @BeforeEach
    void setUp() {
        trainerRole = Role.builder()
                .id(1L)
                .name("ROLE_TRAINER")
                .build();

        adminRole = Role.builder()
                .id(2L)
                .name("ROLE_ADMIN")
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

        admin = User.builder()
                .id(2L)
                .email("admin@gmail.com")
                .firstName("Admin")
                .lastName("User")
                .roles(new HashSet<>(Set.of(adminRole)))
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
                .description("yoga")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .capacity(10)
                .trainer(trainer)
                .room(room)
                .reservations(new HashSet<>())
                .build();

        scheduleChangeRequest = ScheduleChangeRequest.builder()
                .id(1L)
                .trainer(trainer)
                .trainingClass(trainingClass)
                .requestType("MODIFY")
                .reason("Need to change time")
                .className("Updated Yoga Class")
                .classDescription("yoga")
                .requestedStartTime(LocalDateTime.now().plusDays(2))
                .requestedEndTime(LocalDateTime.now().plusDays(2).plusHours(1))
                .requestedCapacity(15)
                .requestedRoom(room)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        scheduleChangeRequestDTO = ScheduleChangeRequestDTO.builder()
                .id(1L)
                .trainingClassId(1L)
                .requestType("MODIFY")
                .reason("Need to change time")
                .className("Updated Yoga Class")
                .classDescription("yoga")
                .requestedStartTime(LocalDateTime.now().plusDays(2))
                .requestedEndTime(LocalDateTime.now().plusDays(2).plusHours(1))
                .requestedCapacity(15)
                .requestedRoomId(1L)
                .requestedRoomName("room1")
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        reviewRequest = ReviewScheduleChangeRequest.builder()
                .status("APPROVED")
                .reviewNote("Approved for scheduling conflict")
                .build();
    }

    @Test
    void getAllScheduleChangeRequests_shouldReturnAllRequests() {
        when(scheduleChangeRequestRepository.findAll()).thenReturn(List.of(scheduleChangeRequest));
        when(scheduleChangeRequestMapper.toDto(scheduleChangeRequest)).thenReturn(scheduleChangeRequestDTO);

        List<ScheduleChangeRequestDTO> result = scheduleChangeRequestService.getAllScheduleChangeRequests();

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(scheduleChangeRequestDTO);
        verify(scheduleChangeRequestRepository).findAll();
        verify(scheduleChangeRequestMapper).toDto(scheduleChangeRequest);
    }

    @Test
    void getPendingScheduleChangeRequests_shouldReturnPendingRequestsOnly() {
        when(scheduleChangeRequestRepository.findByStatus("PENDING")).thenReturn(List.of(scheduleChangeRequest));
        when(scheduleChangeRequestMapper.toDto(scheduleChangeRequest)).thenReturn(scheduleChangeRequestDTO);

        List<ScheduleChangeRequestDTO> result = scheduleChangeRequestService.getPendingScheduleChangeRequests();

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(scheduleChangeRequestDTO);
        verify(scheduleChangeRequestRepository).findByStatus("PENDING");
        verify(scheduleChangeRequestMapper).toDto(scheduleChangeRequest);
    }

    @Test
    void getScheduleChangeRequestById_whenRequestExists_shouldReturnDto() {
        when(scheduleChangeRequestRepository.findById(1L)).thenReturn(Optional.of(scheduleChangeRequest));
        when(scheduleChangeRequestMapper.toDto(scheduleChangeRequest)).thenReturn(scheduleChangeRequestDTO);

        ScheduleChangeRequestDTO result = scheduleChangeRequestService.getScheduleChangeRequestById(1L);

        assertThat(result).isEqualTo(scheduleChangeRequestDTO);
        verify(scheduleChangeRequestRepository).findById(1L);
        verify(scheduleChangeRequestMapper).toDto(scheduleChangeRequest);
    }

    @Test
    void getScheduleChangeRequestById_whenRequestNotFound_shouldThrowException() {
        when(scheduleChangeRequestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> scheduleChangeRequestService.getScheduleChangeRequestById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Schedule change request not found with id: 999");

        verify(scheduleChangeRequestRepository).findById(999L);
        verify(scheduleChangeRequestMapper, never()).toDto(any());
    }

    @Test
    void reviewScheduleChangeRequest_withApproval_shouldApproveAndUpdateClass() {
        when(scheduleChangeRequestRepository.findById(1L)).thenReturn(Optional.of(scheduleChangeRequest));
        when(userRepository.findByEmail("admin@gmail.com")).thenReturn(Optional.of(admin));
        when(scheduleChangeRequestRepository.save(any(ScheduleChangeRequest.class))).thenReturn(scheduleChangeRequest);
        when(trainingClassRepository.save(any(TrainingClass.class))).thenReturn(trainingClass);
        when(scheduleChangeRequestMapper.toDto(scheduleChangeRequest)).thenReturn(scheduleChangeRequestDTO);

        ScheduleChangeRequestDTO result = scheduleChangeRequestService.reviewScheduleChangeRequest(1L, "admin@gmail.com", reviewRequest);

        assertThat(result).isEqualTo(scheduleChangeRequestDTO);
        verify(scheduleChangeRequestRepository).findById(1L);
        verify(userRepository).findByEmail("admin@gmail.com");
        verify(scheduleChangeRequestRepository).save(argThat(r ->
                "APPROVED".equals(r.getStatus()) &&
                        admin.equals(r.getReviewedBy()) &&
                        r.getReviewedAt() != null &&
                        "Approved for scheduling conflict".equals(r.getReviewNote())
        ));
        verify(trainingClassRepository).save(any(TrainingClass.class));
    }

    @Test
    void reviewScheduleChangeRequest_withRejection_shouldRejectWithoutUpdatingClass() {
        ReviewScheduleChangeRequest rejectRequest = ReviewScheduleChangeRequest.builder()
                .status("REJECTED")
                .reviewNote("Conflicts with other classes")
                .build();

        when(scheduleChangeRequestRepository.findById(1L)).thenReturn(Optional.of(scheduleChangeRequest));
        when(userRepository.findByEmail("admin@gmail.com")).thenReturn(Optional.of(admin));
        when(scheduleChangeRequestRepository.save(any(ScheduleChangeRequest.class))).thenReturn(scheduleChangeRequest);
        when(scheduleChangeRequestMapper.toDto(scheduleChangeRequest)).thenReturn(scheduleChangeRequestDTO);

        ScheduleChangeRequestDTO result = scheduleChangeRequestService.reviewScheduleChangeRequest(1L, "admin@gmail.com", rejectRequest);

        assertThat(result).isEqualTo(scheduleChangeRequestDTO);
        verify(scheduleChangeRequestRepository).save(argThat(r -> "REJECTED".equals(r.getStatus())));
        verify(trainingClassRepository, never()).save(any());
    }

    @Test
    void reviewScheduleChangeRequest_whenRequestNotFound_shouldThrowException() {
        when(scheduleChangeRequestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> scheduleChangeRequestService.reviewScheduleChangeRequest(999L, "admin@gmail.com", reviewRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Schedule change request not found with id: 999");

        verify(scheduleChangeRequestRepository).findById(999L);
        verify(scheduleChangeRequestRepository, never()).save(any());
    }

    @Test
    void reviewScheduleChangeRequest_whenAdminNotFound_shouldThrowException() {
        when(scheduleChangeRequestRepository.findById(1L)).thenReturn(Optional.of(scheduleChangeRequest));
        when(userRepository.findByEmail("unknown@gmail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> scheduleChangeRequestService.reviewScheduleChangeRequest(1L, "unknown@gmail.com", reviewRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Admin user not found");

        verify(scheduleChangeRequestRepository, never()).save(any());
    }

    @Test
    void reviewScheduleChangeRequest_whenRequestAlreadyReviewed_shouldThrowException() {
        scheduleChangeRequest.setStatus("APPROVED");
        when(scheduleChangeRequestRepository.findById(1L)).thenReturn(Optional.of(scheduleChangeRequest));

        assertThatThrownBy(() -> scheduleChangeRequestService.reviewScheduleChangeRequest(1L, "admin@gmail.com", reviewRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Request has already been reviewed");

        verify(scheduleChangeRequestRepository, never()).save(any());
    }

    @Test
    void reviewScheduleChangeRequest_withInvalidStatus_shouldThrowException() {
        ReviewScheduleChangeRequest invalidRequest = ReviewScheduleChangeRequest.builder()
                .status("INVALID_STATUS")
                .build();

        when(scheduleChangeRequestRepository.findById(1L)).thenReturn(Optional.of(scheduleChangeRequest));
        when(userRepository.findByEmail("admin@gmail.com")).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> scheduleChangeRequestService.reviewScheduleChangeRequest(1L, "admin@gmail.com", invalidRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Status must be APPROVED or REJECTED");

        verify(scheduleChangeRequestRepository, never()).save(any());
    }

    @Test
    void reviewScheduleChangeRequest_forCancelRequest_shouldDeleteClass() {
        ScheduleChangeRequest cancelRequest = ScheduleChangeRequest.builder()
                .id(1L)
                .trainer(trainer)
                .trainingClass(trainingClass)
                .requestType("CANCEL")
                .reason("reason")
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        when(scheduleChangeRequestRepository.findById(1L)).thenReturn(Optional.of(cancelRequest));
        when(userRepository.findByEmail("admin@gmail.com")).thenReturn(Optional.of(admin));
        when(scheduleChangeRequestRepository.save(any(ScheduleChangeRequest.class))).thenReturn(cancelRequest);
        when(scheduleChangeRequestMapper.toDto(cancelRequest)).thenReturn(scheduleChangeRequestDTO);

        ScheduleChangeRequestDTO result = scheduleChangeRequestService.reviewScheduleChangeRequest(1L, "admin@gmail.com", reviewRequest);

        assertThat(result).isEqualTo(scheduleChangeRequestDTO);
        verify(trainingClassRepository).deleteById(1L);
    }

    @Test
    void reviewScheduleChangeRequest_forAddRequest_shouldCreateNewClass() {
        ScheduleChangeRequest addRequest = ScheduleChangeRequest.builder()
                .id(1L)
                .trainer(trainer)
                .requestType("ADD")
                .className("trainingClass8")
                .classDescription("desctiption")
                .requestedStartTime(LocalDateTime.now().plusDays(3))
                .requestedEndTime(LocalDateTime.now().plusDays(3).plusHours(1))
                .requestedCapacity(15)
                .requestedRoom(room)
                .reason("Need to add new class")
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        when(scheduleChangeRequestRepository.findById(1L)).thenReturn(Optional.of(addRequest));
        when(userRepository.findByEmail("admin@gmail.com")).thenReturn(Optional.of(admin));
        when(scheduleChangeRequestRepository.save(any(ScheduleChangeRequest.class))).thenReturn(addRequest);
        when(scheduleChangeRequestMapper.toDto(addRequest)).thenReturn(scheduleChangeRequestDTO);

        ScheduleChangeRequestDTO result = scheduleChangeRequestService.reviewScheduleChangeRequest(1L, "admin@gmail.com", reviewRequest);

        assertThat(result).isEqualTo(scheduleChangeRequestDTO);
        verify(trainingClassRepository).save(argThat(tc ->
                "trainingClass8".equals(tc.getName()) &&
                        trainer.equals(tc.getTrainer()) &&
                        room.equals(tc.getRoom())
        ));
    }
}