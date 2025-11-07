package com.naivez.fithub.mapper;

import com.naivez.fithub.dto.EquipmentDTO;
import com.naivez.fithub.dto.EquipmentRequest;
import com.naivez.fithub.entity.Equipment;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EquipmentMapper {

    @Mapping(target = "roomId", source = "room.id")
    @Mapping(target = "roomName", source = "room.name")
    EquipmentDTO toDto(Equipment equipment);

    Equipment toEntity(EquipmentDTO equipmentDTO);

    @Mapping(target = "room", ignore = true)
    Equipment toEntity(EquipmentRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "room", ignore = true)
    void updateFromRequest(EquipmentRequest request, @MappingTarget Equipment equipment);
}
