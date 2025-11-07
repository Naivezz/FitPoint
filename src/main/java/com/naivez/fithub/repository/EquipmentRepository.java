package com.naivez.fithub.repository;

import com.naivez.fithub.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    List<Equipment> findByRoomId(Long roomId);
    List<Equipment> findByStatus(String status);
}