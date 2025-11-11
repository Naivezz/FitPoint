package com.naivez.fithub.unit.service;

import com.naivez.fithub.dto.RoomDTO;
import com.naivez.fithub.dto.RoomRequest;
import com.naivez.fithub.entity.Room;
import com.naivez.fithub.mapper.RoomMapper;
import com.naivez.fithub.repository.RoomRepository;
import com.naivez.fithub.service.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomMapper roomMapper;

    @InjectMocks
    private RoomService roomService;

    private Room testRoom;
    private RoomDTO testRoomDTO;
    private RoomRequest testRoomRequest;

    @BeforeEach
    void setUp() {
        testRoom = Room.builder()
                .id(1L)
                .name("Test Room")
                .capacity(20)
                .equipmentList(new HashSet<>())
                .classes(new HashSet<>())
                .build();

        testRoomDTO = RoomDTO.builder()
                .id(1L)
                .name("Test Room")
                .capacity(20)
                .equipmentList(List.of())
                .build();

        testRoomRequest = RoomRequest.builder()
                .name("Test Room")
                .capacity(20)
                .build();
    }

    @Test
    void getAllRooms_shouldReturnListOfRoomDtos() {
        Room room2 = Room.builder()
                .id(2L)
                .name("room2")
                .capacity(15)
                .equipmentList(new HashSet<>())
                .classes(new HashSet<>())
                .build();

        RoomDTO roomDTO2 = RoomDTO.builder()
                .id(2L)
                .name("room2")
                .capacity(15)
                .equipmentList(List.of())
                .build();

        when(roomRepository.findAll()).thenReturn(List.of(testRoom, room2));
        when(roomMapper.toDto(testRoom)).thenReturn(testRoomDTO);
        when(roomMapper.toDto(room2)).thenReturn(roomDTO2);

        List<RoomDTO> result = roomService.getAllRooms();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).contains(testRoomDTO, roomDTO2);
        verify(roomRepository).findAll();
        verify(roomMapper, times(2)).toDto(any(Room.class));
    }

    @Test
    void getAllRooms_whenNoRooms_shouldReturnEmptyList() {
        when(roomRepository.findAll()).thenReturn(List.of());

        List<RoomDTO> result = roomService.getAllRooms();

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(roomRepository).findAll();
        verify(roomMapper, never()).toDto(any(Room.class));
    }

    @Test
    void getRoomById_whenRoomExists_shouldReturnRoomDto() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(roomMapper.toDto(testRoom)).thenReturn(testRoomDTO);

        RoomDTO result = roomService.getRoomById(1L);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(testRoomDTO);
        verify(roomRepository).findById(1L);
        verify(roomMapper).toDto(testRoom);
    }

    @Test
    void getRoomById_whenRoomNotFound_shouldThrowException() {
        when(roomRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.getRoomById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Room not found with id: 999");

        verify(roomRepository).findById(999L);
        verify(roomMapper, never()).toDto(any(Room.class));
    }

    @Test
    void createRoom_withValidRequest_shouldReturnRoomDto() {
        when(roomMapper.toEntity(testRoomRequest)).thenReturn(testRoom);
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);
        when(roomMapper.toDto(testRoom)).thenReturn(testRoomDTO);

        RoomDTO result = roomService.createRoom(testRoomRequest);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(testRoomDTO);
        verify(roomMapper).toEntity(testRoomRequest);
        verify(roomRepository).save(any(Room.class));
        verify(roomMapper).toDto(testRoom);
    }

    @Test
    void updateRoom_whenRoomExists_shouldReturnUpdatedRoomDto() {
        RoomRequest updateRequest = RoomRequest.builder()
                .name("Updated Room")
                .capacity(25)
                .build();

        Room updatedRoom = Room.builder()
                .id(1L)
                .name("Updated Room")
                .capacity(25)
                .equipmentList(new HashSet<>())
                .classes(new HashSet<>())
                .build();

        RoomDTO updatedRoomDTO = RoomDTO.builder()
                .id(1L)
                .name("Updated Room")
                .capacity(25)
                .equipmentList(List.of())
                .build();

        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        doNothing().when(roomMapper).updateFromRequest(eq(updateRequest), any(Room.class));
        when(roomRepository.save(any(Room.class))).thenReturn(updatedRoom);
        when(roomMapper.toDto(updatedRoom)).thenReturn(updatedRoomDTO);

        RoomDTO result = roomService.updateRoom(1L, updateRequest);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Room");
        assertThat(result.getCapacity()).isEqualTo(25);
        verify(roomRepository).findById(1L);
        verify(roomMapper).updateFromRequest(eq(updateRequest), any(Room.class));
        verify(roomRepository).save(any(Room.class));
        verify(roomMapper).toDto(updatedRoom);
    }

    @Test
    void updateRoom_whenRoomNotFound_shouldThrowException() {
        when(roomRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.updateRoom(999L, testRoomRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Room not found with id: 999");

        verify(roomRepository).findById(999L);
        verify(roomMapper, never()).updateFromRequest(any(), any());
        verify(roomRepository, never()).save(any());
    }

    @Test
    void deleteRoom_whenRoomExists_shouldDeleteSuccessfully() {
        when(roomRepository.existsById(1L)).thenReturn(true);
        doNothing().when(roomRepository).deleteById(1L);

        roomService.deleteRoom(1L);

        verify(roomRepository).existsById(1L);
        verify(roomRepository).deleteById(1L);
    }

    @Test
    void deleteRoom_whenRoomNotFound_shouldThrowException() {
        when(roomRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> roomService.deleteRoom(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Room not found with id: 999");

        verify(roomRepository).existsById(999L);
        verify(roomRepository, never()).deleteById(anyLong());
    }
}