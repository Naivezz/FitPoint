package com.naivez.fithub.service;

import com.naivez.fithub.dto.EquipmentDTO;
import com.naivez.fithub.dto.EquipmentRequest;
import com.naivez.fithub.entity.Equipment;
import com.naivez.fithub.entity.Room;
import com.naivez.fithub.mapper.EquipmentMapper;
import com.naivez.fithub.repository.EquipmentRepository;
import com.naivez.fithub.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final RoomRepository roomRepository;
    private final EquipmentMapper equipmentMapper;

    public List<EquipmentDTO> getAllEquipment() {
        return equipmentRepository.findAll().stream()
                .map(equipmentMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<EquipmentDTO> getEquipmentByRoom(Long roomId) {
        return equipmentRepository.findByRoomId(roomId).stream()
                .map(equipmentMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<EquipmentDTO> getEquipmentByStatus(String status) {
        return equipmentRepository.findByStatus(status).stream()
                .map(equipmentMapper::toDto)
                .collect(Collectors.toList());
    }

    public EquipmentDTO getEquipmentById(Long id) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipment not found with id: " + id));
        return equipmentMapper.toDto(equipment);
    }

    @Transactional
    public EquipmentDTO createEquipment(EquipmentRequest request) {
        log.info("Creating new equipment - name: {}, roomId: {}", request.getName(), request.getRoomId());

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> {
                    log.error("Room not found for equipment creation - roomId: {}", request.getRoomId());
                    return new RuntimeException("Room not found with id: " + request.getRoomId());
                });

        Equipment equipment = equipmentMapper.toEntity(request);
        equipment.setRoom(room);

        equipment = equipmentRepository.save(equipment);
        log.info("Equipment created successfully - id: {}, name: {}, room: {}",
                equipment.getId(), equipment.getName(), room.getName());

        return equipmentMapper.toDto(equipment);
    }

    @Transactional
    public EquipmentDTO updateEquipment(Long id, EquipmentRequest request) {
        log.info("Updating equipment - id: {}, new roomId: {}", id, request.getRoomId());

        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipment not found with id: " + id));

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> {
                    log.error("Room not found for equipment update - roomId: {}", request.getRoomId());
                    return new RuntimeException("Room not found with id: " + request.getRoomId());
                });

        equipmentMapper.updateFromRequest(request, equipment);
        equipment.setRoom(room);

        equipment = equipmentRepository.save(equipment);
        log.info("Equipment updated successfully - id: {}, name: {}", equipment.getId(), equipment.getName());

        return equipmentMapper.toDto(equipment);
    }

    @Transactional
    public void deleteEquipment(Long id) {
        log.info("Deleting equipment - id: {}", id);

        if (!equipmentRepository.existsById(id)) {
            throw new RuntimeException("Equipment not found with id: " + id);
        }
        equipmentRepository.deleteById(id);
        log.info("Equipment deleted successfully - id: {}", id);
    }
}