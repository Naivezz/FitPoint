package com.naivez.fithub.unit.service;

import com.naivez.fithub.dto.TrainingClassDTO;
import com.naivez.fithub.dto.TrainingClassRequest;
import com.naivez.fithub.entity.Role;
import com.naivez.fithub.entity.Room;
import com.naivez.fithub.entity.TrainingClass;
import com.naivez.fithub.entity.User;
import com.naivez.fithub.mapper.TrainingClassMapper;
import com.naivez.fithub.repository.RoomRepository;
import com.naivez.fithub.repository.TrainingClassRepository;
import com.naivez.fithub.repository.UserRepository;
import com.naivez.fithub.service.TrainingClassService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingClassServiceTest {

    @Mock
    private TrainingClassRepository trainingClassRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private TrainingClassMapper trainingClassMapper;

    @InjectMocks
    private TrainingClassService trainingClassService;

    private User testTrainer;
    private User testRegularUser;
    private Room testRoom;
    private TrainingClass testTrainingClass;
    private TrainingClassDTO testTrainingClassDTO;
    private TrainingClassRequest testTrainingClassRequest;

    @BeforeEach
    void setUp() {
        Role trainerRole = Role.builder().name("ROLE_TRAINER").build();
        Set<Role> trainerRoles = new HashSet<>();
        trainerRoles.add(trainerRole);

        testTrainer = User.builder()
                .id(1L)
                .email("trainer1@gmail.com")
                .firstName("user1")
                .lastName("trainer1")
                .roles(trainerRoles)
                .build();

        Role userRole = Role.builder().name("ROLE_USER").build();
        Set<Role> userRoles = new HashSet<>();
        userRoles.add(userRole);

        testRegularUser = User.builder()
                .id(2L)
                .email("user@gmail.com")
                .firstName("user2")
                .lastName("User")
                .roles(userRoles)
                .build();

        testRoom = Room.builder()
                .id(1L)
                .name("room1")
                .capacity(20)
                .build();

        testTrainingClass = TrainingClass.builder()
                .id(1L)
                .name("trainingClass1")
                .description("yoga class")
                .startTime(LocalDateTime.now().plusDays(1).withHour(9).withMinute(0))
                .endTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0))
                .capacity(15)
                .trainer(testTrainer)
                .room(testRoom)
                .reservations(new HashSet<>())
                .build();

        testTrainingClassDTO = TrainingClassDTO.builder()
                .id(1L)
                .name("trainingClass1")
                .description("yoga class")
                .startTime(testTrainingClass.getStartTime())
                .endTime(testTrainingClass.getEndTime())
                .capacity(15)
                .trainerName("trainer1")
                .roomName("room1")
                .availableSpots(15)
                .build();

        testTrainingClassRequest = TrainingClassRequest.builder()
                .name("trainingClass1")
                .description("yoga class")
                .startTime(testTrainingClass.getStartTime())
                .endTime(testTrainingClass.getEndTime())
                .capacity(15)
                .trainerId(1L)
                .roomId(1L)
                .build();
    }

    @Test
    void getAllTrainingClasses_shouldReturnAllClasses() {
        TrainingClass class2 = TrainingClass.builder()
                .id(2L)
                .name("trainingClass6")
                .startTime(LocalDateTime.now().plusDays(1).withHour(18).withMinute(0))
                .endTime(LocalDateTime.now().plusDays(1).withHour(19).withMinute(0))
                .capacity(10)
                .trainer(testTrainer)
                .room(testRoom)
                .build();

        TrainingClassDTO class2DTO = TrainingClassDTO.builder()
                .id(2L)
                .name("trainingClass6")
                .startTime(class2.getStartTime())
                .endTime(class2.getEndTime())
                .capacity(10)
                .trainerName("trainer1")
                .roomName("room1")
                .availableSpots(10)
                .build();

        when(trainingClassRepository.findAll()).thenReturn(List.of(testTrainingClass, class2));
        when(trainingClassMapper.toDto(testTrainingClass)).thenReturn(testTrainingClassDTO);
        when(trainingClassMapper.toDto(class2)).thenReturn(class2DTO);

        List<TrainingClassDTO> result = trainingClassService.getAllTrainingClasses();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("trainingClass1");
        assertThat(result.get(1).getName()).isEqualTo("trainingClass6");
        verify(trainingClassRepository).findAll();
        verify(trainingClassMapper, times(2)).toDto(any(TrainingClass.class));
    }

    @Test
    void getUpcomingClasses_shouldReturnUpcomingClassesOnly() {
        TrainingClass pastClass = TrainingClass.builder()
                .id(2L)
                .name("Past Class")
                .startTime(LocalDateTime.now().minusHours(2))
                .endTime(LocalDateTime.now().minusHours(1))
                .capacity(10)
                .trainer(testTrainer)
                .room(testRoom)
                .build();

        when(trainingClassRepository.findUpcomingClasses(any(LocalDateTime.class)))
                .thenReturn(List.of(testTrainingClass));
        when(trainingClassMapper.toDto(testTrainingClass)).thenReturn(testTrainingClassDTO);

        List<TrainingClassDTO> result = trainingClassService.getUpcomingClasses();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("trainingClass1");
        verify(trainingClassRepository).findUpcomingClasses(any(LocalDateTime.class));
        verify(trainingClassMapper).toDto(testTrainingClass);
    }

    @Test
    void getClassesBetween_shouldReturnClassesInDateRange() {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(0).withMinute(0);
        LocalDateTime end = LocalDateTime.now().plusDays(2).withHour(23).withMinute(59);

        when(trainingClassRepository.findClassesBetween(start, end))
                .thenReturn(List.of(testTrainingClass));
        when(trainingClassMapper.toDto(testTrainingClass)).thenReturn(testTrainingClassDTO);

        List<TrainingClassDTO> result = trainingClassService.getClassesBetween(start, end);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("trainingClass1");
        verify(trainingClassRepository).findClassesBetween(start, end);
        verify(trainingClassMapper).toDto(testTrainingClass);
    }

    @Test
    void getDailySchedule_withTrainerEmail_shouldReturnDailyClasses() {
        LocalDate date = LocalDate.now().plusDays(1);
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);

        when(userRepository.findByEmail("trainer1@gmail.com")).thenReturn(Optional.of(testTrainer));
        when(trainingClassRepository.findByTrainerIdAndDateRange(eq(1L), eq(start), eq(end)))
                .thenReturn(List.of(testTrainingClass));
        when(trainingClassMapper.toDto(testTrainingClass)).thenReturn(testTrainingClassDTO);

        List<TrainingClassDTO> result = trainingClassService.getDailySchedule("trainer1@gmail.com", date);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("trainingClass1");
        verify(userRepository).findByEmail("trainer1@gmail.com");
        verify(trainingClassRepository).findByTrainerIdAndDateRange(eq(1L), eq(start), eq(end));
        verify(trainingClassMapper).toDto(testTrainingClass);
    }

    @Test
    void getDailySchedule_whenUserNotFound_shouldThrowException() {
        LocalDate date = LocalDate.now().plusDays(1);
        when(userRepository.findByEmail("unknown@gmail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingClassService.getDailySchedule("unknown@gmail.com", date))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findByEmail("unknown@gmail.com");
        verify(trainingClassRepository, never()).findByTrainerIdAndDateRange(anyLong(), any(), any());
    }

    @Test
    void getDailySchedule_whenUserNotTrainer_shouldThrowException() {
        LocalDate date = LocalDate.now().plusDays(1);
        when(userRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(testRegularUser));

        assertThatThrownBy(() -> trainingClassService.getDailySchedule("user@gmail.com", date))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User is not a trainer");

        verify(userRepository).findByEmail("user@gmail.com");
        verify(trainingClassRepository, never()).findByTrainerIdAndDateRange(anyLong(), any(), any());
    }

    @Test
    void getWeeklySchedule_withTrainerEmail_shouldReturnWeeklyClasses() {
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = startDate.plusDays(6);
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        TrainingClass class2 = TrainingClass.builder()
                .id(2L)
                .name("Yoga")
                .startTime(startDate.plusDays(2).atTime(10, 0))
                .endTime(startDate.plusDays(2).atTime(11, 0))
                .capacity(12)
                .trainer(testTrainer)
                .room(testRoom)
                .build();

        TrainingClassDTO class2DTO = TrainingClassDTO.builder()
                .id(2L)
                .name("Yoga")
                .startTime(class2.getStartTime())
                .endTime(class2.getEndTime())
                .capacity(12)
                .trainerName("trainer1")
                .roomName("room1")
                .availableSpots(12)
                .build();

        when(userRepository.findByEmail("trainer1@gmail.com")).thenReturn(Optional.of(testTrainer));
        when(trainingClassRepository.findByTrainerIdAndDateRange(eq(1L), eq(start), eq(end)))
                .thenReturn(List.of(testTrainingClass, class2));
        when(trainingClassMapper.toDto(testTrainingClass)).thenReturn(testTrainingClassDTO);
        when(trainingClassMapper.toDto(class2)).thenReturn(class2DTO);

        List<TrainingClassDTO> result = trainingClassService.getWeeklySchedule("trainer1@gmail.com", startDate);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("trainingClass1");
        assertThat(result.get(1).getName()).isEqualTo("Yoga");
        verify(userRepository).findByEmail("trainer1@gmail.com");
        verify(trainingClassRepository).findByTrainerIdAndDateRange(eq(1L), eq(start), eq(end));
        verify(trainingClassMapper, times(2)).toDto(any(TrainingClass.class));
    }

    @Test
    void createTrainingClass_withValidRequest_shouldReturnCreatedClass() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testTrainer));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(trainingClassMapper.toEntity(testTrainingClassRequest)).thenReturn(testTrainingClass);
        when(trainingClassRepository.save(testTrainingClass)).thenReturn(testTrainingClass);
        when(trainingClassMapper.toDto(testTrainingClass)).thenReturn(testTrainingClassDTO);

        TrainingClassDTO result = trainingClassService.createTrainingClass(testTrainingClassRequest);

        assertThat(result).isEqualTo(testTrainingClassDTO);
        verify(userRepository).findById(1L);
        verify(roomRepository).findById(1L);
        verify(trainingClassMapper).toEntity(testTrainingClassRequest);
        verify(trainingClassRepository).save(testTrainingClass);
        verify(trainingClassMapper).toDto(testTrainingClass);
    }

    @Test
    void createTrainingClass_whenEndTimeBeforeStartTime_shouldThrowException() {
        TrainingClassRequest invalidRequest = TrainingClassRequest.builder()
                .name("Invalid Class")
                .startTime(LocalDateTime.now().plusHours(2))
                .endTime(LocalDateTime.now().plusHours(1))
                .capacity(10)
                .trainerId(1L)
                .roomId(1L)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testTrainer));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));

        assertThatThrownBy(() -> trainingClassService.createTrainingClass(invalidRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("End time must be after start time");

        verify(userRepository).findById(1L);
        verify(roomRepository).findById(1L);
        verify(trainingClassRepository, never()).save(any());
    }

    @Test
    void updateTrainingClass_withValidRequest_shouldReturnUpdatedClass() {
        TrainingClassRequest updateRequest = TrainingClassRequest.builder()
                .name("Class")
                .description("class")
                .startTime(LocalDateTime.now().plusDays(2).withHour(9).withMinute(0))
                .endTime(LocalDateTime.now().plusDays(2).withHour(10).withMinute(0))
                .capacity(20)
                .trainerId(1L)
                .roomId(1L)
                .build();

        TrainingClass updatedClass = TrainingClass.builder()
                .id(1L)
                .name("Class")
                .description("class")
                .startTime(updateRequest.getStartTime())
                .endTime(updateRequest.getEndTime())
                .capacity(20)
                .trainer(testTrainer)
                .room(testRoom)
                .reservations(new HashSet<>())
                .build();

        TrainingClassDTO updatedDTO = TrainingClassDTO.builder()
                .id(1L)
                .name("Class")
                .description("class")
                .startTime(updateRequest.getStartTime())
                .endTime(updateRequest.getEndTime())
                .capacity(20)
                .trainerName("trainer1")
                .roomName("room1")
                .availableSpots(20)
                .build();

        when(trainingClassRepository.findById(1L)).thenReturn(Optional.of(testTrainingClass));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testTrainer));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(trainingClassRepository.save(testTrainingClass)).thenReturn(updatedClass);
        when(trainingClassMapper.toDto(updatedClass)).thenReturn(updatedDTO);

        TrainingClassDTO result = trainingClassService.updateTrainingClass(1L, updateRequest);

        assertThat(result).isEqualTo(updatedDTO);
        verify(trainingClassRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(roomRepository).findById(1L);
        verify(trainingClassMapper).updateFromRequest(updateRequest, testTrainingClass);
        verify(trainingClassRepository).save(testTrainingClass);
        verify(trainingClassMapper).toDto(updatedClass);
    }

    @Test
    void updateTrainingClass_whenClassNotFound_shouldThrowException() {
        when(trainingClassRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingClassService.updateTrainingClass(999L, testTrainingClassRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Training class not found");

        verify(trainingClassRepository).findById(999L);
        verify(userRepository, never()).findById(anyLong());
        verify(trainingClassRepository, never()).save(any());
    }

    @Test
    void deleteTrainingClass_withExistingId_shouldDeleteClass() {
        when(trainingClassRepository.existsById(1L)).thenReturn(true);

        trainingClassService.deleteTrainingClass(1L);

        verify(trainingClassRepository).existsById(1L);
        verify(trainingClassRepository).deleteById(1L);
    }

    @Test
    void deleteTrainingClass_whenClassNotFound_shouldThrowException() {
        when(trainingClassRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> trainingClassService.deleteTrainingClass(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Training class not found");

        verify(trainingClassRepository).existsById(999L);
        verify(trainingClassRepository, never()).deleteById(anyLong());
    }
}