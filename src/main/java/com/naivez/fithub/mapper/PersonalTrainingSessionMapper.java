package com.naivez.fithub.mapper;

import com.naivez.fithub.dto.PersonalTrainingSessionDTO;
import com.naivez.fithub.entity.PersonalTrainingSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PersonalTrainingSessionMapper {

    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "clientName", source = "client", qualifiedByName = "getClientName")
    PersonalTrainingSessionDTO toDto(PersonalTrainingSession personalTrainingSession);

    PersonalTrainingSession toEntity(PersonalTrainingSessionDTO personalTrainingSessionDTO);

    @Named("getClientName")
    default String getClientName(com.naivez.fithub.entity.User client) {
        if (client == null) {
            return null;
        }
        return client.getFirstName() + " " + client.getLastName();
    }
}