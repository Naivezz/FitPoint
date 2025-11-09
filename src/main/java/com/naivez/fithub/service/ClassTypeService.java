package com.naivez.fithub.service;

import com.naivez.fithub.dto.ClassTypeDTO;
import com.naivez.fithub.entity.TrainingClass;
import com.naivez.fithub.mapper.ClassTypeMapper;
import com.naivez.fithub.repository.TrainingClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClassTypeService {

    private final TrainingClassRepository trainingClassRepository;
    private final ClassTypeMapper classTypeMapper;

    public List<ClassTypeDTO> getAllClassTypes() {
        List<TrainingClass> classes = trainingClassRepository.findAll();
        return classes.stream()
                .map(classTypeMapper::toClassTypeDTO)
                .collect(Collectors.groupingBy(ClassTypeDTO::getName))
                .values().stream()
                .map(list -> list.get(0))
                .collect(Collectors.toList());
    }
}