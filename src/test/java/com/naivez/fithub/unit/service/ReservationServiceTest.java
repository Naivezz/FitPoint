package com.naivez.fithub.unit.service;

import com.naivez.fithub.dto.RatingRequest;
import com.naivez.fithub.dto.ReservationDTO;
import com.naivez.fithub.dto.ReservationRequest;
import com.naivez.fithub.dto.TrainingClassDTO;
import com.naivez.fithub.entity.*;
import com.naivez.fithub.mapper.ReservationMapper;
import com.naivez.fithub.mapper.TrainingClassMapper;
import com.naivez.fithub.repository.ReservationRepository;
import com.naivez.fithub.repository.TrainingClassRepository;
import com.naivez.fithub.repository.UserRepository;
import com.naivez.fithub.service.ReservationService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private TrainingClassRepository trainingClassRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReservationMapper reservationMapper;

    @Mock
    private TrainingClassMapper trainingClassMapper;

    @InjectMocks
    private ReservationService reservationService;

    private User testUser;
    private TrainingClass testClass;
    private Reservation testReservation;
    private ReservationDTO testReservationDTO;
    private TrainingClassDTO testClassDTO;
    private ReservationRequest testReservationRequest;
    private RatingRequest testRatingRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("user1@gmail.com")
                .firstName("user1")
                .lastName("user1")
                .phone("1111111111")
                .roles(new HashSet<>())
                .memberships(new HashSet<>())
                .reservations(new HashSet<>())
                .notifications(new HashSet<>())
                .build();

        testClass = TrainingClass.builder()
                .id(1L)
                .name("trainingClass1")
                .description("Beginner yoga class")
                .startTime(LocalDateTime.now().plusHours(3))
                .endTime(LocalDateTime.now().plusHours(4))
                .capacity(10)
                .reservations(new HashSet<>())
                .build();

        testReservation = Reservation.builder()
                .id(1L)
                .user(testUser)
                .trainingClass(testClass)
                .reservationDate(LocalDateTime.now())
                .status("CONFIRMED")
                .build();

        testReservationDTO = ReservationDTO.builder()
                .id(1L)
                .trainingClassId(1L)
                .className("trainingClass1")
                .reservationDate(LocalDateTime.now())
                .status("CONFIRMED")
                .classStartTime(testClass.getStartTime())
                .classEndTime(testClass.getEndTime())
                .build();

        testClassDTO = TrainingClassDTO.builder()
                .id(1L)
                .name("trainingClass1")
                .description("Beginner yoga class")
                .startTime(testClass.getStartTime())
                .endTime(testClass.getEndTime())
                .capacity(10)
                .availableSpots(5)
                .build();

        testReservationRequest = ReservationRequest.builder()
                .trainingClassId(1L)
                .build();

        testRatingRequest = RatingRequest.builder()
                .rating(5)
                .comment("Great class!")
                .build();
    }

    @Test
    void getAvailableClasses_shouldReturnClassesWithAvailableSpots() {
        when(trainingClassRepository.findUpcomingClasses(any(LocalDateTime.class)))
                .thenReturn(List.of(testClass));
        when(trainingClassMapper.toDto(testClass)).thenReturn(testClassDTO);

        List<TrainingClassDTO> result = reservationService.getAvailableClasses();

        assertThat(result).hasSize(1);
        TrainingClassDTO resultClass = result.get(0);
        assertThat(resultClass.getId()).isEqualTo(1L);
        assertThat(resultClass.getName()).isEqualTo("trainingClass1");
        assertThat(resultClass.getAvailableSpots()).isEqualTo(5);

        verify(trainingClassRepository).findUpcomingClasses(any(LocalDateTime.class));
        verify(trainingClassMapper).toDto(testClass);
    }

    @Test
    void createReservation_withValidRequest_shouldReturnReservationDto() {
        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));
        when(trainingClassRepository.findById(1L)).thenReturn(Optional.of(testClass));
        when(reservationRepository.existsByUserAndTrainingClassAndStatus(testUser, testClass, "CONFIRMED"))
                .thenReturn(false);
        when(reservationRepository.countConfirmedReservationsByClassId(1L)).thenReturn(5L);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);
        when(reservationMapper.toDto(testReservation)).thenReturn(testReservationDTO);

        ReservationDTO result = reservationService.createReservation("user1@gmail.com", testReservationRequest);

        assertThat(result).isEqualTo(testReservationDTO);
        verify(userRepository).findByEmail("user1@gmail.com");
        verify(trainingClassRepository).findById(1L);
        verify(reservationRepository).existsByUserAndTrainingClassAndStatus(testUser, testClass, "CONFIRMED");
        verify(reservationRepository).countConfirmedReservationsByClassId(1L);
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void createReservation_whenUserNotFound_shouldThrowException() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.createReservation("unknown@example.com", testReservationRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findByEmail("unknown@example.com");
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservation_whenClassNotFound_shouldThrowException() {
        ReservationRequest requestWithInvalidClass = ReservationRequest.builder()
                .trainingClassId(999L)
                .build();

        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));
        when(trainingClassRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.createReservation("user1@gmail.com", requestWithInvalidClass))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Training class not found");

        verify(trainingClassRepository).findById(999L);
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservation_whenAlreadyReserved_shouldThrowException() {
        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));
        when(trainingClassRepository.findById(1L)).thenReturn(Optional.of(testClass));
        when(reservationRepository.existsByUserAndTrainingClassAndStatus(testUser, testClass, "CONFIRMED"))
                .thenReturn(true);

        assertThatThrownBy(() -> reservationService.createReservation("user1@gmail.com", testReservationRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("You already have a reservation for this class");

        verify(reservationRepository).existsByUserAndTrainingClassAndStatus(testUser, testClass, "CONFIRMED");
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservation_whenClassFullyBooked_shouldThrowException() {
        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));
        when(trainingClassRepository.findById(1L)).thenReturn(Optional.of(testClass));
        when(reservationRepository.existsByUserAndTrainingClassAndStatus(testUser, testClass, "CONFIRMED"))
                .thenReturn(false);
        when(reservationRepository.countConfirmedReservationsByClassId(1L)).thenReturn(10L);

        assertThatThrownBy(() -> reservationService.createReservation("user1@gmail.com", testReservationRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Class is fully booked");

        verify(reservationRepository).countConfirmedReservationsByClassId(1L);
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void cancelReservation_withValidRequest_shouldCancelSuccessfully() {
        testClass.setStartTime(LocalDateTime.now().plusHours(5));
        testReservation.setTrainingClass(testClass);

        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));

        reservationService.cancelReservation("user1@gmail.com", 1L);

        verify(userRepository).findByEmail("user1@gmail.com");
        verify(reservationRepository).findById(1L);
        verify(reservationRepository).save(argThat(r -> "CANCELLED".equals(r.getStatus())));
    }

    @Test
    void cancelReservation_whenReservationNotFound_shouldThrowException() {
        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.cancelReservation("user1@gmail.com", 999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Reservation not found");

        verify(reservationRepository).findById(999L);
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void cancelReservation_whenNotUsersReservation_shouldThrowException() {
        User otherUser = User.builder()
                .id(2L)
                .email("other@gmail.com")
                .build();
        testReservation.setUser(otherUser);

        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));

        assertThatThrownBy(() -> reservationService.cancelReservation("user1@gmail.com", 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("You can only cancel your own reservations");

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void cancelReservation_whenTooLateToCancel_shouldThrowException() {
        testClass.setStartTime(LocalDateTime.now().plusMinutes(90));
        testReservation.setTrainingClass(testClass);

        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));

        assertThatThrownBy(() -> reservationService.cancelReservation("user1@gmail.com", 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Reservations can only be cancelled at least 2 hours before the class starts");

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void rateClass_withValidRequest_shouldUpdateRating() {
        testClass.setEndTime(LocalDateTime.now().minusHours(1));
        testReservation.setTrainingClass(testClass);
        testClass.getReservations().add(testReservation);

        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));
        when(trainingClassRepository.findById(1L)).thenReturn(Optional.of(testClass));

        reservationService.rateClass("user1@gmail.com", 1L, testRatingRequest);

        verify(reservationRepository).save(argThat(r ->
                r.getRating() != null && r.getRating().equals(5) &&
                        "Great class!".equals(r.getComment())));
        verify(trainingClassRepository).save(any(TrainingClass.class));
    }

    @Test
    void rateClass_whenClassNotFinished_shouldThrowException() {
        testClass.setEndTime(LocalDateTime.now().plusHours(1));
        testReservation.setTrainingClass(testClass);

        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));

        assertThatThrownBy(() -> reservationService.rateClass("user1@gmail.com", 1L, testRatingRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("You can only rate classes that have already finished");

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void getMyReservations_shouldReturnUserReservations() {
        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));
        when(reservationRepository.findByUserWithTrainingClass(1L)).thenReturn(List.of(testReservation));
        when(reservationMapper.toDto(testReservation)).thenReturn(testReservationDTO);

        List<ReservationDTO> result = reservationService.getMyReservations("user1@gmail.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testReservationDTO);
        verify(userRepository).findByEmail("user1@gmail.com");
        verify(reservationRepository).findByUserWithTrainingClass(1L);
        verify(reservationMapper).toDto(testReservation);
    }

    @Test
    void getMyReservations_whenNoReservations_shouldReturnEmptyList() {
        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));
        when(reservationRepository.findByUserWithTrainingClass(1L)).thenReturn(List.of());

        List<ReservationDTO> result = reservationService.getMyReservations("user1@gmail.com");

        assertThat(result).isEmpty();
        verify(reservationRepository).findByUserWithTrainingClass(1L);
        verify(reservationMapper, never()).toDto(any());
    }

    @Test
    void getUpcomingReservations_shouldReturnOnlyUpcomingConfirmedReservations() {
        LocalDateTime now = LocalDateTime.now();

        Reservation upcomingReservation = Reservation.builder()
                .id(2L)
                .user(testUser)
                .trainingClass(testClass)
                .reservationDate(now.minusDays(1))
                .status("CONFIRMED")
                .build();

        TrainingClass pastClass = TrainingClass.builder()
                .id(2L)
                .name("Past Class")
                .startTime(now.minusHours(2))
                .endTime(now.minusHours(1))
                .capacity(10)
                .build();

        Reservation pastReservation = Reservation.builder()
                .id(3L)
                .user(testUser)
                .trainingClass(pastClass)
                .reservationDate(now.minusDays(1))
                .status("CONFIRMED")
                .build();

        Reservation cancelledReservation = Reservation.builder()
                .id(4L)
                .user(testUser)
                .trainingClass(testClass)
                .reservationDate(now.minusDays(1))
                .status("CANCELLED")
                .build();

        ReservationDTO upcomingDTO = ReservationDTO.builder()
                .id(2L)
                .trainingClassId(1L)
                .className("trainingClass1")
                .reservationDate(now.minusDays(1))
                .status("CONFIRMED")
                .classStartTime(testClass.getStartTime())
                .classEndTime(testClass.getEndTime())
                .build();

        ReservationDTO pastDTO = ReservationDTO.builder()
                .id(3L)
                .trainingClassId(2L)
                .className("Past Class")
                .reservationDate(now.minusDays(1))
                .status("CONFIRMED")
                .classStartTime(pastClass.getStartTime())
                .classEndTime(pastClass.getEndTime())
                .build();

        ReservationDTO cancelledDTO = ReservationDTO.builder()
                .id(4L)
                .trainingClassId(1L)
                .className("trainingClass1")
                .reservationDate(now.minusDays(1))
                .status("CANCELLED")
                .classStartTime(testClass.getStartTime())
                .classEndTime(testClass.getEndTime())
                .build();

        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));
        when(reservationRepository.findByUserWithTrainingClass(1L))
                .thenReturn(List.of(upcomingReservation, pastReservation, cancelledReservation));
        when(reservationMapper.toDto(upcomingReservation)).thenReturn(upcomingDTO);
        when(reservationMapper.toDto(pastReservation)).thenReturn(pastDTO);
        when(reservationMapper.toDto(cancelledReservation)).thenReturn(cancelledDTO);

        List<ReservationDTO> result = reservationService.getUpcomingReservations("user1@gmail.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(2L);
        assertThat(result.get(0).getStatus()).isEqualTo("CONFIRMED");
        assertThat(result.get(0).getClassStartTime()).isAfter(now);
    }

    @Test
    void getUpcomingReservations_whenNoUpcomingReservations_shouldReturnEmptyList() {
        LocalDateTime now = LocalDateTime.now();

        TrainingClass pastClass = TrainingClass.builder()
                .id(2L)
                .name("Past Class")
                .startTime(now.minusHours(2))
                .endTime(now.minusHours(1))
                .capacity(10)
                .build();

        Reservation pastReservation = Reservation.builder()
                .id(3L)
                .user(testUser)
                .trainingClass(pastClass)
                .reservationDate(now.minusDays(1))
                .status("CONFIRMED")
                .build();

        ReservationDTO pastDTO = ReservationDTO.builder()
                .id(3L)
                .trainingClassId(2L)
                .className("Past Class")
                .reservationDate(now.minusDays(1))
                .status("CONFIRMED")
                .classStartTime(pastClass.getStartTime())
                .classEndTime(pastClass.getEndTime())
                .build();

        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));
        when(reservationRepository.findByUserWithTrainingClass(1L))
                .thenReturn(List.of(pastReservation));
        when(reservationMapper.toDto(pastReservation)).thenReturn(pastDTO);

        List<ReservationDTO> result = reservationService.getUpcomingReservations("user1@gmail.com");

        assertThat(result).isEmpty();
    }

    @Test
    void getPastReservations_shouldReturnOnlyPastReservations() {
        LocalDateTime now = LocalDateTime.now();

        TrainingClass pastClass = TrainingClass.builder()
                .id(2L)
                .name("Past Class")
                .startTime(now.minusHours(3))
                .endTime(now.minusHours(2))
                .capacity(10)
                .build();

        Reservation pastReservation = Reservation.builder()
                .id(3L)
                .user(testUser)
                .trainingClass(pastClass)
                .reservationDate(now.minusDays(1))
                .status("CONFIRMED")
                .build();

        Reservation upcomingReservation = Reservation.builder()
                .id(2L)
                .user(testUser)
                .trainingClass(testClass)
                .reservationDate(now.minusDays(1))
                .status("CONFIRMED")
                .build();

        ReservationDTO pastDTO = ReservationDTO.builder()
                .id(3L)
                .trainingClassId(2L)
                .className("Past Class")
                .reservationDate(now.minusDays(1))
                .status("CONFIRMED")
                .classStartTime(pastClass.getStartTime())
                .classEndTime(pastClass.getEndTime())
                .build();

        ReservationDTO upcomingDTO = ReservationDTO.builder()
                .id(2L)
                .trainingClassId(1L)
                .className("trainingClass1")
                .reservationDate(now.minusDays(1))
                .status("CONFIRMED")
                .classStartTime(testClass.getStartTime())
                .classEndTime(testClass.getEndTime())
                .build();

        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));
        when(reservationRepository.findByUserWithTrainingClass(1L))
                .thenReturn(List.of(pastReservation, upcomingReservation));
        when(reservationMapper.toDto(pastReservation)).thenReturn(pastDTO);
        when(reservationMapper.toDto(upcomingReservation)).thenReturn(upcomingDTO);

        List<ReservationDTO> result = reservationService.getPastReservations("user1@gmail.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(3L);
        assertThat(result.get(0).getClassEndTime()).isBefore(now);
    }

    @Test
    void getPastReservations_whenNoPastReservations_shouldReturnEmptyList() {
        LocalDateTime now = LocalDateTime.now();

        Reservation upcomingReservation = Reservation.builder()
                .id(2L)
                .user(testUser)
                .trainingClass(testClass)
                .reservationDate(now.minusDays(1))
                .status("CONFIRMED")
                .build();

        ReservationDTO upcomingDTO = ReservationDTO.builder()
                .id(2L)
                .trainingClassId(1L)
                .className("trainingClass1")
                .reservationDate(now.minusDays(1))
                .status("CONFIRMED")
                .classStartTime(testClass.getStartTime())
                .classEndTime(testClass.getEndTime())
                .build();

        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));
        when(reservationRepository.findByUserWithTrainingClass(1L))
                .thenReturn(List.of(upcomingReservation));
        when(reservationMapper.toDto(upcomingReservation)).thenReturn(upcomingDTO);

        List<ReservationDTO> result = reservationService.getPastReservations("user1@gmail.com");

        assertThat(result).isEmpty();
    }

    @Test
    void cancelReservation_whenAlreadyCancelled_shouldThrowException() {
        testClass.setStartTime(LocalDateTime.now().plusHours(5));
        testReservation.setTrainingClass(testClass);
        testReservation.setStatus("CANCELLED");

        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));

        assertThatThrownBy(() -> reservationService.cancelReservation("user1@gmail.com", 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Reservation is already cancelled");

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void cancelReservation_whenClassAlreadyStarted_shouldThrowException() {
        testClass.setStartTime(LocalDateTime.now().minusHours(1));
        testReservation.setTrainingClass(testClass);

        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));

        assertThatThrownBy(() -> reservationService.cancelReservation("user1@gmail.com", 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot cancel a reservation for a class that has already started");

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void rateClass_whenReservationNotConfirmed_shouldThrowException() {
        testClass.setEndTime(LocalDateTime.now().minusHours(1));
        testReservation.setTrainingClass(testClass);
        testReservation.setStatus("CANCELLED");

        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));

        assertThatThrownBy(() -> reservationService.rateClass("user1@gmail.com", 1L, testRatingRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("You can only rate classes you attended");

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservation_whenClassAlreadyStarted_shouldThrowException() {
        testClass.setStartTime(LocalDateTime.now().minusHours(1));

        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));
        when(trainingClassRepository.findById(1L)).thenReturn(Optional.of(testClass));

        assertThatThrownBy(() -> reservationService.createReservation("user1@gmail.com", testReservationRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot reserve a class that has already started");

        verify(reservationRepository, never()).save(any());
    }
}