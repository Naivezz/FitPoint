package com.naivez.fithub.mapper;

import com.naivez.fithub.dto.ReservationDTO;
import com.naivez.fithub.entity.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReservationMapper {

    @Mapping(target = "trainingClassId", source = "trainingClass.id")
    @Mapping(target = "className", source = "trainingClass.name")
    @Mapping(target = "trainerName", source = "trainingClass.trainer", qualifiedByName = "getTrainerName")
    @Mapping(target = "classStartTime", source = "trainingClass.startTime")
    @Mapping(target = "classEndTime", source = "trainingClass.endTime")
    ReservationDTO toDto(Reservation reservation);

    Reservation toEntity(ReservationDTO reservationDTO);

    @Named("getTrainerName")
    default String getTrainerName(com.naivez.fithub.entity.User trainer) {
        if (trainer == null) {
            return null;
        }
        return trainer.getFirstName() + " " + trainer.getLastName();
    }
}
