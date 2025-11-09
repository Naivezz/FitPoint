package com.naivez.fithub.controller;

import com.naivez.fithub.dto.EquipmentDTO;
import com.naivez.fithub.dto.EquipmentRequest;
import com.naivez.fithub.service.EquipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipment")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    public ResponseEntity<List<EquipmentDTO>> getAllEquipment() {
        List<EquipmentDTO> equipment = equipmentService.getAllEquipment();
        return ResponseEntity.ok(equipment);
    }

    @GetMapping("/room/{roomId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    public ResponseEntity<List<EquipmentDTO>> getEquipmentByRoom(@PathVariable Long roomId) {
        List<EquipmentDTO> equipment = equipmentService.getEquipmentByRoom(roomId);
        return ResponseEntity.ok(equipment);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    public ResponseEntity<List<EquipmentDTO>> getEquipmentByStatus(@PathVariable String status) {
        List<EquipmentDTO> equipment = equipmentService.getEquipmentByStatus(status);
        return ResponseEntity.ok(equipment);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    public ResponseEntity<EquipmentDTO> getEquipmentById(@PathVariable Long id) {
        try {
            EquipmentDTO equipment = equipmentService.getEquipmentById(id);
            return ResponseEntity.ok(equipment);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EquipmentDTO> createEquipment(@Valid @RequestBody EquipmentRequest request) {
        try {
            EquipmentDTO equipment = equipmentService.createEquipment(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(equipment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EquipmentDTO> updateEquipment(@PathVariable Long id,
                                                        @Valid @RequestBody EquipmentRequest request) {
        try {
            EquipmentDTO equipment = equipmentService.updateEquipment(id, request);
            return ResponseEntity.ok(equipment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEquipment(@PathVariable Long id) {
        try {
            equipmentService.deleteEquipment(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}