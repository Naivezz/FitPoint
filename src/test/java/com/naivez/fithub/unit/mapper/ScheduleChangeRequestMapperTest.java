package com.naivez.fithub.unit.mapper;

import com.naivez.fithub.dto.ScheduleChangeRequestDTO;
import com.naivez.fithub.entity.Room;
import com.naivez.fithub.entity.ScheduleChangeRequest;
import com.naivez.fithub.entity.TrainingClass;
import com.naivez.fithub.entity.User;
import com.naivez.fithub.mapper.ScheduleChangeRequestMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduleChangeRequestMapperTest {

    private ScheduleChangeRequestMapper scheduleChangeRequestMapper;

    @BeforeEach
    void setUp() {
        scheduleChangeRequestMapper = Mappers.getMapper(ScheduleChangeRequestMapper.class);
    }

    @Test
    void toDto_whenEntityHasAllFields_shouldMapCorrectly() {
        User trainer = User.builder()
                .id(1L)
                .firstName("user1")
                .lastName("trainer1")
                .email("trainer1@gmail.com")
                .build();

        User admin = User.builder()
                .id(2L)
                .firstName("Admin")
                .lastName("User")
                .email("admin1@gmail.com")
                .build();

        Room currentRoom = Room.builder()
                .id(1L)
                .name("room1")
                .build();

        Room requestedRoom = Room.builder()
                .id(2L)
                .name("Room")
                .build();

        TrainingClass trainingClass = TrainingClass.builder()
                .id(3L)
                .name("trainingClass1")
                .trainer(trainer)
                .room(currentRoom)
                .build();

        ScheduleChangeRequest request = ScheduleChangeRequest.builder()
                .id(1L)
                .trainer(trainer)
                .trainingClass(trainingClass)
                .requestType("MODIFY")
                .reason("test message 3")
                .requestedStartTime(LocalDateTime.of(2024, 12, 15, 10, 0))
                .requestedEndTime(LocalDateTime.of(2024, 12, 15, 11, 0))
                .requestedRoom(requestedRoom)
                .requestedCapacity(25)
                .status("APPROVED")
                .reviewedBy(admin)
                .reviewedAt(LocalDateTime.of(2024, 12, 12, 14, 30))
                .createdAt(LocalDateTime.of(2024, 12, 10, 9, 15))
                .build();

        ScheduleChangeRequestDTO result = scheduleChangeRequestMapper.toDto(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTrainingClassId()).isEqualTo(3L);
        assertThat(result.getRequestType()).isEqualTo("MODIFY");
        assertThat(result.getReason()).isEqualTo("test message 3");
        assertThat(result.getRequestedStartTime()).isEqualTo(LocalDateTime.of(2024, 12, 15, 10, 0));
        assertThat(result.getRequestedEndTime()).isEqualTo(LocalDateTime.of(2024, 12, 15, 11, 0));
        assertThat(result.getRequestedRoomId()).isEqualTo(2L);
        assertThat(result.getRequestedRoomName()).isEqualTo("Room");
        assertThat(result.getRequestedCapacity()).isEqualTo(25);
        assertThat(result.getStatus()).isEqualTo("APPROVED");
        assertThat(result.getReviewedByName()).isEqualTo("Admin User");
        assertThat(result.getReviewedAt()).isEqualTo(LocalDateTime.of(2024, 12, 12, 14, 30));
        assertThat(result.getCreatedAt()).isEqualTo(LocalDateTime.of(2024, 12, 10, 9, 15));
    }

    @Test
    void toDto_whenEntityIsNull_shouldReturnNull() {
        ScheduleChangeRequestDTO result = scheduleChangeRequestMapper.toDto(null);

        assertThat(result).isNull();
    }

    @Test
    void toDto_whenTrainingClassIsNull_shouldMapWithNullClassId() {
        User trainer = User.builder()
                .id(1L)
                .firstName("user2")
                .lastName("trainer1")
                .build();

        ScheduleChangeRequest request = ScheduleChangeRequest.builder()
                .id(2L)
                .trainer(trainer)
                .trainingClass(null)
                .requestType("ADD")
                .reason("reason")
                .status("PENDING")
                .build();

        ScheduleChangeRequestDTO result = scheduleChangeRequestMapper.toDto(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getTrainingClassId()).isNull();
        assertThat(result.getRequestType()).isEqualTo("ADD");
        assertThat(result.getReason()).isEqualTo("reason");
        assertThat(result.getStatus()).isEqualTo("PENDING");
    }

    @Test
    void toDto_whenRequestedRoomIsNull_shouldMapWithNullRoomFields() {
        TrainingClass trainingClass = TrainingClass.builder()
                .id(4L)
                .name("trainingClass6")
                .build();

        ScheduleChangeRequest request = ScheduleChangeRequest.builder()
                .id(3L)
                .trainingClass(trainingClass)
                .requestType("CANCEL")
                .reason("Trainer unavailable")
                .requestedRoom(null)
                .status("PENDING")
                .build();

        ScheduleChangeRequestDTO result = scheduleChangeRequestMapper.toDto(request);

        assertThat(result).isNotNull();
        assertThat(result.getTrainingClassId()).isEqualTo(4L);
        assertThat(result.getRequestType()).isEqualTo("CANCEL");
        assertThat(result.getRequestedRoomId()).isNull();
        assertThat(result.getRequestedRoomName()).isNull();
    }

    @Test
    void toDto_whenReviewedByIsNull_shouldMapWithNullReviewerName() {
        ScheduleChangeRequest request = ScheduleChangeRequest.builder()
                .id(4L)
                .requestType("MODIFY")
                .reason("Schedule conflict")
                .status("PENDING")
                .reviewedBy(null)
                .reviewedAt(null)
                .build();

        ScheduleChangeRequestDTO result = scheduleChangeRequestMapper.toDto(request);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("PENDING");
        assertThat(result.getReviewedByName()).isNull();
        assertThat(result.getReviewedAt()).isNull();
    }

    @Test
    void getReviewerName_whenReviewerHasFullName_shouldReturnFormattedName() {
        User reviewer = User.builder()
                .firstName("user5")
                .lastName("Administrator")
                .build();

        String result = scheduleChangeRequestMapper.getReviewerName(reviewer);

        assertThat(result).isEqualTo("user5 Administrator");
    }

    @Test
    void getReviewerName_whenReviewerIsNull_shouldReturnNull() {
        String result = scheduleChangeRequestMapper.getReviewerName(null);

        assertThat(result).isNull();
    }

    @Test
    void toEntity_whenDtoHasAllFields_shouldMapCorrectly() {
        ScheduleChangeRequestDTO dto = ScheduleChangeRequestDTO.builder()
                .id(5L)
                .trainingClassId(6L)
                .requestType("ADD")
                .reason("reason")
                .requestedStartTime(LocalDateTime.of(2024, 12, 20, 7, 0))
                .requestedEndTime(LocalDateTime.of(2024, 12, 20, 8, 0))
                .requestedRoomId(3L)
                .requestedRoomName("room2")
                .requestedCapacity(30)
                .status("APPROVED")
                .reviewedByName("Manager Smith")
                .reviewedAt(LocalDateTime.of(2024, 12, 15, 11, 0))
                .createdAt(LocalDateTime.of(2024, 12, 14, 16, 45))
                .build();

        ScheduleChangeRequest result = scheduleChangeRequestMapper.toEntity(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getRequestType()).isEqualTo("ADD");
        assertThat(result.getReason()).isEqualTo("reason");
        assertThat(result.getRequestedStartTime()).isEqualTo(LocalDateTime.of(2024, 12, 20, 7, 0));
        assertThat(result.getRequestedEndTime()).isEqualTo(LocalDateTime.of(2024, 12, 20, 8, 0));
        assertThat(result.getRequestedCapacity()).isEqualTo(30);
        assertThat(result.getStatus()).isEqualTo("APPROVED");
        assertThat(result.getReviewedAt()).isEqualTo(LocalDateTime.of(2024, 12, 15, 11, 0));
        assertThat(result.getCreatedAt()).isEqualTo(LocalDateTime.of(2024, 12, 14, 16, 45));
    }

    @Test
    void toEntity_whenDtoIsNull_shouldReturnNull() {
        ScheduleChangeRequest result = scheduleChangeRequestMapper.toEntity((ScheduleChangeRequestDTO) null);

        assertThat(result).isNull();
    }
}