package com.naivez.fithub.unit.mapper;

import com.naivez.fithub.dto.ReservationDTO;
import com.naivez.fithub.entity.Reservation;
import com.naivez.fithub.entity.TrainingClass;
import com.naivez.fithub.entity.User;
import com.naivez.fithub.mapper.ReservationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationMapperTest {

    private ReservationMapper reservationMapper;

    @BeforeEach
    void setUp() {
        reservationMapper = Mappers.getMapper(ReservationMapper.class);
    }

    @Test
    void toDto_whenEntityHasAllFields_shouldMapCorrectly() {
        User trainer = User.builder()
                .id(1L)
                .firstName("user1")
                .lastName("trainer1")
                .email("trainer1@gmail.com")
                .build();

        User client = User.builder()
                .id(2L)
                .firstName("user2")
                .lastName("user1")
                .email("user1@gmail.com")
                .build();

        TrainingClass trainingClass = TrainingClass.builder()
                .id(3L)
                .name("trainingClass1")
                .trainer(trainer)
                .startTime(LocalDateTime.of(2024, 12, 15, 9, 0))
                .endTime(LocalDateTime.of(2024, 12, 15, 10, 0))
                .capacity(20)
                .build();

        Reservation reservation = Reservation.builder()
                .id(1L)
                .user(client)
                .trainingClass(trainingClass)
                .reservationDate(LocalDateTime.of(2024, 12, 10, 14, 30))
                .status("CONFIRMED")
                .rating(5)
                .comment("Excellent")
                .build();

        ReservationDTO result = reservationMapper.toDto(reservation);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTrainingClassId()).isEqualTo(3L);
        assertThat(result.getClassName()).isEqualTo("trainingClass1");
        assertThat(result.getTrainerName()).isEqualTo("user1 trainer1");
        assertThat(result.getClassStartTime()).isEqualTo(LocalDateTime.of(2024, 12, 15, 9, 0));
        assertThat(result.getClassEndTime()).isEqualTo(LocalDateTime.of(2024, 12, 15, 10, 0));
        assertThat(result.getReservationDate()).isEqualTo(LocalDateTime.of(2024, 12, 10, 14, 30));
        assertThat(result.getStatus()).isEqualTo("CONFIRMED");
        assertThat(result.getRating()).isEqualTo(5);
        assertThat(result.getComment()).isEqualTo("Excellent");
    }

    @Test
    void toDto_whenEntityIsNull_shouldReturnNull() {
        ReservationDTO result = reservationMapper.toDto(null);

        assertThat(result).isNull();
    }

    @Test
    void toDto_whenTrainingClassIsNull_shouldMapWithNullClassFields() {
        User client = User.builder()
                .id(2L)
                .firstName("user2")
                .lastName("user1")
                .build();

        Reservation reservation = Reservation.builder()
                .id(1L)
                .user(client)
                .trainingClass(null)
                .reservationDate(LocalDateTime.now())
                .status("CANCELLED")
                .build();

        ReservationDTO result = reservationMapper.toDto(reservation);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTrainingClassId()).isNull();
        assertThat(result.getClassName()).isNull();
        assertThat(result.getTrainerName()).isNull();
        assertThat(result.getClassStartTime()).isNull();
        assertThat(result.getClassEndTime()).isNull();
        assertThat(result.getStatus()).isEqualTo("CANCELLED");
    }

    @Test
    void toDto_whenTrainerIsNull_shouldMapWithNullTrainerName() {
        TrainingClass trainingClass = TrainingClass.builder()
                .id(3L)
                .name("Class")
                .trainer(null)
                .startTime(LocalDateTime.of(2024, 12, 15, 12, 0))
                .endTime(LocalDateTime.of(2024, 12, 15, 13, 0))
                .build();

        Reservation reservation = Reservation.builder()
                .id(1L)
                .trainingClass(trainingClass)
                .status("CONFIRMED")
                .build();

        ReservationDTO result = reservationMapper.toDto(reservation);

        assertThat(result).isNotNull();
        assertThat(result.getTrainingClassId()).isEqualTo(3L);
        assertThat(result.getClassName()).isEqualTo("Class");
        assertThat(result.getTrainerName()).isNull();
        assertThat(result.getClassStartTime()).isEqualTo(LocalDateTime.of(2024, 12, 15, 12, 0));
        assertThat(result.getClassEndTime()).isEqualTo(LocalDateTime.of(2024, 12, 15, 13, 0));
    }

    @Test
    void getTrainerName_whenTrainerHasFullName_shouldReturnFormattedName() {
        User trainer = User.builder()
                .firstName("name")
                .lastName("name1")
                .build();

        String result = reservationMapper.getTrainerName(trainer);

        assertThat(result).isEqualTo("name name1");
    }

    @Test
    void getTrainerName_whenTrainerIsNull_shouldReturnNull() {
        String result = reservationMapper.getTrainerName(null);

        assertThat(result).isNull();
    }

    @Test
    void getTrainerName_whenTrainerHasOnlyFirstName_shouldReturnFormattedName() {
        User trainer = User.builder()
                .firstName("Alex")
                .lastName(null)
                .build();

        String result = reservationMapper.getTrainerName(trainer);

        assertThat(result).isEqualTo("Alex null");
    }

    @Test
    void getTrainerName_whenTrainerHasEmptyNames_shouldReturnEmptyString() {
        User trainer = User.builder()
                .firstName("")
                .lastName("")
                .build();

        String result = reservationMapper.getTrainerName(trainer);

        assertThat(result).isEqualTo(" ");
    }

    @Test
    void toEntity_whenDtoHasAllFields_shouldMapCorrectly() {
        ReservationDTO dto = ReservationDTO.builder()
                .id(1L)
                .trainingClassId(3L)
                .className("trainingClass6")
                .trainerName("name")
                .classStartTime(LocalDateTime.of(2024, 12, 16, 18, 0))
                .classEndTime(LocalDateTime.of(2024, 12, 16, 19, 0))
                .reservationDate(LocalDateTime.of(2024, 12, 12, 10, 15))
                .status("CONFIRMED")
                .rating(4)
                .comment("Good workout")
                .build();

        Reservation result = reservationMapper.toEntity(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getReservationDate()).isEqualTo(LocalDateTime.of(2024, 12, 12, 10, 15));
        assertThat(result.getStatus()).isEqualTo("CONFIRMED");
        assertThat(result.getRating()).isEqualTo(4);
        assertThat(result.getComment()).isEqualTo("Good workout");
    }

    @Test
    void toEntity_whenDtoIsNull_shouldReturnNull() {
        Reservation result = reservationMapper.toEntity((ReservationDTO) null);

        assertThat(result).isNull();
    }
}