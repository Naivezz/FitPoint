package com.naivez.fithub.mapper;

import com.naivez.fithub.dto.TrainingClassDTO;
import com.naivez.fithub.entity.TrainingClass;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TrainingClassMapper {

    @Mapping(target = "trainerName", source = "trainer", qualifiedByName = "getTrainerName")
    @Mapping(target = "roomName", source = "room.name")
    @Mapping(target = "availableSpots", source = ".", qualifiedByName = "calculateAvailableSpots")
    TrainingClassDTO toDto(TrainingClass trainingClass);

    TrainingClass toEntity(TrainingClassDTO trainingClassDTO);

    @Named("getTrainerName")
    default String getTrainerName(com.naivez.fithub.entity.User trainer) {
        if (trainer == null) {
            return null;
        }
        return trainer.getFirstName() + " " + trainer.getLastName();
    }

    @Named("calculateAvailableSpots")
    default int calculateAvailableSpots(TrainingClass trainingClass) {
        int reservedSpots = trainingClass.getReservations() != null ?
                (int) trainingClass.getReservations().stream()
                        .filter(r -> "CONFIRMED".equals(r.getStatus()))
                        .count() : 0;
        return trainingClass.getCapacity() - reservedSpots;
    }
}
