package com.naivez.fithub.mapper;

import com.naivez.fithub.dto.TrainerNoteDTO;
import com.naivez.fithub.entity.TrainerNote;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TrainerNoteMapper {

    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "clientName", source = "client", qualifiedByName = "getClientName")
    TrainerNoteDTO toDto(TrainerNote trainerNote);

    TrainerNote toEntity(TrainerNoteDTO trainerNoteDTO);

    @Named("getClientName")
    default String getClientName(com.naivez.fithub.entity.User client) {
        if (client == null) {
            return null;
        }
        return client.getFirstName() + " " + client.getLastName();
    }
}