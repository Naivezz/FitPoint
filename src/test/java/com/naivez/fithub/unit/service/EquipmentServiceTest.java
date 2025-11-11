package com.naivez.fithub.unit.service;

import com.naivez.fithub.dto.EquipmentDTO;
import com.naivez.fithub.dto.EquipmentRequest;
import com.naivez.fithub.entity.Equipment;
import com.naivez.fithub.entity.Room;
import com.naivez.fithub.mapper.EquipmentMapper;
import com.naivez.fithub.repository.EquipmentRepository;
import com.naivez.fithub.repository.RoomRepository;
import com.naivez.fithub.service.EquipmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EquipmentServiceTest {

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private EquipmentMapper equipmentMapper;

    @InjectMocks
    private EquipmentService equipmentService;

    private Equipment testEquipment;
    private EquipmentDTO testEquipmentDTO;
    private EquipmentRequest testEquipmentRequest;
    private Room testRoom;

    @BeforeEach
    void setUp() {
        testRoom = Room.builder()
                .id(1L)
                .name("room1")
                .capacity(20)
                .build();

        testEquipment = Equipment.builder()
                .id(1L)
                .name("equipment2")
                .quantity(10)
                .status("AVAILABLE")
                .room(testRoom)
                .build();

        testEquipmentDTO = EquipmentDTO.builder()
                .id(1L)
                .name("equipment2")
                .quantity(10)
                .status("AVAILABLE")
                .roomId(1L)
                .roomName("room1")
                .build();

        testEquipmentRequest = EquipmentRequest.builder()
                .name("equipment2")
                .quantity(10)
                .status("AVAILABLE")
                .roomId(1L)
                .build();
    }

    @Test
    void getAllEquipment_shouldReturnListOfEquipmentDtos() {
        Equipment equipment2 = Equipment.builder()
                .id(2L)
                .name("equipment3")
                .quantity(20)
                .status("AVAILABLE")
                .room(testRoom)
                .build();

        EquipmentDTO equipmentDTO2 = EquipmentDTO.builder()
                .id(2L)
                .name("equipment3")
                .quantity(20)
                .status("AVAILABLE")
                .roomId(1L)
                .roomName("room1")
                .build();

        when(equipmentRepository.findAll()).thenReturn(List.of(testEquipment, equipment2));
        when(equipmentMapper.toDto(testEquipment)).thenReturn(testEquipmentDTO);
        when(equipmentMapper.toDto(equipment2)).thenReturn(equipmentDTO2);

        List<EquipmentDTO> result = equipmentService.getAllEquipment();

        assertThat(result).hasSize(2);
        assertThat(result).contains(testEquipmentDTO, equipmentDTO2);
        verify(equipmentRepository).findAll();
        verify(equipmentMapper, times(2)).toDto(any(Equipment.class));
    }

    @Test
    void getAllEquipment_whenNoEquipment_shouldReturnEmptyList() {
        when(equipmentRepository.findAll()).thenReturn(List.of());

        List<EquipmentDTO> result = equipmentService.getAllEquipment();

        assertThat(result).isEmpty();
        verify(equipmentRepository).findAll();
    }

    @Test
    void getEquipmentByRoom_shouldReturnEquipmentInRoom() {
        when(equipmentRepository.findByRoomId(1L)).thenReturn(List.of(testEquipment));
        when(equipmentMapper.toDto(testEquipment)).thenReturn(testEquipmentDTO);

        List<EquipmentDTO> result = equipmentService.getEquipmentByRoom(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testEquipmentDTO);
        verify(equipmentRepository).findByRoomId(1L);
        verify(equipmentMapper).toDto(testEquipment);
    }

    @Test
    void getEquipmentByStatus_shouldReturnEquipmentWithStatus() {
        when(equipmentRepository.findByStatus("AVAILABLE")).thenReturn(List.of(testEquipment));
        when(equipmentMapper.toDto(testEquipment)).thenReturn(testEquipmentDTO);

        List<EquipmentDTO> result = equipmentService.getEquipmentByStatus("AVAILABLE");

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testEquipmentDTO);
        verify(equipmentRepository).findByStatus("AVAILABLE");
    }

    @Test
    void getEquipmentById_whenEquipmentExists_shouldReturnEquipmentDto() {
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(testEquipment));
        when(equipmentMapper.toDto(testEquipment)).thenReturn(testEquipmentDTO);

        EquipmentDTO result = equipmentService.getEquipmentById(1L);

        assertThat(result).isEqualTo(testEquipmentDTO);
        verify(equipmentRepository).findById(1L);
        verify(equipmentMapper).toDto(testEquipment);
    }

    @Test
    void getEquipmentById_whenEquipmentNotFound_shouldThrowException() {
        when(equipmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> equipmentService.getEquipmentById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Equipment not found with id: 999");

        verify(equipmentRepository).findById(999L);
        verify(equipmentMapper, never()).toDto(any());
    }

    @Test
    void createEquipment_withValidRequest_shouldReturnEquipmentDto() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(equipmentMapper.toEntity(testEquipmentRequest)).thenReturn(testEquipment);
        when(equipmentRepository.save(any(Equipment.class))).thenReturn(testEquipment);
        when(equipmentMapper.toDto(testEquipment)).thenReturn(testEquipmentDTO);

        EquipmentDTO result = equipmentService.createEquipment(testEquipmentRequest);

        assertThat(result).isEqualTo(testEquipmentDTO);
        verify(roomRepository).findById(1L);
        verify(equipmentMapper).toEntity(testEquipmentRequest);
        verify(equipmentRepository).save(any(Equipment.class));
        verify(equipmentMapper).toDto(testEquipment);
    }

    @Test
    void createEquipment_whenRoomNotFound_shouldThrowException() {
        when(roomRepository.findById(999L)).thenReturn(Optional.empty());
        testEquipmentRequest.setRoomId(999L);

        assertThatThrownBy(() -> equipmentService.createEquipment(testEquipmentRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Room not found with id: 999");

        verify(roomRepository).findById(999L);
        verify(equipmentRepository, never()).save(any());
    }

    @Test
    void updateEquipment_whenEquipmentAndRoomExist_shouldReturnUpdatedDto() {
        EquipmentRequest updateRequest = EquipmentRequest.builder()
                .name("Updated Equipment")
                .quantity(15)
                .status("AVAILABLE")
                .roomId(1L)
                .build();

        Equipment updatedEquipment = Equipment.builder()
                .id(1L)
                .name("Updated Equipment")
                .quantity(15)
                .status("AVAILABLE")
                .room(testRoom)
                .build();

        EquipmentDTO updatedDTO = EquipmentDTO.builder()
                .id(1L)
                .name("Updated Equipment")
                .quantity(15)
                .status("AVAILABLE")
                .roomId(1L)
                .roomName("room1")
                .build();

        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(testEquipment));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        doNothing().when(equipmentMapper).updateFromRequest(eq(updateRequest), any(Equipment.class));
        when(equipmentRepository.save(any(Equipment.class))).thenReturn(updatedEquipment);
        when(equipmentMapper.toDto(updatedEquipment)).thenReturn(updatedDTO);

        EquipmentDTO result = equipmentService.updateEquipment(1L, updateRequest);

        assertThat(result).isEqualTo(updatedDTO);
        verify(equipmentRepository).findById(1L);
        verify(roomRepository).findById(1L);
        verify(equipmentMapper).updateFromRequest(eq(updateRequest), any(Equipment.class));
        verify(equipmentRepository).save(any(Equipment.class));
    }

    @Test
    void updateEquipment_whenEquipmentNotFound_shouldThrowException() {
        when(equipmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> equipmentService.updateEquipment(999L, testEquipmentRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Equipment not found with id: 999");

        verify(equipmentRepository).findById(999L);
        verify(equipmentRepository, never()).save(any());
    }

    @Test
    void updateEquipment_whenRoomNotFound_shouldThrowException() {
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(testEquipment));
        when(roomRepository.findById(999L)).thenReturn(Optional.empty());
        testEquipmentRequest.setRoomId(999L);

        assertThatThrownBy(() -> equipmentService.updateEquipment(1L, testEquipmentRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Room not found with id: 999");

        verify(roomRepository).findById(999L);
        verify(equipmentRepository, never()).save(any());
    }

    @Test
    void deleteEquipment_whenEquipmentExists_shouldDeleteSuccessfully() {
        when(equipmentRepository.existsById(1L)).thenReturn(true);
        doNothing().when(equipmentRepository).deleteById(1L);

        equipmentService.deleteEquipment(1L);

        verify(equipmentRepository).existsById(1L);
        verify(equipmentRepository).deleteById(1L);
    }

    @Test
    void deleteEquipment_whenEquipmentNotFound_shouldThrowException() {
        when(equipmentRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> equipmentService.deleteEquipment(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Equipment not found with id: 999");

        verify(equipmentRepository).existsById(999L);
        verify(equipmentRepository, never()).deleteById(anyLong());
    }
}