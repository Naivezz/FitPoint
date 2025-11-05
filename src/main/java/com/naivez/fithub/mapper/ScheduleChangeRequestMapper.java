package com.naivez.fithub.mapper;

import com.naivez.fithub.dto.ScheduleChangeRequestDTO;
import com.naivez.fithub.entity.ScheduleChangeRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ScheduleChangeRequestMapper {

    @Mapping(target = "trainingClassId", source = "trainingClass.id")
    @Mapping(target = "requestedRoomId", source = "requestedRoom.id")
    @Mapping(target = "requestedRoomName", source = "requestedRoom.name")
    @Mapping(target = "reviewedByName", source = "reviewedBy", qualifiedByName = "getReviewerName")
    ScheduleChangeRequestDTO toDto(ScheduleChangeRequest scheduleChangeRequest);

    ScheduleChangeRequest toEntity(ScheduleChangeRequestDTO scheduleChangeRequestDTO);

    @Named("getReviewerName")
    default String getReviewerName(com.naivez.fithub.entity.User reviewer) {
        if (reviewer == null) {
            return null;
        }
        return reviewer.getFirstName() + " " + reviewer.getLastName();
    }
}