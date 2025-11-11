package com.naivez.fithub.unit.mapper;

import com.naivez.fithub.dto.PersonalTrainingSessionDTO;
import com.naivez.fithub.entity.PersonalTrainingSession;
import com.naivez.fithub.entity.User;
import com.naivez.fithub.mapper.PersonalTrainingSessionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PersonalTrainingSessionMapperTest {

    private PersonalTrainingSessionMapper personalTrainingSessionMapper;

    @BeforeEach
    void setUp() {
        personalTrainingSessionMapper = Mappers.getMapper(PersonalTrainingSessionMapper.class);
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

        PersonalTrainingSession session = PersonalTrainingSession.builder()
                .id(1L)
                .trainer(trainer)
                .client(client)
                .startTime(LocalDateTime.of(2024, 12, 15, 10, 0))
                .endTime(LocalDateTime.of(2024, 12, 15, 11, 0))
                .sessionGoal("test session goal 4")
                .sessionNotes("Great progress")
                .status("COMPLETED")
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        PersonalTrainingSessionDTO result = personalTrainingSessionMapper.toDto(session);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getClientId()).isEqualTo(2L);
        assertThat(result.getClientName()).isEqualTo("user2 user1");
        assertThat(result.getStartTime()).isEqualTo(LocalDateTime.of(2024, 12, 15, 10, 0));
        assertThat(result.getEndTime()).isEqualTo(LocalDateTime.of(2024, 12, 15, 11, 0));
        assertThat(result.getSessionGoal()).isEqualTo("test session goal 4");
        assertThat(result.getSessionNotes()).isEqualTo("Great progress");
        assertThat(result.getStatus()).isEqualTo("COMPLETED");
        assertThat(result.getCreatedAt()).isNotNull();
    }

    @Test
    void toDto_whenEntityIsNull_shouldReturnNull() {
        PersonalTrainingSessionDTO result = personalTrainingSessionMapper.toDto(null);

        assertThat(result).isNull();
    }

    @Test
    void toDto_whenClientIsNull_shouldMapWithNullClientFields() {
        User trainer = User.builder()
                .id(1L)
                .firstName("user1")
                .lastName("trainer1")
                .build();

        PersonalTrainingSession session = PersonalTrainingSession.builder()
                .id(1L)
                .trainer(trainer)
                .client(null)
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusHours(2))
                .status("SCHEDULED")
                .build();

        PersonalTrainingSessionDTO result = personalTrainingSessionMapper.toDto(session);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getClientId()).isNull();
        assertThat(result.getClientName()).isNull();
        assertThat(result.getStatus()).isEqualTo("SCHEDULED");
    }

    @Test
    void getClientName_whenClientHasFullName_shouldReturnFormattedName() {
        User client = User.builder()
                .firstName("user1")
                .lastName("user1")
                .build();

        String result = personalTrainingSessionMapper.getClientName(client);

        assertThat(result).isEqualTo("user1 user1");
    }

    @Test
    void toEntity_whenDtoHasAllFields_shouldMapCorrectly() {
        PersonalTrainingSessionDTO dto = PersonalTrainingSessionDTO.builder()
                .id(1L)
                .clientId(2L)
                .clientName("user1")
                .startTime(LocalDateTime.of(2024, 12, 15, 14, 0))
                .endTime(LocalDateTime.of(2024, 12, 15, 15, 0))
                .sessionGoal("test session goal 6")
                .sessionNotes("Good")
                .status("COMPLETED")
                .createdAt(LocalDateTime.now())
                .build();

        PersonalTrainingSession result = personalTrainingSessionMapper.toEntity(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStartTime()).isEqualTo(LocalDateTime.of(2024, 12, 15, 14, 0));
        assertThat(result.getEndTime()).isEqualTo(LocalDateTime.of(2024, 12, 15, 15, 0));
        assertThat(result.getSessionGoal()).isEqualTo("test session goal 6");
        assertThat(result.getSessionNotes()).isEqualTo("Good");
        assertThat(result.getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    void toEntity_whenDtoIsNull_shouldReturnNull() {
        PersonalTrainingSession result = personalTrainingSessionMapper.toEntity((PersonalTrainingSessionDTO) null);

        assertThat(result).isNull();
    }
}