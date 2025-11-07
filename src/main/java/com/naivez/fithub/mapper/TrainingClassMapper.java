package com.naivez.fithub.mapper;

import com.naivez.fithub.dto.TrainingClassDTO;
import com.naivez.fithub.dto.TrainingClassRequest;
import com.naivez.fithub.entity.TrainingClass;
import com.naivez.fithub.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TrainingClassMapper {

    @Mapping(target = "trainerName", source = "trainer", qualifiedByName = "getTrainerName")
    @Mapping(target = "roomName", source = "room.name")
    @Mapping(target = "availableSpots", source = ".", qualifiedByName = "calculateAvailableSpots")
    TrainingClassDTO toDto(TrainingClass trainingClass);

    TrainingClass toEntity(TrainingClassDTO trainingClassDTO);

    @Mapping(target = "trainer", ignore = true)
    @Mapping(target = "room", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    TrainingClass toEntity(TrainingClassRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "trainer", ignore = true)
    @Mapping(target = "room", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    void updateFromRequest(TrainingClassRequest request, @MappingTarget TrainingClass trainingClass);

    @Named("getTrainerName")
    default String getTrainerName(User trainer) {
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
