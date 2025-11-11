package com.naivez.fithub.unit.mapper;

import com.naivez.fithub.dto.EquipmentDTO;
import com.naivez.fithub.dto.EquipmentRequest;
import com.naivez.fithub.entity.Equipment;
import com.naivez.fithub.entity.Room;
import com.naivez.fithub.mapper.EquipmentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class EquipmentMapperTest {

    private EquipmentMapper equipmentMapper;

    @BeforeEach
    void setUp() {
        equipmentMapper = Mappers.getMapper(EquipmentMapper.class);
    }

    @Test
    void toDto_whenEntityHasBasicFields_shouldMapAllFields() {
        Room room = Room.builder()
                .id(1L)
                .name("room1")
                .build();

        Equipment equipment = Equipment.builder()
                .id(1L)
                .name("equipment2")
                .quantity(10)
                .status("AVAILABLE")
                .room(room)
                .build();

        EquipmentDTO result = equipmentMapper.toDto(equipment);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("equipment2");
        assertThat(result.getQuantity()).isEqualTo(10);
        assertThat(result.getStatus()).isEqualTo("AVAILABLE");
        assertThat(result.getRoomId()).isEqualTo(1L);
        assertThat(result.getRoomName()).isEqualTo("room1");
    }

    @Test
    void toDto_whenEntityIsNull_shouldReturnNull() {
        EquipmentDTO result = equipmentMapper.toDto((Equipment) null);

        assertThat(result).isNull();
    }

    @Test
    void toEntity_whenDtoHasAllFields_shouldMapCorrectly() {
        EquipmentDTO dto = EquipmentDTO.builder()
                .id(1L)
                .name("equipment1")
                .quantity(5)
                .status("AVAILABLE")
                .roomId(2L)
                .roomName("room2")
                .build();

        Equipment result = equipmentMapper.toEntity(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("equipment1");
        assertThat(result.getQuantity()).isEqualTo(5);
        assertThat(result.getStatus()).isEqualTo("AVAILABLE");
    }

    @Test
    void toEntity_whenDtoIsNull_shouldReturnNull() {
        Equipment result = equipmentMapper.toEntity((EquipmentDTO) null);

        assertThat(result).isNull();
    }

    @Test
    void toEntity_whenRequestHasAllFields_shouldMapCorrectly() {
        EquipmentRequest request = EquipmentRequest.builder()
                .name("equipment3")
                .quantity(20)
                .status("AVAILABLE")
                .roomId(3L)
                .build();

        Equipment result = equipmentMapper.toEntity(request);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("equipment3");
        assertThat(result.getQuantity()).isEqualTo(20);
        assertThat(result.getStatus()).isEqualTo("AVAILABLE");
        assertThat(result.getId()).isNull();
        assertThat(result.getRoom()).isNull();
    }

    @Test
    void updateFromRequest_whenCalled_shouldModifyTargetEntity() {
        Room room = Room.builder()
                .id(1L)
                .name("room1")
                .build();

        Equipment existingEquipment = Equipment.builder()
                .id(1L)
                .name("equipment1")
                .quantity(5)
                .status("Broken")
                .room(room)
                .build();

        EquipmentRequest request = EquipmentRequest.builder()
                .name("Updated Equipment")
                .quantity(10)
                .status("AVAILABLE")
                .roomId(2L)
                .build();

        equipmentMapper.updateFromRequest(request, existingEquipment);

        assertThat(existingEquipment.getId()).isEqualTo(1L);
        assertThat(existingEquipment.getName()).isEqualTo("Updated Equipment");
        assertThat(existingEquipment.getQuantity()).isEqualTo(10);
        assertThat(existingEquipment.getStatus()).isEqualTo("AVAILABLE");
        assertThat(existingEquipment.getRoom()).isEqualTo(room);
    }
}