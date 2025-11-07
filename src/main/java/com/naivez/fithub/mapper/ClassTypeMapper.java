package com.naivez.fithub.mapper;

import com.naivez.fithub.dto.ClassTypeDTO;
import com.naivez.fithub.entity.TrainingClass;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClassTypeMapper {

    ClassTypeDTO toClassTypeDTO(TrainingClass trainingClass);
}
