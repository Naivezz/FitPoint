package com.naivez.fithub.unit.mapper;

import com.naivez.fithub.dto.TrainerNoteDTO;
import com.naivez.fithub.entity.TrainerNote;
import com.naivez.fithub.entity.User;
import com.naivez.fithub.mapper.TrainerNoteMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TrainerNoteMapperTest {

    private TrainerNoteMapper trainerNoteMapper;

    @BeforeEach
    void setUp() {
        trainerNoteMapper = Mappers.getMapper(TrainerNoteMapper.class);
    }

    @Test
    void toDto_whenEntityHasBasicFields_shouldMapAllFields() {
        User trainer = User.builder()
                .id(1L)
                .email("trainer1@gmail.com")
                .firstName("user1")
                .lastName("trainer1")
                .build();

        User client = User.builder()
                .id(2L)
                .email("user1@gmail.com")
                .firstName("user2")
                .lastName("user1")
                .build();

        LocalDateTime createdTime = LocalDateTime.of(2025, 10, 30, 14, 30, 0);
        LocalDateTime updatedTime = LocalDateTime.of(2025, 10, 30, 16, 45, 0);

        TrainerNote trainerNote = TrainerNote.builder()
                .id(1L)
                .trainer(trainer)
                .client(client)
                .note("test trainer note 2")
                .createdAt(createdTime)
                .updatedAt(updatedTime)
                .build();

        TrainerNoteDTO result = trainerNoteMapper.toDto(trainerNote);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getClientId()).isEqualTo(2L);
        assertThat(result.getClientName()).isEqualTo("user2 user1");
        assertThat(result.getNote()).isEqualTo("test trainer note 2");
        assertThat(result.getCreatedAt()).isEqualTo(createdTime);
        assertThat(result.getUpdatedAt()).isEqualTo(updatedTime);
    }

    @Test
    void toDto_whenEntityIsNull_shouldReturnNull() {
        TrainerNoteDTO result = trainerNoteMapper.toDto((TrainerNote) null);

        assertThat(result).isNull();
    }

    @Test
    void toDto_whenClientIsNull_shouldMapOtherFieldsWithNullClientInfo() {
        User trainer = User.builder()
                .id(1L)
                .email("trainer1@gmail.com")
                .build();

        TrainerNote trainerNote = TrainerNote.builder()
                .id(2L)
                .trainer(trainer)
                .client(null)
                .note("training notes")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        TrainerNoteDTO result = trainerNoteMapper.toDto(trainerNote);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getClientId()).isNull();
        assertThat(result.getClientName()).isNull();
        assertThat(result.getNote()).isEqualTo("training notes");
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
    }

    @Test
    void toEntity_whenDtoHasAllFields_shouldMapCorrectly() {
        LocalDateTime createdTime = LocalDateTime.of(2025, 11, 1, 10, 0, 0);
        LocalDateTime updatedTime = LocalDateTime.of(2025, 11, 1, 12, 30, 0);

        TrainerNoteDTO dto = TrainerNoteDTO.builder()
                .id(1L)
                .clientId(2L)
                .clientName("user1")
                .note("Great progress")
                .createdAt(createdTime)
                .updatedAt(updatedTime)
                .build();

        TrainerNote result = trainerNoteMapper.toEntity(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNote()).isEqualTo("Great progress");
        assertThat(result.getCreatedAt()).isEqualTo(createdTime);
        assertThat(result.getUpdatedAt()).isEqualTo(updatedTime);
        assertThat(result.getTrainer()).isNull();
        assertThat(result.getClient()).isNull();
    }

    @Test
    void toEntity_whenDtoIsNull_shouldReturnNull() {
        TrainerNote result = trainerNoteMapper.toEntity((TrainerNoteDTO) null);

        assertThat(result).isNull();
    }

    @Test
    void getClientName_whenClientHasFullName_shouldReturnFullName() {
        User client = User.builder()
                .firstName("Alexander")
                .lastName("Petrov")
                .build();

        String result = trainerNoteMapper.getClientName(client);

        assertThat(result).isEqualTo("Alexander Petrov");
    }

    @Test
    void getClientName_whenClientIsNull_shouldReturnNull() {
        String result = trainerNoteMapper.getClientName(null);

        assertThat(result).isNull();
    }
}