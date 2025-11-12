package com.naivez.fithub.service;

import com.naivez.fithub.dto.RoomDTO;
import com.naivez.fithub.dto.RoomRequest;
import com.naivez.fithub.entity.Room;
import com.naivez.fithub.mapper.RoomMapper;
import com.naivez.fithub.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;

    public List<RoomDTO> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(roomMapper::toDto)
                .collect(Collectors.toList());
    }

    public RoomDTO getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));
        return roomMapper.toDto(room);
    }

    @Transactional
    public RoomDTO createRoom(RoomRequest request) {
        log.info("Creating new room - name: {}, capacity: {}", request.getName(), request.getCapacity());

        Room room = roomMapper.toEntity(request);

        if (room.getEquipmentList() == null) {
            room.setEquipmentList(new HashSet<>());
        }

        if (room.getClasses() == null) {
            room.setClasses(new HashSet<>());
        }
        room = roomRepository.save(room);
        log.info("Room created successfully - id: {}, name: {}", room.getId(), room.getName());

        return roomMapper.toDto(room);
    }

    @Transactional
    public RoomDTO updateRoom(Long id, RoomRequest request) {
        log.info("Updating room - id: {}, new name: {}", id, request.getName());

        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));

        roomMapper.updateFromRequest(request, room);

        room = roomRepository.save(room);
        log.info("Room updated successfully - id: {}, name: {}", id, room.getName());

        return roomMapper.toDto(room);
    }

    @Transactional
    public void deleteRoom(Long id) {
        log.info("Deleting room - id: {}", id);

        if (!roomRepository.existsById(id)) {
            throw new RuntimeException("Room not found with id: " + id);
        }
        roomRepository.deleteById(id);
        log.info("Room deleted successfully - id: {}", id);
    }
}