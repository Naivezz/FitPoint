package com.naivez.fithub.controller;

import com.naivez.fithub.dto.CreateEmployeeRequest;
import com.naivez.fithub.dto.EmployeeDTO;
import com.naivez.fithub.dto.ReviewScheduleChangeRequest;
import com.naivez.fithub.dto.ScheduleChangeRequestDTO;
import com.naivez.fithub.service.EmployeeService;
import com.naivez.fithub.service.ScheduleChangeRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final EmployeeService employeeService;
    private final ScheduleChangeRequestService scheduleChangeRequestService;

    @GetMapping("/employees")
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees() {
        List<EmployeeDTO> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/employees/{id}")
    public ResponseEntity<EmployeeDTO> getEmployeeById(@PathVariable Long id) {
        try {
            EmployeeDTO employee = employeeService.getEmployeeById(id);
            return ResponseEntity.ok(employee);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/employees")
    public ResponseEntity<EmployeeDTO> createEmployee(@Valid @RequestBody CreateEmployeeRequest request) {
        try {
            EmployeeDTO employee = employeeService.createEmployee(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(employee);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/employees/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        try {
            employeeService.deleteEmployee(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/schedule-change-requests")
    public ResponseEntity<List<ScheduleChangeRequestDTO>> getAllScheduleChangeRequests() {
        List<ScheduleChangeRequestDTO> requests = scheduleChangeRequestService.getAllScheduleChangeRequests();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/schedule-change-requests/pending")
    public ResponseEntity<List<ScheduleChangeRequestDTO>> getPendingScheduleChangeRequests() {
        List<ScheduleChangeRequestDTO> requests = scheduleChangeRequestService.getPendingScheduleChangeRequests();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/schedule-change-requests/{id}")
    public ResponseEntity<ScheduleChangeRequestDTO> getScheduleChangeRequestById(@PathVariable Long id) {
        try {
            ScheduleChangeRequestDTO request = scheduleChangeRequestService.getScheduleChangeRequestById(id);
            return ResponseEntity.ok(request);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/schedule-change-requests/{id}/review")
    public ResponseEntity<ScheduleChangeRequestDTO> reviewScheduleChangeRequest(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long id,
            @Valid @RequestBody ReviewScheduleChangeRequest reviewRequest) {
        try {
            ScheduleChangeRequestDTO request = scheduleChangeRequestService.reviewScheduleChangeRequest(id, user.getUsername(), reviewRequest);
            return ResponseEntity.ok(request);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}