package com.naivez.fithub.controller;

import com.naivez.fithub.dto.*;
import com.naivez.fithub.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/class-types")
    public ResponseEntity<List<ClassTypeDTO>> getAllClassTypes() {
        List<ClassTypeDTO> classTypes = adminService.getAllClassTypes();
        return ResponseEntity.ok(classTypes);
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<RoomDTO>> getAllRooms() {
        List<RoomDTO> rooms = adminService.getAllRooms();
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/rooms/{id}")
    public ResponseEntity<RoomDTO> getRoomById(@PathVariable Long id) {
        try {
            RoomDTO room = adminService.getRoomById(id);
            return ResponseEntity.ok(room);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/rooms")
    public ResponseEntity<RoomDTO> createRoom(@Valid @RequestBody RoomRequest request) {
        try {
            RoomDTO room = adminService.createRoom(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(room);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/rooms/{id}")
    public ResponseEntity<RoomDTO> updateRoom(@PathVariable Long id, @Valid @RequestBody RoomRequest request) {
        try {
            RoomDTO room = adminService.updateRoom(id, request);
            return ResponseEntity.ok(room);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/rooms/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        try {
            adminService.deleteRoom(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/equipment")
    public ResponseEntity<List<EquipmentDTO>> getAllEquipment() {
        List<EquipmentDTO> equipment = adminService.getAllEquipment();
        return ResponseEntity.ok(equipment);
    }

    @GetMapping("/equipment/room/{roomId}")
    public ResponseEntity<List<EquipmentDTO>> getEquipmentByRoom(@PathVariable Long roomId) {
        List<EquipmentDTO> equipment = adminService.getEquipmentByRoom(roomId);
        return ResponseEntity.ok(equipment);
    }

    @GetMapping("/equipment/status/{status}")
    public ResponseEntity<List<EquipmentDTO>> getEquipmentByStatus(@PathVariable String status) {
        List<EquipmentDTO> equipment = adminService.getEquipmentByStatus(status);
        return ResponseEntity.ok(equipment);
    }

    @GetMapping("/equipment/{id}")
    public ResponseEntity<EquipmentDTO> getEquipmentById(@PathVariable Long id) {
        try {
            EquipmentDTO equipment = adminService.getEquipmentById(id);
            return ResponseEntity.ok(equipment);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/equipment")
    public ResponseEntity<EquipmentDTO> createEquipment(@Valid @RequestBody EquipmentRequest request) {
        try {
            EquipmentDTO equipment = adminService.createEquipment(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(equipment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/equipment/{id}")
    public ResponseEntity<EquipmentDTO> updateEquipment(@PathVariable Long id, @Valid @RequestBody EquipmentRequest request) {
        try {
            EquipmentDTO equipment = adminService.updateEquipment(id, request);
            return ResponseEntity.ok(equipment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/equipment/{id}")
    public ResponseEntity<Void> deleteEquipment(@PathVariable Long id) {
        try {
            adminService.deleteEquipment(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/promotions")
    public ResponseEntity<List<PromotionDTO>> getAllPromotions() {
        List<PromotionDTO> promotions = adminService.getAllPromotions();
        return ResponseEntity.ok(promotions);
    }

    @GetMapping("/promotions/active")
    public ResponseEntity<List<PromotionDTO>> getActivePromotions() {
        List<PromotionDTO> promotions = adminService.getActivePromotions();
        return ResponseEntity.ok(promotions);
    }

    @GetMapping("/promotions/{id}")
    public ResponseEntity<PromotionDTO> getPromotionById(@PathVariable Long id) {
        try {
            PromotionDTO promotion = adminService.getPromotionById(id);
            return ResponseEntity.ok(promotion);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/promotions")
    public ResponseEntity<PromotionDTO> createPromotion(@Valid @RequestBody PromotionRequest request) {
        try {
            PromotionDTO promotion = adminService.createPromotion(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(promotion);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/promotions/{id}")
    public ResponseEntity<PromotionDTO> updatePromotion(@PathVariable Long id, @Valid @RequestBody PromotionRequest request) {
        try {
            PromotionDTO promotion = adminService.updatePromotion(id, request);
            return ResponseEntity.ok(promotion);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/promotions/{id}")
    public ResponseEntity<Void> deletePromotion(@PathVariable Long id) {
        try {
            adminService.deletePromotion(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/coupons")
    public ResponseEntity<List<CouponDTO>> getAllCoupons() {
        List<CouponDTO> coupons = adminService.getAllCoupons();
        return ResponseEntity.ok(coupons);
    }

    @GetMapping("/coupons/{id}")
    public ResponseEntity<CouponDTO> getCouponById(@PathVariable Long id) {
        try {
            CouponDTO coupon = adminService.getCouponById(id);
            return ResponseEntity.ok(coupon);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/coupons")
    public ResponseEntity<CouponDTO> createCoupon(@Valid @RequestBody CouponRequest request) {
        try {
            CouponDTO coupon = adminService.createCoupon(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(coupon);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/coupons/{id}")
    public ResponseEntity<CouponDTO> updateCoupon(@PathVariable Long id,
                                                  @Valid @RequestBody CouponRequest request) {
        try {
            CouponDTO coupon = adminService.updateCoupon(id, request);
            return ResponseEntity.ok(coupon);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/coupons/{id}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable Long id) {
        try {
            adminService.deleteCoupon(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/coupons/{id}/deactivate")
    public ResponseEntity<CouponDTO> deactivateCoupon(@PathVariable Long id) {
        try {
            CouponDTO coupon = adminService.deactivateCoupon(id);
            return ResponseEntity.ok(coupon);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/employees")
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees() {
        List<EmployeeDTO> employees = adminService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/employees/{id}")
    public ResponseEntity<EmployeeDTO> getEmployeeById(@PathVariable Long id) {
        try {
            EmployeeDTO employee = adminService.getEmployeeById(id);
            return ResponseEntity.ok(employee);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/employees")
    public ResponseEntity<EmployeeDTO> createEmployee(@Valid @RequestBody CreateEmployeeRequest request) {
        try {
            EmployeeDTO employee = adminService.createEmployee(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(employee);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/employees/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        try {
            adminService.deleteEmployee(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/classes")
    public ResponseEntity<List<TrainingClassDTO>> getAllTrainingClasses() {
        List<TrainingClassDTO> classes = adminService.getAllTrainingClasses();
        return ResponseEntity.ok(classes);
    }

    @GetMapping("/classes/upcoming")
    public ResponseEntity<List<TrainingClassDTO>> getUpcomingClasses() {
        List<TrainingClassDTO> classes = adminService.getUpcomingClasses();
        return ResponseEntity.ok(classes);
    }

    @GetMapping("/classes/between")
    public ResponseEntity<List<TrainingClassDTO>> getClassesBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<TrainingClassDTO> classes = adminService.getClassesBetween(start, end);
        return ResponseEntity.ok(classes);
    }

    @PostMapping("/classes")
    public ResponseEntity<TrainingClassDTO> createTrainingClass(@Valid @RequestBody TrainingClassRequest request) {
        try {
            TrainingClassDTO trainingClass = adminService.createTrainingClass(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(trainingClass);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/classes/{id}")
    public ResponseEntity<TrainingClassDTO> updateTrainingClass(@PathVariable Long id,
                                                                @Valid @RequestBody TrainingClassRequest request) {
        try {
            TrainingClassDTO trainingClass = adminService.updateTrainingClass(id, request);
            return ResponseEntity.ok(trainingClass);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/classes/{id}")
    public ResponseEntity<Void> deleteTrainingClass(@PathVariable Long id) {
        try {
            adminService.deleteTrainingClass(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/schedule-change-requests")
    public ResponseEntity<List<ScheduleChangeRequestDTO>> getAllScheduleChangeRequests() {
        List<ScheduleChangeRequestDTO> requests = adminService.getAllScheduleChangeRequests();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/schedule-change-requests/pending")
    public ResponseEntity<List<ScheduleChangeRequestDTO>> getPendingScheduleChangeRequests() {
        List<ScheduleChangeRequestDTO> requests = adminService.getPendingScheduleChangeRequests();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/schedule-change-requests/{id}")
    public ResponseEntity<ScheduleChangeRequestDTO> getScheduleChangeRequestById(@PathVariable Long id) {
        try {
            ScheduleChangeRequestDTO request = adminService.getScheduleChangeRequestById(id);
            return ResponseEntity.ok(request);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/schedule-change-requests/{id}/review")
    public ResponseEntity<ScheduleChangeRequestDTO> reviewScheduleChangeRequest(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody ReviewScheduleChangeRequest reviewRequest) {
        try {
            String adminEmail = authentication.getName();
            ScheduleChangeRequestDTO request = adminService.reviewScheduleChangeRequest(id, adminEmail, reviewRequest);
            return ResponseEntity.ok(request);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/clients")
    public ResponseEntity<List<ClientProfileDTO>> getAllClients() {
        List<ClientProfileDTO> clients = adminService.getAllClients();
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/clients/{id}")
    public ResponseEntity<ClientProfileDTO> getClientById(@PathVariable Long id) {
        try {
            ClientProfileDTO client = adminService.getClientById(id);
            return ResponseEntity.ok(client);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/membership-types")
    public ResponseEntity<List<MembershipTypeDTO>> getMembershipTypes() {
        List<MembershipTypeDTO> membershipTypes = adminService.getMembershipTypes();
        return ResponseEntity.ok(membershipTypes);
    }
}