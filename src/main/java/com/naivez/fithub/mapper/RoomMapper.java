package com.naivez.fithub.mapper;

import com.naivez.fithub.dto.RoomDTO;
import com.naivez.fithub.dto.RoomRequest;
import com.naivez.fithub.entity.Room;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {EquipmentMapper.class})
public interface RoomMapper {

    @Mapping(target = "equipmentList", source = "equipmentList")
    RoomDTO toDto(Room room);

    Room toEntity(RoomDTO roomDTO);

    List<RoomDTO> toDtoList(List<Room> rooms);

    Room toEntity(RoomRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromRequest(RoomRequest request, @MappingTarget Room room);
}
