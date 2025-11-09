package com.naivez.fithub.service;

import com.naivez.fithub.dto.RoomDTO;
import com.naivez.fithub.dto.RoomRequest;
import com.naivez.fithub.entity.Room;
import com.naivez.fithub.mapper.RoomMapper;
import com.naivez.fithub.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

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
        Room room = roomMapper.toEntity(request);
        if (room.getEquipmentList() == null) room.setEquipmentList(new HashSet<>());
        if (room.getClasses() == null) room.setClasses(new HashSet<>());
        room = roomRepository.save(room);
        return roomMapper.toDto(room);
    }

    @Transactional
    public RoomDTO updateRoom(Long id, RoomRequest request) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));

        roomMapper.updateFromRequest(request, room);

        room = roomRepository.save(room);
        return roomMapper.toDto(room);
    }

    @Transactional
    public void deleteRoom(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new RuntimeException("Room not found with id: " + id);
        }
        roomRepository.deleteById(id);
    }
}