package com.naivez.fithub.unit.mapper;

import com.naivez.fithub.dto.TrainingClassDTO;
import com.naivez.fithub.dto.TrainingClassRequest;
import com.naivez.fithub.entity.Reservation;
import com.naivez.fithub.entity.Room;
import com.naivez.fithub.entity.TrainingClass;
import com.naivez.fithub.entity.User;
import com.naivez.fithub.mapper.TrainingClassMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TrainingClassMapperTest {

    private TrainingClassMapper trainingClassMapper;

    @BeforeEach
    void setUp() {
        trainingClassMapper = Mappers.getMapper(TrainingClassMapper.class);
    }

    @Test
    void toDto_whenEntityHasAllFields_shouldMapCorrectly() {
        User trainer = User.builder()
                .id(1L)
                .firstName("user1")
                .lastName("trainer1")
                .email("trainer1@gmail.com")
                .build();

        Room room = Room.builder()
                .id(2L)
                .name("room1")
                .capacity(30)
                .build();

        User client1 = User.builder().id(3L).build();
        User client2 = User.builder().id(4L).build();
        User client3 = User.builder().id(5L).build();

        Reservation reservation1 = Reservation.builder()
                .user(client1)
                .status("CONFIRMED")
                .build();
        Reservation reservation2 = Reservation.builder()
                .user(client2)
                .status("CONFIRMED")
                .build();
        Reservation reservation3 = Reservation.builder()
                .user(client3)
                .status("CANCELLED")
                .build();

        Set<Reservation> reservations = new HashSet<>();
        reservations.add(reservation1);
        reservations.add(reservation2);
        reservations.add(reservation3);

        TrainingClass trainingClass = TrainingClass.builder()
                .id(1L)
                .name("trainingClass1")
                .description("description")
                .trainer(trainer)
                .room(room)
                .startTime(LocalDateTime.of(2024, 12, 15, 9, 0))
                .endTime(LocalDateTime.of(2024, 12, 15, 10, 0))
                .capacity(20)
                .reservations(reservations)
                .build();

        TrainingClassDTO result = trainingClassMapper.toDto(trainingClass);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("trainingClass1");
        assertThat(result.getDescription()).isEqualTo("description");
        assertThat(result.getTrainerName()).isEqualTo("user1 trainer1");
        assertThat(result.getRoomName()).isEqualTo("room1");
        assertThat(result.getStartTime()).isEqualTo(LocalDateTime.of(2024, 12, 15, 9, 0));
        assertThat(result.getEndTime()).isEqualTo(LocalDateTime.of(2024, 12, 15, 10, 0));
        assertThat(result.getCapacity()).isEqualTo(20);
        assertThat(result.getAvailableSpots()).isEqualTo(18);
    }

    @Test
    void toDto_whenEntityIsNull_shouldReturnNull() {
        TrainingClassDTO result = trainingClassMapper.toDto((TrainingClass) null);

        assertThat(result).isNull();
    }

    @Test
    void toDto_whenTrainerIsNull_shouldMapWithNullTrainerName() {
        Room room = Room.builder()
                .id(1L)
                .name("room2")
                .build();

        TrainingClass trainingClass = TrainingClass.builder()
                .id(1L)
                .name("name class")
                .trainer(null)
                .room(room)
                .capacity(15)
                .reservations(new HashSet<>())
                .build();

        TrainingClassDTO result = trainingClassMapper.toDto(trainingClass);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("name class");
        assertThat(result.getTrainerName()).isNull();
        assertThat(result.getRoomName()).isEqualTo("room2");
        assertThat(result.getAvailableSpots()).isEqualTo(15);
    }

    @Test
    void toDto_whenRoomIsNull_shouldMapWithNullRoomName() {
        User trainer = User.builder()
                .firstName("user2")
                .lastName("user1")
                .build();

        TrainingClass trainingClass = TrainingClass.builder()
                .id(2L)
                .name("name class")
                .trainer(trainer)
                .room(null)
                .capacity(25)
                .reservations(new HashSet<>())
                .build();

        TrainingClassDTO result = trainingClassMapper.toDto(trainingClass);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("name class");
        assertThat(result.getTrainerName()).isEqualTo("user2 user1");
        assertThat(result.getRoomName()).isNull();
    }

    @Test
    void getTrainerName_whenTrainerHasFullName_shouldReturnFormattedName() {
        User trainer = User.builder()
                .firstName("name")
                .lastName("name1")
                .build();

        String result = trainingClassMapper.getTrainerName(trainer);

        assertThat(result).isEqualTo("name name1");
    }

    @Test
    void getTrainerName_whenTrainerIsNull_shouldReturnNull() {
        String result = trainingClassMapper.getTrainerName(null);

        assertThat(result).isNull();
    }

    @Test
    void calculateAvailableSpots_whenNoReservations_shouldReturnFullCapacity() {
        TrainingClass trainingClass = TrainingClass.builder()
                .capacity(20)
                .reservations(new HashSet<>())
                .build();

        int result = trainingClassMapper.calculateAvailableSpots(trainingClass);

        assertThat(result).isEqualTo(20);
    }

    @Test
    void calculateAvailableSpots_whenReservationsIsNull_shouldReturnFullCapacity() {
        TrainingClass trainingClass = TrainingClass.builder()
                .capacity(15)
                .reservations(null)
                .build();

        int result = trainingClassMapper.calculateAvailableSpots(trainingClass);

        assertThat(result).isEqualTo(15);
    }

    @Test
    void calculateAvailableSpots_whenOnlyConfirmedReservations_shouldSubtractFromCapacity() {
        Reservation confirmed1 = Reservation.builder().status("CONFIRMED").build();
        Reservation confirmed2 = Reservation.builder().status("CONFIRMED").build();
        Reservation cancelled = Reservation.builder().status("CANCELLED").build();
        Reservation pending = Reservation.builder().status("PENDING").build();

        Set<Reservation> reservations = Set.of(confirmed1, confirmed2, cancelled, pending);

        TrainingClass trainingClass = TrainingClass.builder()
                .capacity(25)
                .reservations(reservations)
                .build();

        int result = trainingClassMapper.calculateAvailableSpots(trainingClass);

        assertThat(result).isEqualTo(23);
    }

    @Test
    void calculateAvailableSpots_whenFullyBooked_shouldReturnZero() {
        Set<Reservation> reservations = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            reservations.add(Reservation.builder().status("CONFIRMED").build());
        }

        TrainingClass trainingClass = TrainingClass.builder()
                .capacity(10)
                .reservations(reservations)
                .build();

        int result = trainingClassMapper.calculateAvailableSpots(trainingClass);

        assertThat(result).isEqualTo(0);
    }

    @Test
    void toEntity_whenDtoHasAllFields_shouldMapCorrectly() {
        TrainingClassDTO dto = TrainingClassDTO.builder()
                .id(3L)
                .name("trainingClass6")
                .description("description")
                .trainerName("name")
                .roomName("room1")
                .startTime(LocalDateTime.of(2024, 12, 16, 18, 0))
                .endTime(LocalDateTime.of(2024, 12, 16, 19, 0))
                .capacity(15)
                .availableSpots(10)
                .build();

        TrainingClass result = trainingClassMapper.toEntity(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getName()).isEqualTo("trainingClass6");
        assertThat(result.getDescription()).isEqualTo("description");
        assertThat(result.getStartTime()).isEqualTo(LocalDateTime.of(2024, 12, 16, 18, 0));
        assertThat(result.getEndTime()).isEqualTo(LocalDateTime.of(2024, 12, 16, 19, 0));
        assertThat(result.getCapacity()).isEqualTo(15);
    }

    @Test
    void toEntity_whenRequestHasAllFields_shouldMapCorrectly() {
        TrainingClassRequest request = TrainingClassRequest.builder()
                .name("name")
                .description("test class description 3")
                .startTime(LocalDateTime.of(2024, 12, 20, 7, 0))
                .endTime(LocalDateTime.of(2024, 12, 20, 8, 0))
                .capacity(12)
                .build();

        TrainingClass result = trainingClassMapper.toEntity(request);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("name");
        assertThat(result.getDescription()).isEqualTo("test class description 3");
        assertThat(result.getStartTime()).isEqualTo(LocalDateTime.of(2024, 12, 20, 7, 0));
        assertThat(result.getEndTime()).isEqualTo(LocalDateTime.of(2024, 12, 20, 8, 0));
        assertThat(result.getCapacity()).isEqualTo(12);
        assertThat(result.getId()).isNull();
        assertThat(result.getTrainer()).isNull();
        assertThat(result.getRoom()).isNull();
        assertThat(result.getReservations()).isNull();
    }

    @Test
    void updateFromRequest_whenCalled_shouldModifyTargetEntity() {
        User existingTrainer = User.builder().id(1L).firstName("user1").build();
        Room existingRoom = Room.builder().id(2L).name("room1").build();
        Set<Reservation> existingReservations = new HashSet<>();

        TrainingClass existingClass = TrainingClass.builder()
                .id(4L)
                .name("Old Class Name")
                .description("Old description")
                .trainer(existingTrainer)
                .room(existingRoom)
                .startTime(LocalDateTime.of(2024, 12, 15, 10, 0))
                .endTime(LocalDateTime.of(2024, 12, 15, 11, 0))
                .capacity(20)
                .reservations(existingReservations)
                .build();

        TrainingClassRequest request = TrainingClassRequest.builder()
                .name("Updated Class Name")
                .description("Updated description")
                .startTime(LocalDateTime.of(2024, 12, 16, 11, 0))
                .endTime(LocalDateTime.of(2024, 12, 16, 12, 0))
                .capacity(25)
                .build();

        trainingClassMapper.updateFromRequest(request, existingClass);

        assertThat(existingClass.getId()).isEqualTo(4L);
        assertThat(existingClass.getName()).isEqualTo("Updated Class Name");
        assertThat(existingClass.getDescription()).isEqualTo("Updated description");
        assertThat(existingClass.getStartTime()).isEqualTo(LocalDateTime.of(2024, 12, 16, 11, 0));
        assertThat(existingClass.getEndTime()).isEqualTo(LocalDateTime.of(2024, 12, 16, 12, 0));
        assertThat(existingClass.getCapacity()).isEqualTo(25);
        assertThat(existingClass.getTrainer()).isEqualTo(existingTrainer);
        assertThat(existingClass.getRoom()).isEqualTo(existingRoom);
        assertThat(existingClass.getReservations()).isEqualTo(existingReservations);
    }

    @Test
    void toEntity_whenDtoIsNull_shouldReturnNull() {
        TrainingClass result = trainingClassMapper.toEntity((TrainingClassDTO) null);

        assertThat(result).isNull();
    }

    @Test
    void toEntity_whenRequestIsNull_shouldReturnNull() {
        TrainingClass result = trainingClassMapper.toEntity((TrainingClassRequest) null);

        assertThat(result).isNull();
    }
}