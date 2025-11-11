package com.naivez.fithub.unit.mapper;

import com.naivez.fithub.dto.EquipmentDTO;
import com.naivez.fithub.dto.RoomDTO;
import com.naivez.fithub.dto.RoomRequest;
import com.naivez.fithub.entity.Equipment;
import com.naivez.fithub.entity.Room;
import com.naivez.fithub.mapper.RoomMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RoomMapperTest {

    private RoomMapper roomMapper;

    @BeforeEach
    void setUp() {
        roomMapper = Mappers.getMapper(RoomMapper.class);
    }

    @Test
    void toDto_whenEntityHasBasicFields_shouldMapAllFields() {
        Room room = Room.builder()
                .id(1L)
                .name("room1")
                .capacity(20)
                .equipmentList(new HashSet<>())
                .classes(new HashSet<>())
                .build();

        RoomDTO result = roomMapper.toDto(room);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("room1");
        assertThat(result.getCapacity()).isEqualTo(20);
    }

    @Test
    void toDto_whenEntityIsNull_shouldReturnNull() {
        RoomDTO result = roomMapper.toDto((Room) null);

        assertThat(result).isNull();
    }

    @Test
    void toEntity_whenDtoHasAllFields_shouldMapCorrectly() {
        RoomDTO dto = RoomDTO.builder()
                .id(1L)
                .name("room2")
                .capacity(30)
                .equipmentList(List.of())
                .build();

        Room result = roomMapper.toEntity(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("room2");
        assertThat(result.getCapacity()).isEqualTo(30);
    }

    @Test
    void toEntity_whenDtoIsNull_shouldReturnNull() {
        Room result = roomMapper.toEntity((RoomDTO) null);

        assertThat(result).isNull();
    }

    @Test
    void toEntity_whenRequestHasAllFields_shouldMapCorrectly() {
        RoomRequest request = RoomRequest.builder()
                .name("Room")
                .capacity(15)
                .build();

        Room result = roomMapper.toEntity(request);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Room");
        assertThat(result.getCapacity()).isEqualTo(15);
        assertThat(result.getId()).isNull();
    }

    @Test
    void toEntity_whenRequestIsNull_shouldReturnNull() {
        Room result = roomMapper.toEntity((RoomRequest) null);

        assertThat(result).isNull();
    }

    @Test
    void toDtoList_whenListHasMultipleEntities_shouldMapAll() {
        Room room1 = Room.builder()
                .id(1L)
                .name("room1")
                .capacity(10)
                .equipmentList(new HashSet<>())
                .classes(new HashSet<>())
                .build();

        Room room2 = Room.builder()
                .id(2L)
                .name("room2")
                .capacity(20)
                .equipmentList(new HashSet<>())
                .classes(new HashSet<>())
                .build();

        List<Room> rooms = List.of(room1, room2);

        List<RoomDTO> result = roomMapper.toDtoList(rooms);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("room1");
        assertThat(result.get(1).getName()).isEqualTo("room2");
    }

    @Test
    void toDtoList_whenListIsNull_shouldReturnNull() {
        List<RoomDTO> result = roomMapper.toDtoList(null);

        assertThat(result).isNull();
    }

    @Test
    void updateFromRequest_whenCalled_shouldModifyTargetEntity() {
        Room existingRoom = Room.builder()
                .id(1L)
                .name("Old Name")
                .capacity(10)
                .equipmentList(new HashSet<>())
                .classes(new HashSet<>())
                .build();

        RoomRequest request = RoomRequest.builder()
                .name("New Name")
                .capacity(25)
                .build();

        roomMapper.updateFromRequest(request, existingRoom);

        assertThat(existingRoom.getId()).isEqualTo(1L);
        assertThat(existingRoom.getName()).isEqualTo("New Name");
        assertThat(existingRoom.getCapacity()).isEqualTo(25);
    }
}