package com.naivez.fithub.unit.service;

import com.naivez.fithub.dto.ClassTypeDTO;
import com.naivez.fithub.entity.TrainingClass;
import com.naivez.fithub.mapper.ClassTypeMapper;
import com.naivez.fithub.repository.TrainingClassRepository;
import com.naivez.fithub.service.ClassTypeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClassTypeServiceTest {

    @Mock
    private TrainingClassRepository trainingClassRepository;

    @Mock
    private ClassTypeMapper classTypeMapper;

    @InjectMocks
    private ClassTypeService classTypeService;

    private TrainingClass yogaClass1;
    private TrainingClass yogaClass2;
    private TrainingClass pilatesClass;
    private ClassTypeDTO yogaClassTypeDTO;
    private ClassTypeDTO pilatesClassTypeDTO;

    @BeforeEach
    void setUp() {
        yogaClass1 = TrainingClass.builder()
                .id(1L)
                .name("classType1")
                .description("trainingClass1")
                .startTime(LocalDateTime.of(2024, 1, 15, 9, 0))
                .endTime(LocalDateTime.of(2024, 1, 15, 10, 0))
                .capacity(20)
                .build();

        yogaClass2 = TrainingClass.builder()
                .id(2L)
                .name("classType1")
                .description("trainingClass2")
                .startTime(LocalDateTime.of(2024, 1, 16, 18, 0))
                .endTime(LocalDateTime.of(2024, 1, 16, 19, 0))
                .capacity(15)
                .build();

        pilatesClass = TrainingClass.builder()
                .id(3L)
                .name("classType2")
                .description("trainingClass7")
                .startTime(LocalDateTime.of(2024, 1, 15, 10, 0))
                .endTime(LocalDateTime.of(2024, 1, 15, 11, 0))
                .capacity(12)
                .build();

        yogaClassTypeDTO = ClassTypeDTO.builder()
                .name("classType1")
                .description("trainingClass1")
                .build();

        pilatesClassTypeDTO = ClassTypeDTO.builder()
                .name("classType2")
                .description("trainingClass7")
                .build();
    }

    @Test
    void getAllClassTypes_whenMultipleClassesWithSameName_shouldReturnUniqueClassTypes() {
        ClassTypeDTO yogaClassTypeDTO2 = ClassTypeDTO.builder()
                .name("classType1")
                .description("trainingClass2")
                .build();

        when(trainingClassRepository.findAll()).thenReturn(List.of(yogaClass1, yogaClass2, pilatesClass));
        when(classTypeMapper.toClassTypeDTO(yogaClass1)).thenReturn(yogaClassTypeDTO);
        when(classTypeMapper.toClassTypeDTO(yogaClass2)).thenReturn(yogaClassTypeDTO2);
        when(classTypeMapper.toClassTypeDTO(pilatesClass)).thenReturn(pilatesClassTypeDTO);

        List<ClassTypeDTO> result = classTypeService.getAllClassTypes();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ClassTypeDTO::getName)
                .containsExactlyInAnyOrder("classType1", "classType2");

        verify(trainingClassRepository).findAll();
        verify(classTypeMapper, times(3)).toClassTypeDTO(any(TrainingClass.class));
    }

    @Test
    void getAllClassTypes_whenNoClasses_shouldReturnEmptyList() {
        when(trainingClassRepository.findAll()).thenReturn(List.of());

        List<ClassTypeDTO> result = classTypeService.getAllClassTypes();

        assertThat(result).isEmpty();
        verify(trainingClassRepository).findAll();
        verify(classTypeMapper, never()).toClassTypeDTO(any(TrainingClass.class));
    }

    @Test
    void getAllClassTypes_whenSingleClass_shouldReturnSingleClassType() {
        when(trainingClassRepository.findAll()).thenReturn(List.of(yogaClass1));
        when(classTypeMapper.toClassTypeDTO(yogaClass1)).thenReturn(yogaClassTypeDTO);

        List<ClassTypeDTO> result = classTypeService.getAllClassTypes();

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(yogaClassTypeDTO);
        assertThat(result.get(0).getName()).isEqualTo("classType1");

        verify(trainingClassRepository).findAll();
        verify(classTypeMapper).toClassTypeDTO(yogaClass1);
    }

    @Test
    void getAllClassTypes_whenClassNamesAreEmpty_shouldHandleCorrectly() {
        TrainingClass emptyNameClass = TrainingClass.builder()
                .id(8L)
                .name("")
                .description("Class with empty name")
                .startTime(LocalDateTime.of(2024, 1, 22, 15, 0))
                .endTime(LocalDateTime.of(2024, 1, 22, 16, 0))
                .capacity(3)
                .build();

        ClassTypeDTO emptyNameClassTypeDTO = ClassTypeDTO.builder()
                .name("")
                .description("Class with empty name")
                .build();

        when(trainingClassRepository.findAll()).thenReturn(List.of(emptyNameClass));
        when(classTypeMapper.toClassTypeDTO(emptyNameClass)).thenReturn(emptyNameClassTypeDTO);

        List<ClassTypeDTO> result = classTypeService.getAllClassTypes();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEmpty();
        assertThat(result.get(0).getDescription()).isEqualTo("Class with empty name");

        verify(trainingClassRepository).findAll();
        verify(classTypeMapper).toClassTypeDTO(emptyNameClass);
    }
}