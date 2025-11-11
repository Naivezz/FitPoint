package com.naivez.fithub.unit.mapper;

import com.naivez.fithub.dto.ClassTypeDTO;
import com.naivez.fithub.entity.TrainingClass;
import com.naivez.fithub.entity.User;
import com.naivez.fithub.mapper.ClassTypeMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ClassTypeMapperTest {

    private ClassTypeMapper classTypeMapper;

    @BeforeEach
    void setUp() {
        classTypeMapper = Mappers.getMapper(ClassTypeMapper.class);
    }

    @Test
    void toClassTypeDTO_whenTrainingClassHasBasicFields_shouldMapCorrectly() {
        User trainer = User.builder()
                .id(1L)
                .firstName("user1")
                .lastName("trainer1")
                .build();

        TrainingClass trainingClass = TrainingClass.builder()
                .id(1L)
                .name("name")
                .description("description")
                .trainer(trainer)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .capacity(20)
                .build();

        ClassTypeDTO result = classTypeMapper.toClassTypeDTO(trainingClass);

        assertThat(result).isNotNull();
    }

    @Test
    void toClassTypeDTO_whenTrainingClassIsNull_shouldReturnNull() {
        ClassTypeDTO result = classTypeMapper.toClassTypeDTO(null);
        assertThat(result).isNull();
    }
}