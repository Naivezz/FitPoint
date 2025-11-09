package com.naivez.fithub.controller;

import com.naivez.fithub.dto.TrainingClassDTO;
import com.naivez.fithub.dto.TrainingClassRequest;
import com.naivez.fithub.service.TrainingClassService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class TrainingClassController {

    private final TrainingClassService trainingClassService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER', 'CLIENT')")
    public ResponseEntity<List<TrainingClassDTO>> getAllTrainingClasses() {
        List<TrainingClassDTO> classes = trainingClassService.getAllTrainingClasses();
        return ResponseEntity.ok(classes);
    }

    @GetMapping("/upcoming")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER', 'CLIENT')")
    public ResponseEntity<List<TrainingClassDTO>> getUpcomingClasses() {
        List<TrainingClassDTO> classes = trainingClassService.getUpcomingClasses();
        return ResponseEntity.ok(classes);
    }

    @GetMapping("/between")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER', 'CLIENT')")
    public ResponseEntity<List<TrainingClassDTO>> getClassesBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<TrainingClassDTO> classes = trainingClassService.getClassesBetween(start, end);
        return ResponseEntity.ok(classes);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TrainingClassDTO> createTrainingClass(@Valid @RequestBody TrainingClassRequest request) {
        try {
            TrainingClassDTO trainingClass = trainingClassService.createTrainingClass(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(trainingClass);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TrainingClassDTO> updateTrainingClass(@PathVariable Long id,
                                                                @Valid @RequestBody TrainingClassRequest request) {
        try {
            TrainingClassDTO trainingClass = trainingClassService.updateTrainingClass(id, request);
            return ResponseEntity.ok(trainingClass);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTrainingClass(@PathVariable Long id) {
        try {
            trainingClassService.deleteTrainingClass(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}